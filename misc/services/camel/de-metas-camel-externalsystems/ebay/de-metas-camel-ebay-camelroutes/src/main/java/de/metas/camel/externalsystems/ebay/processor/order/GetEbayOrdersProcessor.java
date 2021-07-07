/*
 * #%L
 * de-metas-camel-ebay-camelroutes
 * %%
 * Copyright (C) 2021 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

package de.metas.camel.externalsystems.ebay.processor.order;

import com.ebay.api.client.auth.oauth2.CredentialUtil;
import com.ebay.api.client.auth.oauth2.OAuth2Api;
import com.ebay.api.client.auth.oauth2.model.OAuthResponse;
import de.metas.camel.externalsystems.common.ProcessLogger;
import de.metas.camel.externalsystems.ebay.ApiMode;
import de.metas.camel.externalsystems.ebay.CredentialParams;
import de.metas.camel.externalsystems.ebay.EbayConstants;
import de.metas.camel.externalsystems.ebay.EbayImportOrdersRouteContext;
import de.metas.camel.externalsystems.ebay.api.OrderApi;
import de.metas.camel.externalsystems.ebay.api.invoker.ApiClient;
import de.metas.camel.externalsystems.ebay.api.invoker.Configuration;
import de.metas.camel.externalsystems.ebay.api.invoker.auth.OAuth;
import de.metas.camel.externalsystems.ebay.api.model.Order;
import de.metas.camel.externalsystems.ebay.api.model.OrderSearchPagedCollection;
import de.metas.common.externalsystem.ExternalSystemConstants;
import de.metas.common.externalsystem.JsonExternalSystemRequest;
import de.metas.common.util.CoalesceUtil;
import lombok.NonNull;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.metas.camel.externalsystems.common.ExternalSystemCamelConstants.HEADER_ORG_CODE;
import static de.metas.camel.externalsystems.common.ExternalSystemCamelConstants.HEADER_PINSTANCE_ID;
import static de.metas.camel.externalsystems.ebay.EbayConstants.ROUTE_PROPERTY_EBAY_AUTH_CLIENT;

/**
 * Processor to load orders from the eBay fulfilment api.
 */
public class GetEbayOrdersProcessor implements Processor
{

	private static final List<String> SCOPE_LIST = Collections.singletonList("https://api.ebay.com/oauth/api_scope");

	protected Logger log = LoggerFactory.getLogger(getClass());

	private final ProcessLogger processLogger;

	public GetEbayOrdersProcessor(final ProcessLogger processLogger)
	{
		this.processLogger = processLogger;
	}

	@Override
	public void process(final Exchange exchange) throws Exception
	{
		log.debug("Execute ebay order request");

		final JsonExternalSystemRequest request = exchange.getIn().getBody(JsonExternalSystemRequest.class);

		exchange.getIn().setHeader(HEADER_ORG_CODE, request.getOrgCode());
		if (request.getAdPInstanceId() != null)
		{
			exchange.getIn().setHeader(HEADER_PINSTANCE_ID, request.getAdPInstanceId().getValue());

			processLogger.logMessage("Ebay:GetOrders process started!" + Instant.now(), request.getAdPInstanceId().getValue());
		}

		final String updatedAfter = CoalesceUtil.coalesceNotNull(
				request.getParameters().get(ExternalSystemConstants.PARAM_UPDATED_AFTER),
				Instant.ofEpochMilli(0).toString());

		final ApiMode apiMode = ApiMode.valueOf(request.getParameters().get(ExternalSystemConstants.PARAM_API_MODE));

		final OAuth2Api oAuth2Api = Optional.ofNullable(exchange.getIn().getHeader(ROUTE_PROPERTY_EBAY_AUTH_CLIENT, OAuth2Api.class))
				.orElseGet(OAuth2Api::new);

		final OAuthResponse oauth2Response = getAuthResponse(request.getParameters(), oAuth2Api);

		// execut api call
		if (oauth2Response.getAccessToken().isPresent())
		{

			// construct api client or use provided one.
			final OrderApi orderApi;
			if (exchange.getIn().getHeader(EbayConstants.ROUTE_PROPERTY_EBAY_CLIENT) == null)
			{
				log.debug("Constructing ebay api client");

				ApiClient defaultClient = Configuration.getDefaultApiClient();
				defaultClient.setBasePath(apiMode.getEnvironment().getApiEndpoint());

				// Configure OAuth2 access token for authorization: api_auth
				OAuth api_auth = (OAuth)defaultClient.getAuthentication("api_auth");
				api_auth.setAccessToken(oauth2Response.getAccessToken().get().getToken());

				orderApi = new OrderApi(defaultClient);

			}
			else
			{

				log.debug("Using provided ebay api client");
				orderApi = (OrderApi)exchange.getIn().getHeader(EbayConstants.ROUTE_PROPERTY_EBAY_CLIENT);
			}

			String fieldGroups = null;
			String filter = "lastmodifieddate:".concat(updatedAfter);
			String limit = "50";
			String offset = null;
			String orderIds = null;
			OrderSearchPagedCollection response = orderApi.getOrders(fieldGroups, filter, limit, offset, orderIds);

			List<Order> orders = response.getOrders();

			// add orders to exchange
			exchange.getIn().setBody(orders);

			// add order context to exchange.
			final EbayImportOrdersRouteContext ordersContext = EbayImportOrdersRouteContext.builder()
					.orgCode(request.getOrgCode())
					.externalSystemRequest(request)
					.build();

			exchange.setProperty(EbayConstants.ROUTE_PROPERTY_IMPORT_ORDERS_CONTEXT, ordersContext);

		}
		else
		{
			throw new RuntimeException("Ebay:Failed to aquire access token! " + Instant.now());
		}

	}

	private OAuthResponse getAuthResponse(@NonNull final Map<String, String> parameters, @NonNull final OAuth2Api oAuth2Api) throws IOException
	{
		final ApiMode apiMode = ApiMode.valueOf(parameters.get(ExternalSystemConstants.PARAM_API_MODE));

		final Map<String, String> mapCredentialUtil = new HashMap<>();
		mapCredentialUtil.put(CredentialParams.APP_ID.getValue(), parameters.get(ExternalSystemConstants.PARAM_APP_ID));
		mapCredentialUtil.put(CredentialParams.CERT_ID.getValue(), parameters.get(ExternalSystemConstants.PARAM_CERT_ID));
		mapCredentialUtil.put(CredentialParams.REDIRECT_URI.getValue(), parameters.get(ExternalSystemConstants.PARAM_REDIRECT_URL));

		final Map<String, Map<String, String>> parentMap = new HashMap<>();
		parentMap.put(apiMode.getValue(), mapCredentialUtil);

		final Yaml yaml = new Yaml();
		final String output = yaml.dump(parentMap);

		CredentialUtil.load(output);

		return oAuth2Api.getApplicationToken(apiMode.getEnvironment(), SCOPE_LIST);
	}
}
