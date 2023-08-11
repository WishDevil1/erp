/*
 * #%L
 * de.metas.adempiere.adempiere.base
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

package de.metas.workflow.service.impl;

import de.metas.copy_with_details.template.CopyTemplateCustomizer;
import de.metas.util.InSetPredicate;
import lombok.NonNull;
import org.compiere.model.I_AD_WF_Node;
import org.compiere.model.I_AD_Workflow;
import org.compiere.model.POInfo;
import org.compiere.model.copy.ValueToCopy;
import org.eevolution.model.I_PP_WF_Node_Product;
import org.springframework.stereotype.Component;

@Component
public class AD_WF_Node_CopyTemplateCustomizer implements CopyTemplateCustomizer
{
	@Override
	public String getTableName()
	{
		return I_AD_WF_Node.Table_Name;
	}

	@Override
	public ValueToCopy extractValueToCopy(final POInfo poInfo, final String columnName)
	{
		return I_AD_Workflow.COLUMNNAME_Value.equals(columnName) ? ValueToCopy.DIRECT_COPY : ValueToCopy.NOT_SPECIFIED;
	}

	@Override
	public @NonNull InSetPredicate<String> getChildTableNames()
	{
		return InSetPredicate.only(I_PP_WF_Node_Product.Table_Name);
	}
}