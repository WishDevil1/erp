package de.metas.monitoring.adapter;

import java.util.concurrent.Callable;

/*
 * #%L
 * de.metas.monitoring
 * %%
 * Copyright (C) 2020 metas GmbH
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

public class NoopPerformanceMonitoringService implements PerformanceMonitoringService
{

	@Override
	public <V> V monitorSpan(Callable<V> callable, SpanMetadata request)
	{
		try
		{
			return callable.call();
		}
		catch (Exception e)
		{
			throw PerformanceMonitoringServiceUtil.asRTE(e);
		}
	}

	@Override
	public <V> V monitorTransaction(Callable<V> callable, TransactionMetadata request)
	{
		try
		{
			return callable.call();
		}
		catch (Exception e)
		{
			throw PerformanceMonitoringServiceUtil.asRTE(e);
		}
	}

}
