/*
 * #%L
 * de-metas-camel-sap-file-import
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

package de.metas.camel.externalsystems.sap.creditlimit;

import de.metas.camel.externalsystems.common.v2.BPUpsertCamelRequest;
import de.metas.camel.externalsystems.sap.api.model.creditlimit.CreditLimitRow;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import javax.annotation.Nullable;

@Data
@Builder
public class CreditLimitContext
{
	@NonNull
	private String orgCode;

	@Nullable
	private SyncCreditLimitRequestBuilder creditLimitUpsertGroupBuilder;

	@Nullable
	private BPUpsertCamelRequest bpUpsertCamelRequest;

	public void initUpsertCreditLimitRequestBuilder(@NonNull final CreditLimitRow creditLimitRow)
	{
		this.creditLimitUpsertGroupBuilder = SyncCreditLimitRequestBuilder.of(creditLimitRow, orgCode);
	}
}
