/*
 * #%L
 * de.metas.business.rest-api-impl
 * %%
 * Copyright (C) 2022 metas GmbH
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

package de.metas.rest_api.v2.pricing.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.metas.RestUtils;
import de.metas.bpartner.BPartnerId;
import de.metas.bpartner.service.IBPartnerDAO;
import de.metas.common.pricing.v2.productprice.JsonResponsePrice;
import de.metas.common.pricing.v2.productprice.JsonResponsePriceList;
import de.metas.common.rest_api.common.JsonMetasfreshId;
import de.metas.common.rest_api.v2.JsonSOTrx;
import de.metas.currency.CurrencyCode;
import de.metas.externalreference.ExternalIdentifier;
import de.metas.location.CountryId;
import de.metas.location.ICountryDAO;
import de.metas.money.CurrencyId;
import de.metas.organization.IOrgDAO;
import de.metas.organization.OrgId;
import de.metas.pricing.PriceListId;
import de.metas.pricing.PriceListVersionId;
import de.metas.pricing.PricingSystemId;
import de.metas.pricing.exceptions.PriceListVersionNotFoundException;
import de.metas.pricing.service.IPriceListDAO;
import de.metas.pricing.service.PriceListsCollection;
import de.metas.product.ProductId;
import de.metas.rest_api.bpartner_pricelist.BpartnerPriceListServicesFacade;
import de.metas.rest_api.v2.bpartner.bpartnercomposite.JsonRetrieverService;
import de.metas.rest_api.v2.product.ProductRestService;
import de.metas.util.Check;
import de.metas.util.Services;
import de.metas.util.web.exception.InvalidIdentifierException;
import lombok.Builder;
import lombok.NonNull;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_Country;
import org.compiere.model.I_M_PriceList;
import org.compiere.model.I_M_ProductPrice;
import org.compiere.util.TimeUtil;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class SearchProductPricesCommand
{
	private final IBPartnerDAO bpartnerDAO = Services.get(IBPartnerDAO.class);
	private final IPriceListDAO priceListDAO = Services.get(IPriceListDAO.class);
	private final ICountryDAO countryDAO = Services.get(ICountryDAO.class);
	private final IOrgDAO orgDAO = Services.get(IOrgDAO.class);

	// services
	private final ProductRestService productRestService;
	private final JsonRetrieverService jsonRetrieverService;
	private final BpartnerPriceListServicesFacade bpartnerPriceListServicesFacade;

	// parameters
	private final ExternalIdentifier bpartnerIdentifier;
	private final ExternalIdentifier productIdentifier;
	private final LocalDate targetDate;
	private final OrgId orgId;

	@Builder
	public SearchProductPricesCommand(
			@NonNull final ProductRestService productRestService,
			@NonNull final JsonRetrieverService jsonRetrieverService,
			@NonNull final BpartnerPriceListServicesFacade bpartnerPriceListServicesFacade,
			@NonNull final ExternalIdentifier bpartnerIdentifier,
			@NonNull final ExternalIdentifier productIdentifier,
			@NonNull final LocalDate targetDate,
			@Nullable final String orgCode)
	{
		this.productRestService = productRestService;
		this.jsonRetrieverService = jsonRetrieverService;
		this.bpartnerPriceListServicesFacade = bpartnerPriceListServicesFacade;
		this.bpartnerIdentifier = bpartnerIdentifier;
		this.productIdentifier = productIdentifier;
		this.targetDate = targetDate;
		this.orgId = RestUtils.retrieveOrgIdOrDefault(orgCode);
	}

	public static class SearchProductPricesCommandBuilder
	{
		public JsonResponsePriceList execute()
		{
			return build().execute();
		}
	}

	@NonNull
	public JsonResponsePriceList execute()
	{
		final ProductId productId = getProductId();

		final Set<PricingSystemId> assignedBPPricingSystemIds = getBPartnerPricingSystemIds();

		if (assignedBPPricingSystemIds.isEmpty())
		{
			return JsonResponsePriceList.builder().prices(ImmutableList.of()).build();
		}

		final String productValue = bpartnerPriceListServicesFacade.getProductValue(productId);

		final ZonedDateTime priceDate = getTargetDateAndTime();

		final ImmutableList.Builder<JsonResponsePrice> jsonResponsePriceCollector = ImmutableList.builder();

		for (final PricingSystemId pricingSystemId : assignedBPPricingSystemIds)
		{
			final PriceListsCollection priceListsCollection = priceListDAO.retrievePriceListsCollectionByPricingSystemId(pricingSystemId);

			for (final I_M_PriceList priceList : priceListsCollection.getPriceList())
			{
				final CurrencyCode priceListCurrencyCode = bpartnerPriceListServicesFacade.getCurrencyCodeById(CurrencyId.ofRepoId(priceList.getC_Currency_ID()));

				streamPrices(productId, PriceListId.ofRepoId(priceList.getM_PriceList_ID()), priceDate)
						.map(productPrice -> JsonResponsePrice.builder()
								.productId(JsonMetasfreshId.of(productId.getRepoId()))
								.productCode(productValue)
								.taxCategoryId(JsonMetasfreshId.of(productPrice.getC_TaxCategory_ID()))
								.currencyCode(priceListCurrencyCode.toThreeLetterCode())
								.countryCode(getCountryCode(priceList))
								.price(productPrice.getPriceStd())
								.isSOTrx(JsonSOTrx.ofBoolean(priceList.isSOPriceList()))
								.build())
						.forEach(jsonResponsePriceCollector::add);
			}
		}

		return JsonResponsePriceList.builder()
				.prices(jsonResponsePriceCollector.build())
				.build();
	}

	@NonNull
	private Set<PricingSystemId> getBPartnerPricingSystemIds()
	{
		final I_C_BPartner bPartner = bpartnerDAO.getById(getBPartnerId());

		final ImmutableSet.Builder<PricingSystemId> pricingSystemIds = ImmutableSet.builder();

		Optional.ofNullable(PricingSystemId.ofRepoIdOrNull(bPartner.getM_PricingSystem_ID()))
				.ifPresent(pricingSystemIds::add);

		Optional.ofNullable(PricingSystemId.ofRepoIdOrNull(bPartner.getPO_PricingSystem_ID()))
				.ifPresent(pricingSystemIds::add);

		return pricingSystemIds.build();
	}

	@Nullable
	private String getCountryCode(@NonNull final I_M_PriceList priceList)
	{
		return Optional.ofNullable(CountryId.ofRepoIdOrNull(priceList.getC_Country_ID()))
				.map(countryDAO::getById)
				.map(I_C_Country::getCountryCode)
				.orElse(null);
	}

	@NonNull
	private BPartnerId getBPartnerId()
	{
		return jsonRetrieverService.resolveBPartnerExternalIdentifier(bpartnerIdentifier, orgId)
				.orElseThrow(() -> new InvalidIdentifierException("No BPartner found for identifier")
						.appendParametersToMessage()
						.setParameter("ExternalIdentifier", bpartnerIdentifier));
	}

	@NonNull
	private ProductId getProductId()
	{
		return productRestService.resolveProductExternalIdentifier(productIdentifier, orgId)
				.orElseThrow(() -> new InvalidIdentifierException("Fail to resolve product external identifier")
						.appendParametersToMessage()
						.setParameter("ExternalIdentifier", productIdentifier));
	}

	@NonNull
	private ZonedDateTime getTargetDateAndTime()
	{
		final ZonedDateTime zonedDateTime = TimeUtil.asZonedDateTime(targetDate, orgDAO.getTimeZone(orgId));
		Check.assumeNotNull(zonedDateTime, "zonedDateTime is not null!");

		return zonedDateTime;
	}

	@NonNull
	private Stream<I_M_ProductPrice> streamPrices(
			@NonNull final ProductId productId,
			@NonNull final PriceListId priceListId,
			@NonNull final ZonedDateTime priceDate)
	{
		final PriceListVersionId priceListVersionId;
		try
		{
			priceListVersionId = bpartnerPriceListServicesFacade.getPriceListVersionId(priceListId, priceDate);
		}
		catch (final PriceListVersionNotFoundException exception)
		{
			return Stream.empty();
		}

		return bpartnerPriceListServicesFacade.getProductPricesByPLVAndProduct(priceListVersionId, productId)
				.stream();
	}
}
