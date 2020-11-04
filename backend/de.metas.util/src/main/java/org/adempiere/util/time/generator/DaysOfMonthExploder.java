package org.adempiere.util.time.generator;

/*
 * #%L
 * de.metas.util
 * %%
 * Copyright (C) 2015 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */


import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.adempiere.util.Check;

import de.metas.util.collections.ListUtils;

public class DaysOfMonthExploder implements IDateSequenceExploder
{
	public static final DaysOfMonthExploder LAST_DAY = new DaysOfMonthExploder(31);
	private final Set<Integer> days;

	public DaysOfMonthExploder(final Collection<Integer> days)
	{
		super();

		Check.assumeNotNull(days, "days not null");
		this.days = new HashSet<Integer>(days);
	}

	public DaysOfMonthExploder(final int... days)
	{
		this(ListUtils.asList(days));
	}

	@Override
	public Collection<Date> explode(final Date date)
	{
		final long dateMillis = date.getTime();

		final Set<Date> dates = new HashSet<Date>();
		for (final int day : days)
		{
			final Calendar cal = new GregorianCalendar();
			cal.setTime(date);
			setDayOfMonth(cal, day);

			// Skip all dates which are before our given date
			if (dateMillis > cal.getTimeInMillis())
			{
				continue;
			}

			final Date dayDate = cal.getTime();
			dates.add(dayDate);
		}

		return dates;
	}

	private final void setDayOfMonth(final Calendar calendar, final int dayOfMonth)
	{
		final int maxDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		final int dayOfMonthActual;
		if (dayOfMonth > maxDayOfMonth)
		{
			dayOfMonthActual = maxDayOfMonth;
		}
		else
		{
			dayOfMonthActual = dayOfMonth;
		}

		calendar.set(Calendar.DAY_OF_MONTH, dayOfMonthActual);
	}

}
