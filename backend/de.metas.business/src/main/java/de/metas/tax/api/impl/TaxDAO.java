package de.metas.tax.api.impl;

import ch.qos.logback.classic.Level;
import de.metas.bpartner.BPartnerId;
import de.metas.bpartner.BPartnerLocationId;
import de.metas.bpartner.service.IBPartnerDAO;
import de.metas.bpartner.service.IBPartnerOrgBL;
import de.metas.cache.annotation.CacheCtx;
import de.metas.i18n.ITranslatableString;
import de.metas.i18n.TranslatableStrings;
import de.metas.location.CountryId;
import de.metas.location.ICountryAreaBL;
import de.metas.location.ICountryDAO;
import de.metas.location.ILocationDAO;
import de.metas.location.LocationId;
import de.metas.logging.LogManager;
import de.metas.organization.IFiscalRepresentationBL;
import de.metas.organization.IOrgDAO;
import de.metas.organization.OrgId;
import de.metas.tax.api.ITaxDAO;
import de.metas.tax.api.Tax;
import de.metas.tax.api.TaxCategoryId;
import de.metas.tax.api.TaxId;
import de.metas.tax.api.TaxQuery;
import de.metas.tax.api.TaxUtils;
import de.metas.tax.api.TypeOfDestCountry;
import de.metas.tax.model.I_C_VAT_SmallBusiness;
import de.metas.util.Check;
import de.metas.util.ILoggable;
import de.metas.util.Loggables;
import de.metas.util.Services;
import de.metas.util.lang.Percent;
import lombok.NonNull;
import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.dao.IQueryBuilder;
import org.adempiere.ad.dao.impl.CompareQueryFilter.Operator;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.exceptions.ExemptTaxNotFoundException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.proxy.Cached;
import org.adempiere.warehouse.WarehouseId;
import org.adempiere.warehouse.api.IWarehouseBL;
import org.compiere.model.IQuery;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_BPartner_Location;
import org.compiere.model.I_C_Tax;
import org.compiere.model.I_C_TaxCategory;
import org.compiere.model.X_C_Tax;
import org.compiere.util.Env;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static de.metas.common.util.CoalesceUtil.coalesce;
import static de.metas.tax.api.TypeOfDestCountry.DOMESTIC;
import static de.metas.tax.api.TypeOfDestCountry.OUTSIDE_COUNTRY_AREA;
import static de.metas.tax.api.TypeOfDestCountry.WITHIN_COUNTRY_AREA;
import static org.adempiere.model.InterfaceWrapperHelper.loadOutOfTrx;

public class TaxDAO implements ITaxDAO
{
	private final static transient Logger logger = LogManager.getLogger(TaxDAO.class);

	private final IQueryBL queryBL = Services.get(IQueryBL.class);
	private final IOrgDAO orgDAO = Services.get(IOrgDAO.class);
	private final IWarehouseBL warehouseBL = Services.get(IWarehouseBL.class);
	private final ILocationDAO locationDAO = Services.get(ILocationDAO.class);
	private final ICountryDAO countryDAO = Services.get(ICountryDAO.class);
	private final ICountryAreaBL countryAreaBL = Services.get(ICountryAreaBL.class);
	private final IBPartnerDAO bPartnerDAO = Services.get(IBPartnerDAO.class);
	private final IBPartnerOrgBL bPartnerOrgBL = Services.get(IBPartnerOrgBL.class);
	private final IFiscalRepresentationBL fiscalRepresentationBL = Services.get(IFiscalRepresentationBL.class);

	@Override
	public Tax getTaxById(final int taxRepoId)
	{
		return getTaxById(TaxId.ofRepoId(taxRepoId));
	}

	@Override
	public Tax getTaxById(@NonNull final TaxId taxId)
	{
		return TaxUtils.from(loadOutOfTrx(taxId, I_C_Tax.class));
	}

	@Override
	@Nullable
	public Tax getTaxByIdOrNull(final int taxRepoId)
	{
		if (taxRepoId <= 0)
		{
			return null;
		}

		return TaxUtils.from(loadOutOfTrx(taxRepoId, I_C_Tax.class));
	}

