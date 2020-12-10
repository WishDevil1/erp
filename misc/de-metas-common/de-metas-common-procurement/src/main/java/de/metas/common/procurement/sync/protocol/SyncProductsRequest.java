/*
 * #%L
 * de-metas-common-procurement
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

package de.metas.common.procurement.sync.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
public class SyncProductsRequest extends ProcurementEvent
{
	public static SyncProductsRequest of(@NonNull final SyncProduct syncProduct)
	{
		return SyncProductsRequest.builder().product(syncProduct).build();
	}

	public static SyncProductsRequest of(@NonNull final List<SyncProduct> syncProducts)
	{
		return SyncProductsRequest.builder().products(syncProducts).build();
	}
	
	List<SyncProduct> products;

	@Builder
	@JsonCreator
	private SyncProductsRequest(
			@JsonProperty("products") @Singular final List<SyncProduct> products)
	{
		this.products = products;
	}

	@Override
	public String toString()
	{
		return "SyncProductsRequest [products=" + products + "]";
	}
}
