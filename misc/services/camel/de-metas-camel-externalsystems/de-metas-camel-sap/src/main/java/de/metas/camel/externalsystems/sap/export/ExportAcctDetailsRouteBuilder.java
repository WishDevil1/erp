/*
 * #%L
 * de-metas-camel-sap
 * %%
 * Copyright (C) 2023 metas GmbH
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

package de.metas.camel.externalsystems.sap.export;

import com.fasterxml.jackson.databind.JsonNode;
import de.metas.camel.externalsystems.common.CamelRouteUtil;
import de.metas.camel.externalsystems.common.ProcessLogger;
import de.metas.common.externalsystem.ExternalSystemConstants;
import de.metas.common.externalsystem.JsonExternalSystemRequest;
import de.metas.common.util.Check;
import lombok.NonNull;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import static de.metas.camel.externalsystems.common.ExternalSystemCamelConstants.MF_ERROR_ROUTE_ID;
import static de.metas.camel.externalsystems.common.ExternalSystemCamelConstants.MF_INVOKE_AD_PROCESS;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;

@Component
public class ExportAcctDetailsRouteBuilder extends RouteBuilder
{
	private static final String EXPORT_ACCT_DETAILS_ROUTE_ID = "SAP-exportAcctFact";

	public static final String ROUTE_PROPERTY_EXPORT_ACCT_ROUTE_CONTEXT = "ExportAcctDetailsRouteContext";

	@NonNull
	private final ProcessLogger processLogger;

	public ExportAcctDetailsRouteBuilder(final @NonNull ProcessLogger processLogger)
	{
		this.processLogger = processLogger;
	}

	@Override
	public void configure()
	{
		errorHandler(defaultErrorHandler());

		onException(Exception.class)
				.to(direct(MF_ERROR_ROUTE_ID));

		from(direct(EXPORT_ACCT_DETAILS_ROUTE_ID))
				.routeId(EXPORT_ACCT_DETAILS_ROUTE_ID)
				.streamCaching()
				.process(this::prepareRouteContext)
				.process(new InvokeProcessProcessor())

				.to(direct(MF_INVOKE_AD_PROCESS))

				.unmarshal(CamelRouteUtil.setupJacksonDataFormatFor(getContext(), JsonNode.class))
				.process(new PrepareSAPRequestProcessor(processLogger));
	}

	private void prepareRouteContext(@NonNull final Exchange exchange)
	{
		final JsonExternalSystemRequest request = exchange.getIn().getBody(JsonExternalSystemRequest.class);

		final String url = request.getParameters().get(ExternalSystemConstants.PARAM_EXTERNAL_SYSTEM_HTTP_URL);

		if (Check.isBlank(url))
		{
			throw new RuntimeException("Missing mandatory param: " + ExternalSystemConstants.PARAM_EXTERNAL_SYSTEM_HTTP_URL);
		}

		final String signature = request.getParameters().get(ExternalSystemConstants.PARAM_SAP_Signature);

		if (Check.isBlank(signature))
		{
			throw new RuntimeException("Missing mandatory param: " + ExternalSystemConstants.PARAM_SAP_Signature);
		}

		final String signedPermissions = request.getParameters().get(ExternalSystemConstants.PARAM_SAP_SignedPermissions);

		if (Check.isBlank(signedPermissions))
		{
			throw new RuntimeException("Missing mandatory param: " + ExternalSystemConstants.PARAM_SAP_SignedPermissions);
		}

		final String signedVersion = request.getParameters().get(ExternalSystemConstants.PARAM_SAP_SignedVersion);

		if (Check.isBlank(signedVersion))
		{
			throw new RuntimeException("Missing mandatory param: " + ExternalSystemConstants.PARAM_SAP_SignedVersion);
		}

		final String apiVersion = request.getParameters().get(ExternalSystemConstants.PARAM_SAP_ApiVersion);

		if (Check.isBlank(apiVersion))
		{
			throw new RuntimeException("Missing mandatory param: " + ExternalSystemConstants.PARAM_SAP_ApiVersion);
		}

		final String postAcctDocumentsPath = request.getParameters().get(ExternalSystemConstants.PARAM_SAP_Post_Acct_Documents_Path);

		if (Check.isBlank(postAcctDocumentsPath))
		{
			throw new RuntimeException("Missing mandatory param: " + ExternalSystemConstants.PARAM_SAP_Post_Acct_Documents_Path);
		}

		final SAPCredentials credentials = SAPCredentials.builder()
				.baseUrl(url)
				.apiVersion(apiVersion)
				.postAcctDocumentsPath(postAcctDocumentsPath)
				.signature(signature)
				.signedPermissions(signedPermissions)
				.signedVersion(signedVersion)
				.build();

		final ExportAcctDetailsRouteContext routeContext = ExportAcctDetailsRouteContext.builder()
				.credentials(credentials)
				.build();

		exchange.setProperty(ROUTE_PROPERTY_EXPORT_ACCT_ROUTE_CONTEXT, routeContext);
	}
}
