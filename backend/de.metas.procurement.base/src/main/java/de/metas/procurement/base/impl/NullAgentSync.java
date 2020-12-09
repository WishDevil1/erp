package de.metas.procurement.base.impl;

import de.metas.common.procurement.sync.IAgentSync;
import de.metas.common.procurement.sync.protocol.SyncBPartnersRequest;
import de.metas.common.procurement.sync.protocol.SyncConfirmationRequest;
import de.metas.common.procurement.sync.protocol.SyncInfoMessageRequest;
import de.metas.common.procurement.sync.protocol.SyncProductsRequest;
import de.metas.common.procurement.sync.protocol.SyncRfQCloseEventsRequest;
import de.metas.common.procurement.sync.protocol.SyncRfQsRequest;
import org.adempiere.util.lang.ObjectUtils;

/*
 * #%L
 * de.metas.procurement.base
 * %%
 * Copyright (C) 2016 metas GmbH
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

/**
 * An mocked implementation which only prints the requests to System.out.
 *
 * @author metas-dev <dev@metasfresh.com>
 *
 */
public class NullAgentSync implements IAgentSync
{
	public static final transient NullAgentSync instance = new NullAgentSync();

	private NullAgentSync()
	{
		super();
	}

	@Override
	public void syncBPartners(final SyncBPartnersRequest request)
	{
		System.out.println("syncBPartners: " + ObjectUtils.toString(request));
	}

	@Override
	public void syncProducts(final SyncProductsRequest request)
	{
		System.out.println("syncProducts: " + ObjectUtils.toString(request));
	}

	@Override
	public void syncInfoMessage(final SyncInfoMessageRequest request)
	{
		System.out.println("syncInfoMessage: " + ObjectUtils.toString(request));
	}

	@Override
	public void confirm(final SyncConfirmationRequest syncConfirmations)
	{
		System.out.println("confirm: " + ObjectUtils.toString(syncConfirmations));
	}

	@Override
	public void syncRfQs(final SyncRfQsRequest syncRfQsRequest)
	{
		System.out.println("syncRfQs: " + ObjectUtils.toString(syncRfQsRequest));
	}

	@Override
	public void closeRfQs(SyncRfQCloseEventsRequest syncRfQCloseEventsRequest)
	{
		System.out.println("closeRfQs: " + ObjectUtils.toString(syncRfQCloseEventsRequest));
	}
}
