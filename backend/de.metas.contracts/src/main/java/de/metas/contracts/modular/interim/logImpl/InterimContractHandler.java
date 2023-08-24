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

package de.metas.contracts.modular.interim.logImpl;

import de.metas.contracts.FlatrateTermId;
import de.metas.contracts.flatrate.TypeConditions;
import de.metas.contracts.model.I_C_Flatrate_Term;
import de.metas.contracts.modular.IModularContractTypeHandler;
import de.metas.contracts.modular.ModularContractService;
import de.metas.contracts.modular.ModularContract_Constants;
import de.metas.contracts.modular.log.LogEntryContractType;
import de.metas.contracts.modular.log.LogEntryCreateRequest;
import de.metas.contracts.modular.log.LogEntryDocumentType;
import de.metas.contracts.modular.log.LogEntryReverseRequest;
import de.metas.contracts.modular.log.ModularContractLogEntry;
import de.metas.contracts.modular.log.ModularContractLogService;
import de.metas.contracts.modular.settings.ModularContractSettings;
import de.metas.contracts.modular.settings.ModularContractSettingsDAO;
import de.metas.contracts.modular.settings.ModularContractTypeId;
import de.metas.i18n.AdMessageKey;
import de.metas.i18n.Language;
import de.metas.i18n.TranslatableStrings;
import de.metas.lang.SOTrx;
import de.metas.order.OrderLineId;
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
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.util.lang.impl.TableRecordReference;
import org.compiere.model.I_C_UOM;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

@Component
public class InterimContractHandler implements IModularContractTypeHandler<I_C_Flatrate_Term>
{
	private final IOrgDAO orgDAO = Services.get(IOrgDAO.class);
	private final ModularContractLogService modularContractLogService;
	private final ModularContractSettingsDAO modularContractSettingsDAO;

	private final IProductBL productBL = Services.get(IProductBL.class);
	private final IUOMDAO uomDAO = Services.get(IUOMDAO.class);

	private final static AdMessageKey MSG_ON_REVERSE_DESCRIPTION = AdMessageKey.of("de.metas.contracts.modular.interimContractReverseLogDescription");
	private final static AdMessageKey MSG_ON_COMPLETE_DESCRIPTION = AdMessageKey.of("de.metas.contracts.modular.interimContractCompleteLogDescription");

	public InterimContractHandler(
			@NonNull final ModularContractLogService modularContractLogService,
			@NonNull final ModularContractSettingsDAO modularContractSettingsDAO)
	{
		this.modularContractLogService = modularContractLogService;
		this.modularContractSettingsDAO = modularContractSettingsDAO;
	}

	@Override
	public @NonNull Class<I_C_Flatrate_Term> getType()
	{
		return I_C_Flatrate_Term.class;
	}

	@Override
	public boolean applies(final @NonNull I_C_Flatrate_Term flatrateTermRecord)
	{
		return TypeConditions.ofCode(flatrateTermRecord.getType_Conditions()).isInterimContractType();
	}

	@Override
	public boolean applies(final @NonNull LogEntryContractType logEntryContractType)
	{
		return logEntryContractType.isInterimContractType();
	}

