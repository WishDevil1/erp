/*
 * #%L
 * de-metas-camel-alberta-camelroutes
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

package de.metas.camel.externalsystems.alberta.attachment;

import de.metas.common.externalsystem.JsonExternalSystemRequest;
import io.swagger.client.api.AttachmentApi;
import io.swagger.client.api.DocumentApi;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.Instant;

@Data
@Builder
public class GetAttachmentRouteContext
{
	@NonNull
	private String orgCode;

	@NonNull
	private final String apiKey;

	@NonNull
	private final DocumentApi documentApi;

	@NonNull
	private final AttachmentApi attachmentApi;

	@NonNull
	private final String createdAfter;

	@NonNull
	private Instant nextAttachmentImportStartDate;

	@NonNull
	private JsonExternalSystemRequest request;

	public void setNextAttachmentImportStartDate(@NonNull final Instant nextAttachmentImportStartDate)
	{
		if(nextAttachmentImportStartDate.compareTo(this.nextAttachmentImportStartDate) >= 0) {
			this.nextAttachmentImportStartDate = nextAttachmentImportStartDate;
		}
	}
}