	@Override
	@Cached(cacheName = I_C_VAT_SmallBusiness.Table_Name + "#By#C_BPartner_ID#Date")
	public boolean retrieveIsTaxExemptSmallBusiness(
			@NonNull final BPartnerId bPartnerId,
			@NonNull final Timestamp date)
	{
		return queryBL.createQueryBuilder(I_C_VAT_SmallBusiness.class)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_C_VAT_SmallBusiness.COLUMNNAME_C_BPartner_ID, bPartnerId)
				.addCompareFilter(I_C_VAT_SmallBusiness.COLUMNNAME_ValidFrom, Operator.LESS_OR_EQUAL, date)
				.addCompareFilter(I_C_VAT_SmallBusiness.COLUMNNAME_ValidTo, Operator.GREATER_OR_EQUAL, date)
				.create()
				.anyMatch();
	}

	@Override
	public TaxId retrieveExemptTax(@NonNull final OrgId orgId)
	{
		final int C_Tax_ID = queryBL.createQueryBuilder(I_C_Tax.class)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_C_Tax.COLUMNNAME_IsTaxExempt, true)
				.addEqualsFilter(I_C_Tax.COLUMNNAME_AD_Org_ID, orgId)
				.orderByDescending(I_C_Tax.COLUMNNAME_Rate)
				.create()
				.firstId();
		if (C_Tax_ID <= 0)
		{
			throw new ExemptTaxNotFoundException(orgId.getRepoId());
		}
		return TaxId.ofRepoId(C_Tax_ID);
	}

	@Override
	public TaxId getDefaultTaxId(final I_C_TaxCategory taxCategory)
	{
		final Properties ctx = InterfaceWrapperHelper.getCtx(taxCategory);
		final String trxName = InterfaceWrapperHelper.getTrxName(taxCategory);
		final I_C_Tax tax;

		final List<I_C_Tax> list = queryBL.createQueryBuilder(I_C_Tax.class, ctx, trxName)
				.addEqualsFilter(I_C_Tax.COLUMNNAME_C_TaxCategory_ID, taxCategory.getC_TaxCategory_ID())
				.addEqualsFilter(I_C_Tax.COLUMNNAME_IsDefault, true)
				.create()
				.list();
		if (list.size() == 1)
		{
			tax = list.get(0);
		}
		else
		{
			// Error - should only be one default
			throw new AdempiereException("TooManyDefaults");
		}

		return TaxId.ofRepoId(tax.getC_Tax_ID());
	}

	@Override
	@Cached(cacheName = I_C_Tax.Table_Name + "#NoTaxFound")
	public TaxId retrieveNoTaxFoundId(@CacheCtx final Properties ctx)
	{
		final I_C_Tax tax = queryBL.createQueryBuilder(I_C_Tax.class, ctx, ITrx.TRXNAME_None)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_C_Tax.COLUMNNAME_C_Tax_ID, Tax.C_TAX_ID_NO_TAX_FOUND)
				.create()
				.firstOnlyNotNull(I_C_Tax.class);

		return TaxId.ofRepoId(tax.getC_Tax_ID());
	}

	@Override
	@Cached(cacheName = I_C_TaxCategory.Table_Name + "#NoTaxFound")
	public I_C_TaxCategory retrieveNoTaxCategoryFound(@CacheCtx final Properties ctx)
	{
		return queryBL.createQueryBuilder(I_C_TaxCategory.class, ctx, ITrx.TRXNAME_None)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_C_TaxCategory.COLUMNNAME_C_TaxCategory_ID, TaxCategoryId.NOT_FOUND)
				.create()
				.firstOnlyNotNull(I_C_TaxCategory.class);
	}

	@Override
	public int findTaxCategoryId(@NonNull final TaxCategoryQuery query)
	{
		final IQueryBL queryBL = this.queryBL;

		final IQueryBuilder<I_C_TaxCategory> queryBuilder = queryBL.createQueryBuilder(I_C_TaxCategory.class);

		final IQuery<I_C_Tax> querytaxes = queryBL.createQueryBuilder(I_C_Tax.class)
				.addInArrayFilter(I_C_Tax.COLUMNNAME_C_Country_ID, null, query.getCountryId())
				.create();

		return queryBuilder
				.addInArrayFilter(I_C_TaxCategory.COLUMN_VATType, query.getType().getValue(), null)
				.addOnlyActiveRecordsFilter()
				.addOnlyContextClient()
				.addInSubQueryFilter(I_C_TaxCategory.COLUMNNAME_C_TaxCategory_ID, I_C_Tax.COLUMNNAME_C_TaxCategory_ID, querytaxes)
				.orderBy(I_C_TaxCategory.COLUMNNAME_Name)
				.create()
				.firstId();
	}

	@Override
	public I_C_TaxCategory getTaxCategoryById(@NonNull final TaxCategoryId id)
	{
		return loadOutOfTrx(id, I_C_TaxCategory.class);
	}

	@Override
	public ITranslatableString getTaxCategoryNameById(@Nullable final TaxCategoryId id)
	{
		if (id == null)
		{
			return TranslatableStrings.anyLanguage("?");
		}

		final I_C_TaxCategory taxCategory = getTaxCategoryById(id);
		if (taxCategory == null)
		{
			return TranslatableStrings.anyLanguage("<" + id.getRepoId() + ">");
		}

		return InterfaceWrapperHelper.getModelTranslationMap(taxCategory)
				.getColumnTrl(I_C_TaxCategory.COLUMNNAME_Name, taxCategory.getName());
	}

	@Override
	public Optional<TaxCategoryId> getTaxCategoryIdByName(@NonNull final String name)
	{
		final TaxCategoryId taxCategoryId = queryBL.createQueryBuilder(I_C_TaxCategory.class)
				.addEqualsFilter(I_C_TaxCategory.COLUMN_Name, name)
				.create()
				.firstId(TaxCategoryId::ofRepoIdOrNull);

		return Optional.ofNullable(taxCategoryId);
	}

	@Override
	public Percent getRateById(@NonNull final TaxId taxId)
	{
		final Tax tax = getTaxById(taxId);
		return Percent.of(tax.getRate());
	}

	@Override
	@Nullable
	public Tax getBy(@NonNull final TaxQuery taxQuery)
	{
		final BPartnerId bpartnerId = taxQuery.getBpartnerId();
		final Timestamp dateOfInterest = taxQuery.getDateOfInterest();
		final OrgId orgId = taxQuery.getOrgId();

		if (bpartnerId != null && orgId != null && retrieveIsTaxExemptSmallBusiness(bpartnerId, dateOfInterest))
		{
			final TaxId exemptTax = retrieveExemptTax(orgId);
			return getTaxById(exemptTax);
		}
		final List<Tax> taxes = getTaxesFromQuery(taxQuery);

		if (taxes.size() > 1)
		{
			final Tax firstTax = taxes.get(0);
			final Tax secondTax = taxes.get(1);
			final boolean multipleTaxesWithSameSeq = Objects.equals(firstTax.getSeqNo(), secondTax.getSeqNo());
			if (multipleTaxesWithSameSeq)
			{
				throw new AdempiereException("Multiple taxes have the same seqNo: " + firstTax.getTaxId() + " and " + secondTax.getTaxId());
			}

			Loggables.withLogger(logger, Level.WARN).addLog("Multiple C_Tax records {} match the search criteria. Returning the first record based on seqNo.", getTaxIds(taxes));
		}
		else if (taxes.size() == 1)
		{
			Loggables.withLogger(logger, Level.DEBUG).addLog("Exact match found: {}", taxes.get(0).getTaxId().getRepoId());
		}
		return taxes.isEmpty() ? null : taxes.get(0);
	}

	@NonNull
	private List<Tax> getTaxesFromQuery(@NonNull final TaxQuery taxQuery)
	{
		final IQueryBuilder<I_C_Tax> queryBuilder = getTaxQueryBuilder(taxQuery);
		return queryBuilder.create()
				.stream()
				.map(TaxUtils::from)
				.collect(Collectors.toList());
	}

	@NonNull
	private String getTaxIds(final List<Tax> taxes)
	{
		return taxes.stream()
				.map(Tax::getTaxId)
				.map(TaxId::getRepoId)
				.map(Object::toString)
				.collect(Collectors.joining(", "));
	}

	@NonNull
	private IQueryBuilder<I_C_Tax> getTaxQueryBuilder(@NonNull final TaxQuery taxQuery)
	{
		final ILoggable loggable = Loggables.withLogger(logger, Level.DEBUG);

		loggable.addLog("Using query {}", taxQuery);
		final IQueryBuilder<I_C_Tax> queryBuilder = queryBL.createQueryBuilder(I_C_Tax.class);

		OrgId orgId = taxQuery.getOrgId();
		CountryId countryId = taxQuery.getFromCountryId();

		if (countryId != null)
		{
			loggable.addLog("Country ID provided in query: {}", countryId);
		}
		else
		{
			final WarehouseId warehouseId = taxQuery.getWarehouseId();
			if (warehouseId != null)
			{
				final CountryId warehouseCountryId = warehouseBL.getCountryId(warehouseId);
				orgId = warehouseBL.getWarehouseOrgId(warehouseId);
				loggable.addLog("Country ID based on warehouse: {}", warehouseCountryId);
				if (warehouseCountryId != null)
				{
					countryId = warehouseCountryId;
				}
			}
			else if (orgId != null)
			{
				loggable.addLog("Org ID: {} or any", orgId);
				queryBuilder.addInArrayFilter(I_C_Tax.COLUMNNAME_AD_Org_ID, orgId, OrgId.ANY);
				countryId = bPartnerOrgBL.getOrgCountryId(orgId);
				loggable.addLog("Country ID based on organization: {}", countryId);
			}
			if (countryId == null)
			{
				throw new AdempiereException("No country could be identified for the given orgId: " + orgId + " and warehouse: " + warehouseId);
			}
		}

		final boolean euOneStopShop = orgId != null && orgDAO.isEUOneStopShop(orgId);
		if (orgId == null || !euOneStopShop)
		{
			queryBuilder.addEqualsFilter(I_C_Tax.COLUMNNAME_C_Country_ID, countryId);
		}
		else
		{
			loggable.addLog("Is EU OneStopShop");
		}

		final Timestamp dateOfInterest = taxQuery.getDateOfInterest();
		queryBuilder.addCompareFilter(I_C_Tax.COLUMNNAME_ValidFrom, Operator.LESS_OR_EQUAL, dateOfInterest);
		queryBuilder.addCompareFilter(I_C_Tax.COLUMNNAME_ValidTo, Operator.GREATER_OR_EQUAL, dateOfInterest);
		loggable.addLog("Date of Interest<= {}", dateOfInterest);

		final TaxCategoryId taxCategoryId = taxQuery.getTaxCategoryId();
		if (taxCategoryId != null)
		{
			queryBuilder.addEqualsFilter(I_C_Tax.COLUMNNAME_C_TaxCategory_ID, taxCategoryId);
			loggable.addLog("Tax Category ID: {}", taxCategoryId.getRepoId());
		}

		if (taxQuery.getIsSoTrx() != null)
		{
			if (taxQuery.getIsSoTrx())
			{
				queryBuilder.addInArrayFilter(I_C_Tax.COLUMNNAME_SOPOType, X_C_Tax.SOPOTYPE_Both, X_C_Tax.SOPOTYPE_SalesTax);
				loggable.addLog("SOPOType is either {} or {}", X_C_Tax.SOPOTYPE_Both, X_C_Tax.SOPOTYPE_SalesTax);
			}
			else
			{
				queryBuilder.addInArrayFilter(I_C_Tax.COLUMNNAME_SOPOType, X_C_Tax.SOPOTYPE_Both, X_C_Tax.SOPOTYPE_PurchaseTax);
				loggable.addLog("SOPOType is either {} or {}", X_C_Tax.SOPOTYPE_Both, X_C_Tax.SOPOTYPE_PurchaseTax);
			}
		}

		final BPartnerLocationId bPartnerLocationId = taxQuery.getBPartnerLocationId();
		final BPartnerId bpartnerId = taxQuery.getBpartnerId();
		if (bpartnerId != null)
		{
			final I_C_BPartner bpartner = bPartnerDAO.getById(bpartnerId);
			final boolean requiresTaxCertificate = !Check.isBlank(bpartner.getVATaxID());
			loggable.addLog("RequiresTaxCertificate: {}", requiresTaxCertificate);
			queryBuilder.addEqualsFilter(I_C_Tax.COLUMNNAME_RequiresTaxCertificate, requiresTaxCertificate);
		}
		if (bPartnerLocationId != null)
		{
			final CountryId toCountryId = getCountryId(bPartnerLocationId);
			if (euOneStopShop)
			{
				loggable.addLog("To country ID from bpartnerLocation: {}", toCountryId);
				queryBuilder.addEqualsFilter(I_C_Tax.COLUMNNAME_To_Country_ID, toCountryId);
			}
			else
			{
				loggable.addLog("To country ID from bpartnerLocation: {} or NULL", toCountryId);
				queryBuilder.addInArrayFilter(I_C_Tax.COLUMNNAME_To_Country_ID, toCountryId, null);
			}

			final TypeOfDestCountry typeOfDestCountry = getTypeOfDestCountry(countryId, toCountryId);
			loggable.addLog("Type of dest country: {} or NULL", typeOfDestCountry);
			if (typeOfDestCountry != null)
			{
				queryBuilder.addInArrayFilter(I_C_Tax.COLUMNNAME_TypeOfDestCountry, typeOfDestCountry.getCode(), null);
			}

			final Timestamp fiscalRepresentationFromDate = coalesce(taxQuery.getDateOfInterest(), Env.getDate());
			final boolean hasFiscalRepresentation = fiscalRepresentationBL.hasFiscalRepresentation(toCountryId, orgId, fiscalRepresentationFromDate);
			final boolean isNonEU = OUTSIDE_COUNTRY_AREA.equals(typeOfDestCountry);
			if (!euOneStopShop && isNonEU)
			{
				loggable.addLog("Has fiscal Representation = {}", hasFiscalRepresentation);
				queryBuilder.addEqualsFilter(I_C_Tax.COLUMNNAME_IsFiscalRepresentation, hasFiscalRepresentation);
			}
		}

		queryBuilder.orderBy(I_C_Tax.COLUMNNAME_SeqNo);
		return queryBuilder;
	}

	@Nullable
	private TypeOfDestCountry getTypeOfDestCountry(final CountryId countryId, @Nullable final CountryId toCountryId)
	{
		final TypeOfDestCountry typeOfDestCountry;
		if (countryId.equals(toCountryId))
		{
			typeOfDestCountry = DOMESTIC;
		}
		else if (toCountryId == null)
		{
			return null;
		}
		else
		{
			final String countryCode = countryDAO.retrieveCountryCode2ByCountryId(toCountryId);
			final boolean isEULocation = countryAreaBL.isMemberOf(Env.getCtx(),
					ICountryAreaBL.COUNTRYAREAKEY_EU,
					countryCode,
					Env.getDate());
			typeOfDestCountry = isEULocation ? WITHIN_COUNTRY_AREA : OUTSIDE_COUNTRY_AREA;
		}
		return typeOfDestCountry;
	}

	private CountryId getCountryId(final BPartnerLocationId bPartnerLocationId)
	{
		final I_C_BPartner_Location bpartnerLocation = bPartnerDAO.getBPartnerLocationByIdEvenInactive(bPartnerLocationId);
		if (bpartnerLocation == null)
		{
			throw new AdempiereException("No location found for bpartnerLocationId: " + bPartnerLocationId);
		}
		return locationDAO.getCountryIdByLocationId(LocationId.ofRepoId(bpartnerLocation.getC_Location_ID()));
	}

}
