/*
 * #%L
 * de-metas-common-product
 * %%
 * Copyright (C) 2024 metas GmbH
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

package de.metas.common.product.v2.request;

import de.metas.common.pricing.v2.productprice.JsonRequestProductPriceUpsertItem;
import de.metas.common.pricing.v2.productprice.TaxCategory;
import de.metas.common.rest_api.v2.SyncAdvise;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
public class JsonRequestProductTaxCategoryUpsert
{
	@ApiModelProperty(required = true)
	private TaxCategory taxCategory;

	@ApiModelProperty(required = false)
	private String countryCode;

	@ApiModelProperty(hidden = true)
	private boolean countryCodeSet;

	@ApiModelProperty(required = true)
	private Instant validFrom;

	@ApiModelProperty(hidden = true)
	private boolean validFromSet;
	public void setTaxCategory(final TaxCategory taxCategory)
	{
		this.taxCategory = taxCategory;
	}

	public void setValidFrom(final Instant validFrom)
	{
		this.validFrom = validFrom;
		this.validFromSet = true;
	}

	public void setCountryCode(final String countryCode)
	{
		this.countryCode = countryCode;
		this.countryCodeSet = true;
	}

}
