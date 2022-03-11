/*
 * #%L
 * de.metas.cucumber
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

package de.metas.cucumber.stepdefs.externalreference;

import de.metas.common.util.CoalesceUtil;
import de.metas.cucumber.stepdefs.AD_User_StepDefData;
import de.metas.cucumber.stepdefs.DataTableUtil;
import de.metas.cucumber.stepdefs.M_Shipper_StepDefData;
import de.metas.externalreference.ExternalReferenceTypes;
import de.metas.externalreference.ExternalSystems;
import de.metas.externalreference.ExternalUserReferenceType;
import de.metas.externalreference.IExternalReferenceType;
import de.metas.externalreference.IExternalSystem;
import de.metas.externalreference.model.I_S_ExternalReference;
import de.metas.externalreference.shipper.ShipperExternalReferenceType;
import de.metas.util.Services;
import de.metas.util.web.exception.InvalidIdentifierException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import lombok.NonNull;
import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.compiere.SpringContextHolder;
import org.compiere.model.I_AD_User;
import org.compiere.model.I_M_Shipper;

import java.util.List;
import java.util.Map;

import static de.metas.cucumber.stepdefs.StepDefConstants.TABLECOLUMN_IDENTIFIER;
import static de.metas.externalreference.model.I_S_ExternalReference.COLUMNNAME_S_ExternalReference_ID;
import static org.adempiere.model.InterfaceWrapperHelper.newInstanceOutOfTrx;
import static org.assertj.core.api.Assertions.*;
import static org.compiere.model.I_AD_User.COLUMNNAME_AD_User_ID;
import static org.compiere.model.I_M_Shipper.COLUMNNAME_M_Shipper_ID;

public class S_ExternalReference_StepDef
{
	private final AD_User_StepDefData userTable;
	private final S_ExternalReference_StepDefData externalRefTable;
	private final M_Shipper_StepDefData shipperTable;

	final IQueryBL queryBL = Services.get(IQueryBL.class);

	private final ExternalReferenceTypes externalReferenceTypes;
	private final ExternalSystems externalSystems;

	public S_ExternalReference_StepDef(
			@NonNull final AD_User_StepDefData userTable,
			@NonNull final S_ExternalReference_StepDefData externalRefTable,
			@NonNull final M_Shipper_StepDefData shipperTable)
	{
		this.userTable = userTable;
		this.externalRefTable = externalRefTable;
		this.shipperTable = shipperTable;
		this.externalReferenceTypes = SpringContextHolder.instance.getBean(ExternalReferenceTypes.class);
		this.externalSystems = SpringContextHolder.instance.getBean(ExternalSystems.class);
	}

	@Then("verify that S_ExternalReference was created")
	public void verifyExists(@NonNull final DataTable dataTable)
	{
		final List<Map<String, String>> externalReferencesTableList = dataTable.asMaps();
		for (final Map<String, String> dataTableRow : externalReferencesTableList)
		{
			final String externalSystem = DataTableUtil.extractStringOrNullForColumnName(dataTableRow, "ExternalSystem");
			final String type = DataTableUtil.extractStringOrNullForColumnName(dataTableRow, "Type");
			final String externalReference = DataTableUtil.extractStringOrNullForColumnName(dataTableRow, "ExternalReference");
			final String externalReferenceURL = DataTableUtil.extractStringOrNullForColumnName(dataTableRow, "ExternalReferenceURL");

			final boolean externalRefExists = queryBL.createQueryBuilder(I_S_ExternalReference.class)
					.addEqualsFilter(I_S_ExternalReference.COLUMNNAME_ExternalSystem, externalSystem)
					.addEqualsFilter(I_S_ExternalReference.COLUMNNAME_Type, type)
					.addEqualsFilter(I_S_ExternalReference.COLUMNNAME_ExternalReference, externalReference)
					.addEqualsFilter(I_S_ExternalReference.COLUMN_ExternalReferenceURL, externalReferenceURL)
					.create()
					.anyMatch();

			assertThat(externalRefExists).isTrue();
		}
	}

	@And("metasfresh contains S_ExternalReference:")
	public void add_S_ExternalReference(@NonNull final DataTable dataTable)
	{
		for (final Map<String, String> row : dataTable.asMaps())
		{
			final String externalSystemCode = DataTableUtil.extractStringForColumnName(row, I_S_ExternalReference.COLUMNNAME_ExternalSystem);
			final IExternalSystem externalSystemType = externalSystems.ofCode(externalSystemCode)
					.orElseThrow(() -> new AdempiereException("Unknown externalSystemCode" + externalSystemCode));

			final String typeCode = DataTableUtil.extractStringForColumnName(row, I_S_ExternalReference.COLUMNNAME_Type);
			final IExternalReferenceType type = externalReferenceTypes.ofCode(typeCode)
					.orElseThrow(() -> new InvalidIdentifierException("type", typeCode));

			final String externalReference = DataTableUtil.extractStringForColumnName(row, I_S_ExternalReference.COLUMNNAME_ExternalReference);

			final I_S_ExternalReference externalReferenceRecord = CoalesceUtil.coalesceSuppliers(
					() -> queryBL.createQueryBuilder(I_S_ExternalReference.class)
							.addEqualsFilter(I_S_ExternalReference.COLUMNNAME_ExternalReference, externalReference)
							.addEqualsFilter(I_S_ExternalReference.COLUMN_Type, type.getCode())
							.addEqualsFilter(I_S_ExternalReference.COLUMNNAME_ExternalSystem, externalSystemType.getCode())
							.create()
							.firstOnlyOrNull(I_S_ExternalReference.class),
					() -> newInstanceOutOfTrx(I_S_ExternalReference.class));

			assertThat(externalReferenceRecord).isNotNull();

			externalReferenceRecord.setExternalSystem(externalSystemType.getCode());
			externalReferenceRecord.setType(type.getCode());
			externalReferenceRecord.setExternalReference(externalReference);

			if (type.getCode().equals(ExternalUserReferenceType.USER_ID.getCode()))
			{
				final String userIdentifier = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + COLUMNNAME_AD_User_ID + "." + TABLECOLUMN_IDENTIFIER);
				assertThat(userIdentifier).isNotNull();

				final I_AD_User user = userTable.get(userIdentifier);
				assertThat(user).isNotNull();

				externalReferenceRecord.setRecord_ID(user.getAD_User_ID());
			}
			else if (type.getCode().equals(ShipperExternalReferenceType.SHIPPER.getCode()))
			{
				final String shipperIdentifier = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + COLUMNNAME_M_Shipper_ID + "." + TABLECOLUMN_IDENTIFIER);
				assertThat(shipperIdentifier).isNotNull();

				final I_M_Shipper shipper = shipperTable.get(shipperIdentifier);
				assertThat(shipper).isNotNull();

				externalReferenceRecord.setRecord_ID(shipper.getM_Shipper_ID());
			}
			else
			{
				throw new AdempiereException("Unknown X_S_ExternalReference.Type! type:" + typeCode);
			}

			InterfaceWrapperHelper.saveRecord(externalReferenceRecord);

			final String externalReferenceIdentifier = DataTableUtil.extractStringForColumnName(row, COLUMNNAME_S_ExternalReference_ID + "." + TABLECOLUMN_IDENTIFIER);
			externalRefTable.put(externalReferenceIdentifier, externalReferenceRecord);
		}
	}
}