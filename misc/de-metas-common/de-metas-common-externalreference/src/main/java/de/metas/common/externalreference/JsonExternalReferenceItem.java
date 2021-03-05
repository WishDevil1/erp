/*
 * #%L
 * de-metas-common-externalreference
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

package de.metas.common.externalreference;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.metas.common.rest_api.JsonMetasfreshId;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.Nullable;

@Value
public class JsonExternalReferenceItem
{
	public static JsonExternalReferenceItem of(
			@NonNull final JsonExternalReferenceLookupItem lookupItem,
			@NonNull final JsonMetasfreshId metasfreshId)
	{
		return new JsonExternalReferenceItem(lookupItem, metasfreshId, null);
	}

	public static JsonExternalReferenceItem of(
			@NonNull final JsonExternalReferenceLookupItem lookupItem)
	{
		return new JsonExternalReferenceItem(lookupItem, null, null);
	}

	@NonNull
	JsonExternalReferenceLookupItem lookupItem;

	@Nullable
	@JsonInclude(JsonInclude.Include.NON_NULL)
	JsonMetasfreshId metasfreshId;

	@Nullable
	@JsonInclude(JsonInclude.Include.NON_NULL)
	String version;

	@JsonCreator
	@Builder
	private JsonExternalReferenceItem(
			@JsonProperty("lookupItem") @NonNull final JsonExternalReferenceLookupItem lookupItem,
			@JsonProperty("metasfreshId") @Nullable final JsonMetasfreshId metasfreshId,
			@JsonProperty("version") @Nullable final String version)
	{
		this.lookupItem = lookupItem;
		this.metasfreshId = metasfreshId;
		this.version = version;
	}
}
