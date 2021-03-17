package de.metas.ui.web.window.descriptor.factory;

import de.metas.logging.LogManager;
import de.metas.ui.web.window.datatypes.WindowId;
import de.metas.ui.web.window.descriptor.AdvancedSearchBPartnerProcessor;
import de.metas.ui.web.window.descriptor.AdvancedSearchDescriptor;
import de.metas.ui.web.window.descriptor.DocumentEntityDescriptor;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.I_C_BPartner;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;

/*
 * #%L
 * metasfresh-webui-api
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

@Component
public class AdvancedSearchDescriptorsProvider
{
	private static final Logger logger = LogManager.getLogger(AdvancedSearchDescriptorsProvider.class);
	@Autowired
	private DocumentDescriptorFactory documentDescriptors;

	private final ConcurrentHashMap<String, AdvancedSearchDescriptor> advSearchDescriptorsByTableName = new ConcurrentHashMap<>();

	AdvancedSearchDescriptorsProvider()
	{
		// FIXME: hardcoded AdvancedSearchDescriptor for C_BPartner_Adv_Search_v
		addAdvancedSearchDescriptor(AdvancedSearchDescriptor.of(I_C_BPartner.Table_Name, 541045, new AdvancedSearchBPartnerProcessor()));
	}

	public void addAdvancedSearchDescriptor(final AdvancedSearchDescriptor searchDescriptor)
	{
		advSearchDescriptorsByTableName.put(searchDescriptor.getTableName(), searchDescriptor);
	}

	public AdvancedSearchDescriptor getAdvancedSearchDescriptorOrNull(final String tableName)
	{
		return advSearchDescriptorsByTableName.get(tableName);
	}

	/**
	 * @param windowId the window ID of the Advanced Search window
	 * @return corresponding record descriptor from static map
	 */
	public AdvancedSearchDescriptor getAdvancedSearchDescriptor(final WindowId windowId)
	{
		final int windowIdValue = windowId.toInt();

		return advSearchDescriptorsByTableName.values()
				.stream()
				.filter(descriptor -> windowIdValue == descriptor.getWindowId())
				.findFirst()
				.orElseThrow(() -> new AdempiereException("No advanced search quick input defined windowId=" + windowId));
	}

	@Nullable
	public DocumentEntityDescriptor getAdvancedSearchDescriptorIfAvailable(final String tableName)
	{
		final AdvancedSearchDescriptor descriptor = getAdvancedSearchDescriptorOrNull(tableName);
		if (descriptor == null)
		{
			return null;
		}

		try
		{
			return documentDescriptors.getDocumentEntityDescriptor(descriptor.getWindowId());
		}
		catch (final Exception ex)
		{
			logger.warn("Failed fetching document entity descriptor for {}. Ignored", descriptor, ex);
			return null;
		}
	}
}
