/*
 * #%L
 * de.metas.adempiere.adempiere.base
 * %%
 * Copyright (C) 2022 metas GmbH
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

package de.metas.resource;

import de.metas.calendar.util.CalendarDateRange;
import de.metas.product.ResourceId;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.Nullable;

@Value
public class ResourceAssignment
{
	@NonNull ResourceAssignmentId id;
	@NonNull ResourceId resourceId;

	@NonNull String name;
	@Nullable String description;

	@NonNull CalendarDateRange dateRange;

	@Builder(toBuilder = true)
	private ResourceAssignment(
			@NonNull final ResourceAssignmentId id,
			@NonNull final ResourceId resourceId,
			@NonNull final String name,
			@Nullable final String description,
			@NonNull CalendarDateRange dateRange)
	{
		this.id = id;
		this.resourceId = resourceId;
		this.name = name;
		this.description = description;
		this.dateRange = dateRange;
	}
}
