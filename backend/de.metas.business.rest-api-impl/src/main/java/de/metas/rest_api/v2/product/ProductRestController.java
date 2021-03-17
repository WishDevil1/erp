/*
 * #%L
 * de.metas.business.rest-api-impl
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

package de.metas.rest_api.v2.product;

import de.metas.Profiles;
import de.metas.common.product.v2.request.JsonRequestProductUpsert;
import de.metas.common.rest_api.SyncAdvise;
import de.metas.common.rest_api.v2.JsonResponseUpsert;
import de.metas.common.rest_api.v2.JsonResponseUpsertItem;
import de.metas.util.web.MetasfreshRestAPIConstants;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.NonNull;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping(MetasfreshRestAPIConstants.ENDPOINT_API_V2 + "/product")
@RestController
@Profile(Profiles.PROFILE_App)
public class ProductRestController
{
	private final ProductRestService productRestService;

	public ProductRestController(final ProductRestService productRestService)
	{
		this.productRestService = productRestService;
	}

	@ApiOperation("Create or update products and corresponding Bpartner-products.")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully created or updated product(s)"),
			@ApiResponse(code = 401, message = "You are not authorized to create or update the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 422, message = "The request entity could not be processed")
	})
	@PutMapping("{orgCode}")
	public ResponseEntity<JsonResponseUpsert> upsertProducts(@RequestBody @NonNull final JsonRequestProductUpsert request,
			@PathVariable("orgCode") @Nullable final String orgCode)
	{
		final SyncAdvise syncAdvise = request.getSyncAdvise();

		final List<JsonResponseUpsertItem> responseList =
				request.getRequestItems()
						.stream()
						.map(reqItem -> productRestService.createOrUpdateProduct(reqItem, syncAdvise, orgCode))
						.collect(Collectors.toList());

		return ResponseEntity.ok().body(JsonResponseUpsert.builder().responseItems(responseList).build());
	}

}
