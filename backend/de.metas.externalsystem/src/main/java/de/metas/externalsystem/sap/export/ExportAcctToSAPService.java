/*
 * #%L
 * de.metas.externalsystem
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

package de.metas.externalsystem.sap.export;

import com.google.common.collect.ImmutableSet;
import de.metas.audit.data.repository.DataExportAuditLogRepository;
import de.metas.audit.data.repository.DataExportAuditRepository;
import de.metas.common.externalsystem.ExternalSystemConstants;
import de.metas.common.rest_api.common.JsonMetasfreshId;
import de.metas.document.DocTypeId;
import de.metas.externalsystem.ExternalSystemConfigRepo;
import de.metas.externalsystem.ExternalSystemConfigService;
import de.metas.externalsystem.ExternalSystemParentConfig;
import de.metas.externalsystem.ExternalSystemType;
import de.metas.externalsystem.IExternalSystemChildConfig;
import de.metas.externalsystem.IExternalSystemChildConfigId;
import de.metas.externalsystem.export.acct.ExportAcctToExternalSystemService;
import de.metas.externalsystem.rabbitmq.ExternalSystemMessageSender;
import de.metas.externalsystem.sap.ExternalSystemSAPConfig;
import de.metas.inout.IInOutBL;
import de.metas.inout.InOutId;
import de.metas.organization.IOrgDAO;
import de.metas.organization.OrgId;
import de.metas.process.IADPInstanceDAO;
import de.metas.process.IADProcessDAO;
import de.metas.process.PInstanceId;
import de.metas.util.Check;
import de.metas.util.Services;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.util.lang.IAutoCloseable;
import org.adempiere.util.lang.impl.TableRecordReference;
import org.compiere.model.I_M_InOut;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class ExportAcctToSAPService extends ExportAcctToExternalSystemService
{
	private static final String EXTERNAL_SYSTEM_COMMAND_EXPORT_ACCT_FACT = "exportAcctFact";

	private final IInOutBL inOutBL = Services.get(IInOutBL.class);
	private final IOrgDAO orgDAO = Services.get(IOrgDAO.class);
	private final IADProcessDAO processDAO = Services.get(IADProcessDAO.class);
	private final IADPInstanceDAO pInstanceDAO = Services.get(IADPInstanceDAO.class);

	private final TemporaryCache temporaryCache = new TemporaryCache();

	public ExportAcctToSAPService(
			@NonNull final DataExportAuditRepository dataExportAuditRepository,
			@NonNull final DataExportAuditLogRepository dataExportAuditLogRepository,
			@NonNull final ExternalSystemConfigRepo externalSystemConfigRepo,
			@NonNull final ExternalSystemMessageSender externalSystemMessageSender,
			@NonNull final ExternalSystemConfigService externalSystemConfigService)
	{
		super(dataExportAuditRepository,
			  dataExportAuditLogRepository,
			  externalSystemConfigRepo,
			  externalSystemMessageSender,
			  externalSystemConfigService);
	}

	@Override
	public void exportToExternalSystem(final @NonNull IExternalSystemChildConfigId externalSystemChildConfigId, final @NonNull TableRecordReference recordReference, @Nullable final PInstanceId pInstanceId)
	{
		try (final IAutoCloseable ignored = initCache(recordReference))
		{
			super.exportToExternalSystem(externalSystemChildConfigId, recordReference, pInstanceId);
		}
	}

	@Override
	public void exportToExternalSystemIfRequired(final @NonNull TableRecordReference recordToExportReference, @Nullable final Supplier<Optional<Set<IExternalSystemChildConfigId>>> additionalExternalSystemIdsProvider)
	{
		try (final IAutoCloseable ignored = initCache(recordToExportReference))
		{
			super.exportToExternalSystemIfRequired(recordToExportReference, additionalExternalSystemIdsProvider);
		}
	}

	@Override
	protected ExternalSystemType getExternalSystemType()
	{
		return ExternalSystemType.SAP;
	}

	@Override
	protected @NonNull Optional<Set<IExternalSystemChildConfigId>> getAdditionalExternalSystemConfigIds(@NonNull final TableRecordReference recordReference)
	{
		final DocTypeId docTypeId = getAcctDocumentInfo(recordReference).getDocTypeId();

		final ImmutableSet<IExternalSystemChildConfigId> configIds = externalSystemConfigRepo.getActiveByType(ExternalSystemType.SAP)
				.stream()
				.map(ExternalSystemParentConfig::getChildConfig)
				.map(ExternalSystemSAPConfig::cast)
				.filter(config -> config.isExportEnabledForDocType(docTypeId))
				.map(IExternalSystemChildConfig::getId)
				.collect(ImmutableSet.toImmutableSet());

		return Optional.of(configIds);
	}

	@Override
	protected @NonNull Map<String, String> buildParameters(final @NonNull TableRecordReference recordReference, final @NonNull ExternalSystemParentConfig config)
	{
		final ExternalSystemSAPConfig externalSystemSAPConfig = ExternalSystemSAPConfig.cast(config.getChildConfig());

		final Map<String, String> parameters = new HashMap<>();

		parameters.put(ExternalSystemConstants.PARAM_EXTERNAL_SYSTEM_HTTP_URL, Check.assumeNotNull(externalSystemSAPConfig.getBaseURL()
				, "BaseURL cannot be null at this stage! ExternalSystemConfig_SAP_ID=" + externalSystemSAPConfig.getId().getRepoId()));
		parameters.put(ExternalSystemConstants.PARAM_SAP_Signature, Check.assumeNotNull(externalSystemSAPConfig.getSignature()
				, "Signature cannot be null at this stage! ExternalSystemConfig_SAP_ID=" + externalSystemSAPConfig.getId().getRepoId()));
		parameters.put(ExternalSystemConstants.PARAM_SAP_SignedPermissions, Check.assumeNotNull(externalSystemSAPConfig.getSignedPermissions()
				, "SignedPermissions cannot be null at this stage! ExternalSystemConfig_SAP_ID=" + externalSystemSAPConfig.getId().getRepoId()));
		parameters.put(ExternalSystemConstants.PARAM_SAP_SignedVersion, Check.assumeNotNull(externalSystemSAPConfig.getSignedVersion()
				, "SignedVersion cannot be null at this stage! ExternalSystemConfig_SAP_ID=" + externalSystemSAPConfig.getId().getRepoId()));
		parameters.put(ExternalSystemConstants.PARAM_SAP_Post_Acct_Documents_Path, Check.assumeNotNull(externalSystemSAPConfig.getPostAcctDocumentsPath()
				, "PostAcctDocumentsPath cannot be null at this stage! ExternalSystemConfig_SAP_ID=" + externalSystemSAPConfig.getId().getRepoId()));
		parameters.put(ExternalSystemConstants.PARAM_SAP_ApiVersion, Check.assumeNotNull(externalSystemSAPConfig.getApiVersion()
				, "PostAcctDocumentsPath cannot be null at this stage! ExternalSystemConfig_SAP_ID=" + externalSystemSAPConfig.getId().getRepoId()));

		final AcctDocumentInfo acctDocumentInfo = getAcctDocumentInfo(recordReference);

		final SAPExportAcctConfig exportAcctConfig = externalSystemSAPConfig.getExportConfigFor(acctDocumentInfo.getDocTypeId())
				.orElseThrow(() -> new AdempiereException("No SAPExportAcctConfig found for C_DocType_ID=" + acctDocumentInfo.getDocTypeId())
						.appendParametersToMessage()
						.setParameter("TableName", recordReference.getTableName())
						.setParameter("Record_ID", recordReference.getRecord_ID()));

		parameters.put(ExternalSystemConstants.PARAM_PostGREST_AD_Process_Value, getProcessValue(exportAcctConfig));
		parameters.put(ExternalSystemConstants.PARAM_PostGREST_AD_Process_Param_Record_ID_NAME, acctDocumentInfo.getColumnName());
		parameters.put(ExternalSystemConstants.PARAM_PostGREST_AD_Process_Param_Record_ID_VALUE, String.valueOf(acctDocumentInfo.getRecordId()));

		parameters.put(ExternalSystemConstants.PARAM_CHILD_CONFIG_VALUE, externalSystemSAPConfig.getValue());

		return parameters;
	}

	@Override
	protected @NonNull String getExternalCommand()
	{
		return EXTERNAL_SYSTEM_COMMAND_EXPORT_ACCT_FACT;
	}

	@Override
	protected boolean isSyncAcctRecordsEnabled(final @NonNull TableRecordReference recordReference, final @NonNull ExternalSystemParentConfig config)
	{
		final ExternalSystemSAPConfig sapConfig = ExternalSystemSAPConfig.cast(config.getChildConfig());

		final AcctDocumentInfo documentInfo = getAcctDocumentInfo(recordReference);

		return sapConfig.isExportEnabledForDocType(documentInfo.getDocTypeId());
	}

	@Override
	@NonNull
	protected String getOrgCode(@NonNull final TableRecordReference recordReference)
	{
		final AcctDocumentInfo documentInfo = getAcctDocumentInfo(recordReference);

		return orgDAO.getById(documentInfo.getOrgId().getRepoId()).getValue();
	}

	@Nullable
	@Override
	protected JsonMetasfreshId createPInstanceId(final @NonNull TableRecordReference recordReference, final @NonNull ExternalSystemParentConfig config)
	{
		final ExternalSystemSAPConfig sapConfig = ExternalSystemSAPConfig.cast(config.getChildConfig());

		final DocTypeId docTypeId = getAcctDocumentInfo(recordReference).getDocTypeId();

		final SAPExportAcctConfig exportAcctConfig = sapConfig.getExportConfigFor(getAcctDocumentInfo(recordReference).getDocTypeId())
				.orElseThrow(() -> new AdempiereException("No SAPExportAcctConfig found for C_DocType_ID=" + docTypeId)
						.appendParametersToMessage()
						.setParameter("TableName", recordReference.getTableName())
						.setParameter("Record_ID", recordReference.getRecord_ID()));

		final int pInstanceId = pInstanceDAO.createAD_PInstance(exportAcctConfig.getProcessId()).getAD_PInstance_ID();

		return JsonMetasfreshId.of(pInstanceId);
	}

	@NonNull
	private AcctDocumentInfo loadDocumentInfo(@NonNull final TableRecordReference recordReference)
	{
		switch (recordReference.getTableName())
		{
			case I_M_InOut.Table_Name:
				final I_M_InOut inOut = inOutBL.getById(recordReference.getIdAssumingTableName(I_M_InOut.Table_Name, InOutId::ofRepoId));
				return AcctDocumentInfo.builder()
						.docTypeId(DocTypeId.ofRepoId(inOut.getC_DocType_ID()))
						.orgId(OrgId.ofRepoId(inOut.getAD_Org_ID()))
						.columnName(I_M_InOut.COLUMNNAME_M_InOut_ID)
						.recordId(inOut.getM_InOut_ID())
						.build();
			default:
				throw new AdempiereException("Unsupported TableRecordReference!")
						.appendParametersToMessage()
						.setParameter("TableName", recordReference.getTableName())
						.setParameter("Record_Id", recordReference.getRecord_ID());
		}
	}

	@NonNull
	private AcctDocumentInfo getAcctDocumentInfo(@NonNull final TableRecordReference recordReference)
	{
		return temporaryCache.get(recordReference).orElseGet(() -> loadDocumentInfo(recordReference));
	}

	@NonNull
	private IAutoCloseable initCache(@NonNull final TableRecordReference recordReference)
	{
		return temporaryCache.add(recordReference, loadDocumentInfo(recordReference));
	}

	@NonNull
	private String getProcessValue(@NonNull final SAPExportAcctConfig config)
	{
		return processDAO.getById(config.getProcessId()).getValue();
	}

	private static class TemporaryCache
	{
		private final ConcurrentHashMap<TableRecordReference, AcctDocumentInfo> recordReference2AcctDocumentInfo = new ConcurrentHashMap<>();

		@NonNull
		public Optional<AcctDocumentInfo> get(@NonNull final TableRecordReference recordReference)
		{
			return Optional.ofNullable(recordReference2AcctDocumentInfo.get(recordReference));
		}

		@NonNull
		public IAutoCloseable add(@NonNull final TableRecordReference tableRecordReference, @NonNull final AcctDocumentInfo documentInfo)
		{
			recordReference2AcctDocumentInfo.put(tableRecordReference, documentInfo);

			return () -> recordReference2AcctDocumentInfo.remove(tableRecordReference);
		}
	}

	@Value
	@Builder
	private static class AcctDocumentInfo
	{
		@NonNull
		String columnName;

		int recordId;

		@NonNull
		OrgId orgId;

		@NonNull
		DocTypeId docTypeId;
	}
}
