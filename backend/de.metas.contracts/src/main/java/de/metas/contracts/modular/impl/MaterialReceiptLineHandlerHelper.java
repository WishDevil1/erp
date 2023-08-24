/*
 * #%L
 * de.metas.contracts
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

package de.metas.contracts.modular.impl;

import de.metas.bpartner.BPartnerId;
import de.metas.contracts.FlatrateTermId;
import de.metas.contracts.IFlatrateDAO;
import de.metas.contracts.model.I_C_Flatrate_Term;
import de.metas.contracts.modular.IModularContractTypeHandler;
import de.metas.contracts.modular.interim.logImpl.MaterialReceiptLineInterimContractHandler;
import de.metas.contracts.modular.log.LogEntryContractType;
import de.metas.contracts.modular.log.LogEntryCreateRequest;
import de.metas.contracts.modular.log.LogEntryDocumentType;
import de.metas.contracts.modular.log.LogEntryReverseRequest;
import de.metas.contracts.modular.settings.ModularContractSettings;
import de.metas.contracts.modular.settings.ModularContractSettingsDAO;
import de.metas.contracts.modular.settings.ModularContractTypeId;
import de.metas.i18n.AdMessageKey;
import de.metas.i18n.Language;
import de.metas.i18n.TranslatableStrings;
import de.metas.inout.IInOutDAO;
import de.metas.inout.InOutId;
import de.metas.lang.SOTrx;
import de.metas.organization.IOrgDAO;
import de.metas.organization.LocalDateAndOrgId;
import de.metas.organization.OrgId;
import de.metas.product.IProductBL;
import de.metas.product.ProductId;
import de.metas.quantity.Quantity;
import de.metas.uom.IUOMDAO;
import de.metas.uom.UomId;
import de.metas.util.Services;
import lombok.NonNull;
import org.adempiere.util.lang.impl.TableRecordReference;
import org.adempiere.warehouse.WarehouseId;
import org.compiere.model.I_C_UOM;
import org.compiere.model.I_M_InOut;
import org.compiere.model.I_M_InOutLine;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MaterialReceiptLineHandlerHelper
{
	private final IInOutDAO inoutDao = Services.get(IInOutDAO.class);
	private final IFlatrateDAO flatrateDAO = Services.get(IFlatrateDAO.class);
	private final IUOMDAO uomDAO = Services.get(IUOMDAO.class);
	private final IOrgDAO orgDAO = Services.get(IOrgDAO.class);
	private final IProductBL productBL = Services.get(IProductBL.class);

	private final ModularContractSettingsDAO modularContractSettingsDAO;
	private final static AdMessageKey MSG_ON_REVERSE_DESCRIPTION = AdMessageKey.of("de.metas.contracts.modular.receiptReverseLogDescription");
	private final static AdMessageKey MSG_ON_COMPLETE_DESCRIPTION = AdMessageKey.of("de.metas.contracts.modular.receiptCompleteLogDescription");

	public MaterialReceiptLineHandlerHelper(final ModularContractSettingsDAO modularContractSettingsDAO)
	{
		this.modularContractSettingsDAO = modularContractSettingsDAO;
	}

	public @NonNull Optional<LogEntryCreateRequest> createLogEntryCreateRequest(
			@NonNull final I_M_InOutLine inOutLineRecord,
			@NonNull final FlatrateTermId flatrateTermId,
			@NonNull final LogEntryContractType logEntryContractType)
	{
		final ModularContractSettings modularContractSettings = modularContractSettingsDAO.getByFlatrateTermIdOrNull(flatrateTermId);
		if (modularContractSettings == null)
		{
			return Optional.empty();
		}

		final I_M_InOut inOutRecord = inoutDao.getById(InOutId.ofRepoId(inOutLineRecord.getM_InOut_ID()));
		final I_C_Flatrate_Term flatrateTermRecord = flatrateDAO.getById(flatrateTermId);
		final I_C_UOM uomId = uomDAO.getById(UomId.ofRepoId(inOutLineRecord.getC_UOM_ID()));
		final Quantity quantity = Quantity.of(inOutLineRecord.getMovementQty(), uomId);

		final ProductId productId = ProductId.ofRepoId(inOutLineRecord.getM_Product_ID());
		final String productName = productBL.getProductValueAndName(productId);
		final String description = TranslatableStrings.adMessage(MSG_ON_COMPLETE_DESCRIPTION, productName, quantity)
				.translate(Language.getBaseAD_Language());

		final Class<? extends IModularContractTypeHandler<?>> handler;
		final boolean isBillable;
		if (logEntryContractType.isInterimContractType())
		{
			isBillable = inOutRecord.isInterimInvoiceable();
			handler = MaterialReceiptLineInterimContractHandler.class;
		}
		else
		{
			isBillable = true;
			handler = MaterialReceiptLineModularContractHandler.class;
		}

		final Optional<ModularContractTypeId> modularContractTypeId = modularContractSettings.getModularContractTypeId(handler);

		return modularContractTypeId.map(contractTypeId -> LogEntryCreateRequest.builder()
				.contractId(flatrateTermId)
				.productId(ProductId.ofRepoId(inOutLineRecord.getM_Product_ID()))
				.referencedRecord(TableRecordReference.of(I_M_InOutLine.Table_Name, inOutLineRecord.getM_InOutLine_ID()))
				.collectionPointBPartnerId(BPartnerId.ofRepoId(inOutRecord.getC_BPartner_ID()))
				.producerBPartnerId(BPartnerId.ofRepoId(inOutRecord.getC_BPartner_ID()))
				.invoicingBPartnerId(BPartnerId.ofRepoId(flatrateTermRecord.getBill_BPartner_ID()))
				.warehouseId(WarehouseId.ofRepoId(inOutRecord.getM_Warehouse_ID()))
				.documentType(LogEntryDocumentType.MATERIAL_RECEIPT)
				.contractType(logEntryContractType)
				.soTrx(SOTrx.PURCHASE)
				.processed(false)
				.quantity(quantity)
				.amount(null)
				.transactionDate(LocalDateAndOrgId.ofTimestamp(inOutRecord.getMovementDate(), OrgId.ofRepoId(inOutLineRecord.getAD_Org_ID()), orgDAO::getTimeZone))
				.year(modularContractSettings.getYearAndCalendarId().yearId())
				.description(description)
				.modularContractTypeId(contractTypeId)
				.isBillable(isBillable)
				.build());
	}

	public @NonNull Optional<LogEntryReverseRequest> createLogEntryReverseRequest(final @NonNull I_M_InOutLine inOutLineRecord, final @NonNull FlatrateTermId flatrateTermId, @NonNull final LogEntryContractType logEntryContractType)
	{
		final I_C_UOM uomId = uomDAO.getById(UomId.ofRepoId(inOutLineRecord.getC_UOM_ID()));
		final Quantity quantity = Quantity.of(inOutLineRecord.getMovementQty(), uomId);

		final ProductId productId = ProductId.ofRepoId(inOutLineRecord.getM_Product_ID());
		final String productName = productBL.getProductValueAndName(productId);
		final String description = TranslatableStrings.adMessage(MSG_ON_REVERSE_DESCRIPTION, productName, quantity)
				.translate(Language.getBaseAD_Language());

		return Optional.of(LogEntryReverseRequest.builder()
								   .referencedModel(TableRecordReference.of(I_M_InOutLine.Table_Name, inOutLineRecord.getM_InOutLine_ID()))
								   .flatrateTermId(flatrateTermId)
								   .description(description)
								   .logEntryContractType(logEntryContractType)
								   .build());
	}
}