	@Override
	public @NonNull Optional<LogEntryCreateRequest> createLogEntryCreateRequest(final @NonNull I_C_Flatrate_Term flatrateTermRecord, final @NonNull FlatrateTermId flatrateTermId)
	{
		final FlatrateTermId modularContractId = FlatrateTermId.ofRepoId(flatrateTermRecord.getModular_Flatrate_Term_ID());
		final Optional<ModularContractLogEntry> modularContractLogEntryOptional = modularContractLogService.getLastModularContractLog(modularContractId, OrderLineId.ofRepoId(flatrateTermRecord.getC_OrderLine_Term_ID()));
		if (modularContractLogEntryOptional.isEmpty())
		{
			return Optional.empty();
		}
		final ModularContractLogEntry modularContractLogEntry = modularContractLogEntryOptional.get();

		final ModularContractSettings modularContractSettings = modularContractSettingsDAO.getByFlatrateTermIdOrNull(flatrateTermId);
		if (modularContractSettings == null)
		{
			return Optional.empty();
		}

		final Optional<ModularContractTypeId> modularContractTypeId = modularContractSettings.getModularContractTypeId(InterimContractHandler.class);

		final String productName = productBL.getProductValueAndName(modularContractLogEntry.getProductId());
		final String description = TranslatableStrings.adMessage(MSG_ON_COMPLETE_DESCRIPTION, productName, modularContractLogEntry.getQuantity())
				.translate(Language.getBaseAD_Language());

		return modularContractTypeId.map(contractTypeId -> LogEntryCreateRequest.builder()
				.contractId(flatrateTermId)
				.productId(ProductId.ofRepoId(flatrateTermRecord.getM_Product_ID()))
				.referencedRecord(TableRecordReference.of(I_C_Flatrate_Term.Table_Name, flatrateTermId))
				.producerBPartnerId(modularContractLogEntry.getProducerBPartnerId())
				.invoicingBPartnerId(modularContractLogEntry.getInvoicingBPartnerId())
				.collectionPointBPartnerId(modularContractLogEntry.getCollectionPointBPartnerId())
				.warehouseId(modularContractLogEntry.getWarehouseId())
				.documentType(LogEntryDocumentType.CONTRACT_PREFINANCING)
				.contractType(LogEntryContractType.INTERIM)
				.soTrx(SOTrx.PURCHASE)
				.processed(false)
				.quantity(modularContractLogEntry.getQuantity())
				.amount(modularContractLogEntry.getAmount())
				.transactionDate(LocalDateAndOrgId.ofTimestamp(flatrateTermRecord.getStartDate(),
															   OrgId.ofRepoId(flatrateTermRecord.getAD_Org_ID()),
															   orgDAO::getTimeZone))
				.year(modularContractLogEntry.getYear())
				.description(description)
				.modularContractTypeId(contractTypeId)
				.build());

	}

	@Override
	public @NonNull Optional<LogEntryReverseRequest> createLogEntryReverseRequest(final @NonNull I_C_Flatrate_Term flatrateTermRecord, final @NonNull FlatrateTermId flatrateTermId)
	{
		final I_C_UOM uomId = uomDAO.getById(UomId.ofRepoId(flatrateTermRecord.getC_UOM_ID()));
		final Quantity quantity = Quantity.of(flatrateTermRecord.getQtyPlanned_NextYear(), uomId);

		final ProductId productId = ProductId.ofRepoId(flatrateTermRecord.getM_Product_ID());
		final String productName = productBL.getProductValueAndName(productId);
		final String description = TranslatableStrings.adMessage(MSG_ON_REVERSE_DESCRIPTION, productName, quantity)
				.translate(Language.getBaseAD_Language());

		return Optional.of(LogEntryReverseRequest.builder()
								   .referencedModel(TableRecordReference.of(I_C_Flatrate_Term.Table_Name, flatrateTermRecord.getC_Flatrate_Term_ID()))
								   .flatrateTermId(flatrateTermId)
								   .description(description)
								   .logEntryContractType(LogEntryContractType.INTERIM)
								   .build());
	}

	@Override
	public @NonNull Stream<FlatrateTermId> streamContractIds(@NonNull final I_C_Flatrate_Term flatrateTermRecord)
	{
		return Stream.ofNullable(FlatrateTermId.ofRepoIdOrNull(flatrateTermRecord.getC_Flatrate_Term_ID()));
	}

	@Override
	public void validateDocAction(final @NonNull I_C_Flatrate_Term model, final ModularContractService.@NonNull ModelAction action)
	{
		if (action != ModularContractService.ModelAction.COMPLETED && action != ModularContractService.ModelAction.CANCELED)
		{
			throw new AdempiereException(ModularContract_Constants.MSG_ERROR_DOC_ACTION_UNSUPPORTED);
		}
	}
}
