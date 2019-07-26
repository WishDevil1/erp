package de.metas.rest_api.bpartner.impl;

import static de.metas.rest_api.bpartner.SwaggerDocConstants.BPARTER_IDENTIFIER_DOC;
import static de.metas.rest_api.bpartner.SwaggerDocConstants.CONTACT_IDENTIFIER_DOC;
import static de.metas.rest_api.bpartner.SwaggerDocConstants.LOCATION_IDENTIFIER_DOC;
import static de.metas.rest_api.bpartner.SwaggerDocConstants.NEXT_DOC;
import static de.metas.rest_api.bpartner.SwaggerDocConstants.SINCE_DOC;

import java.util.Optional;

import javax.annotation.Nullable;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.metas.Profiles;
import de.metas.bpartner.composite.BPartnerComposite;
import de.metas.rest_api.MetasfreshId;
import de.metas.rest_api.SyncAdvise;
import de.metas.rest_api.SyncAdvise.IfExists;
import de.metas.rest_api.SyncAdvise.IfNotExists;
import de.metas.rest_api.bpartner.BPartnerRestEndpoint;
import de.metas.rest_api.bpartner.impl.bpartnercomposite.JsonServiceFactory;
import de.metas.rest_api.bpartner.impl.bpartnercomposite.jsonpersister.JsonPersisterService;
import de.metas.rest_api.bpartner.request.JsonRequestBPartnerUpsert;
import de.metas.rest_api.bpartner.request.JsonRequestBPartnerUpsertItem;
import de.metas.rest_api.bpartner.request.JsonRequestContactUpsert;
import de.metas.rest_api.bpartner.request.JsonRequestLocationUpsert;
import de.metas.rest_api.bpartner.request.JsonResponseUpsert;
import de.metas.rest_api.bpartner.request.JsonResponseUpsert.JsonResponseUpsertBuilder;
import de.metas.rest_api.bpartner.request.JsonResponseUpsertItem;
import de.metas.rest_api.bpartner.response.JsonResponseComposite;
import de.metas.rest_api.bpartner.response.JsonResponseCompositeList;
import de.metas.rest_api.bpartner.response.JsonResponseContact;
import de.metas.rest_api.bpartner.response.JsonResponseLocation;
import de.metas.util.rest.MetasfreshRestAPIConstants;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.NonNull;

/*
 * #%L
 * de.metas.business.rest-api-impl
 * %%
 * Copyright (C) 2019 metas GmbH
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

@RequestMapping(MetasfreshRestAPIConstants.ENDPOINT_API + "/bpartner")
@RestController
@Profile(Profiles.PROFILE_App)
// the spelling "Bpartner" is to avoid swagger from spelling it "b-partner-rest.."
public class BpartnerRestController implements BPartnerRestEndpoint
{

	private final BPartnerEndpointService bPartnerEndpointservice;
	private final JsonServiceFactory jsonServiceFactory;

	public BpartnerRestController(@NonNull final BPartnerEndpointService bpIbPartnerEndpointservice,
			@NonNull final JsonServiceFactory jsonServiceFactory)
	{
		this.jsonServiceFactory = jsonServiceFactory;
		this.bPartnerEndpointservice = bpIbPartnerEndpointservice;

	}

	//
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully retrieved bpartner"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
	})
	@GetMapping("{bpartnerIdentifier}")
	@Override
	public ResponseEntity<JsonResponseComposite> retrieveBPartner(

			@ApiParam(required = true, value = BPARTER_IDENTIFIER_DOC) //
			@PathVariable("bpartnerIdentifier") //
			@NonNull final String bpartnerIdentifier)
	{
		final Optional<JsonResponseComposite> result = bPartnerEndpointservice.retrieveBPartner(bpartnerIdentifier);
		if (result.isPresent())
		{
			return ResponseEntity.ok(result.get());
		}
		return new ResponseEntity<JsonResponseComposite>(
				(JsonResponseComposite)null,
				HttpStatus.NOT_FOUND);
	}

	//
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully retrieved location"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
	})
	@GetMapping("{bpartnerIdentifier}/location/{locationIdentifier}")
	@Override
	public ResponseEntity<JsonResponseLocation> retrieveBPartnerLocation(

			@ApiParam(required = true, value = BPARTER_IDENTIFIER_DOC) //
			@PathVariable("bpartnerIdentifier") //
			@NonNull final String bpartnerIdentifier,

			@ApiParam(required = true, value = LOCATION_IDENTIFIER_DOC) //
			@PathVariable("locationIdentifier") //
			@NonNull final String locationIdentifier)
	{
		final Optional<JsonResponseLocation> location = bPartnerEndpointservice.retrieveBPartnerLocation(bpartnerIdentifier, locationIdentifier);
		if (location.isPresent())
		{
			return ResponseEntity.ok(location.get());
		}
		return new ResponseEntity<JsonResponseLocation>(
				(JsonResponseLocation)null,
				HttpStatus.NOT_FOUND);
	}

	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully retrieved contact"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
	})
	@GetMapping("{bpartnerIdentifier}/contact/{contactIdentifier}")
	@Override
	public ResponseEntity<JsonResponseContact> retrieveBPartnerContact(

			@ApiParam(required = true, value = BPARTER_IDENTIFIER_DOC) //
			@PathVariable("bpartnerIdentifier") //
			@NonNull final String bpartnerIdentifier,

			@ApiParam(required = true, value = CONTACT_IDENTIFIER_DOC) //
			@PathVariable("contactIdentifier") //
			@NonNull final String contactIdentifier)
	{
		final Optional<JsonResponseContact> contact = bPartnerEndpointservice.retrieveBPartnerContact(bpartnerIdentifier, contactIdentifier);
		if (contact.isPresent())
		{
			return ResponseEntity.ok(contact.get());
		}
		return new ResponseEntity<JsonResponseContact>(
				(JsonResponseContact)null,
				HttpStatus.NOT_FOUND);
	}

	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully retrieved bpartner(s)"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "There is no page for the given 'next' value")
	})
	@GetMapping
	@Override
	public ResponseEntity<JsonResponseCompositeList> retrieveBPartnersSince(

			@ApiParam(SINCE_DOC) //
			@RequestParam(name = "since", required = false) //
			@Nullable final Long epochTimestampMillis,

			@ApiParam(NEXT_DOC) //
			@RequestParam(name = "next", required = false) //
			@Nullable final String next)
	{
		final Optional<JsonResponseCompositeList> result = bPartnerEndpointservice.retrieveBPartnersSince(epochTimestampMillis, next);
		if (result.isPresent())
		{
			return ResponseEntity.ok(result.get());
		}
		return new ResponseEntity<JsonResponseCompositeList>(
				(JsonResponseCompositeList)null,
				HttpStatus.NOT_FOUND);
	}

	//
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Successfully created or updated bpartner(s)"),
			@ApiResponse(code = 401, message = "You are not authorized to create or update the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden")
	})
	@PutMapping
	@Override
	public ResponseEntity<JsonResponseUpsert> createOrUpdateBPartner(
			@RequestBody @NonNull final JsonRequestBPartnerUpsert bpartnerUpsertRequest)
	{
		final JsonPersisterService persister = jsonServiceFactory.createPersister();

		final SyncAdvise defaultSyncAdvise = bpartnerUpsertRequest.getSyncAdvise();

		final JsonResponseUpsertBuilder response = JsonResponseUpsert.builder();

		for (final JsonRequestBPartnerUpsertItem requestItem : bpartnerUpsertRequest.getRequestItems())
		{
			final BPartnerComposite syncToMetasfresh = persister.persist(
					requestItem.getBpartnerIdentifier(),
					requestItem.getBpartnerComposite(),
					defaultSyncAdvise);

			final MetasfreshId metasfreshId = MetasfreshId.of(syncToMetasfresh.getBpartner().getId());

			final JsonResponseUpsertItem responseItem = JsonResponseUpsertItem.builder()
					.identifier(requestItem.getBpartnerIdentifier())
					.metasfreshId(metasfreshId)
					.build();
			response.responseItem(responseItem);
		}
		return new ResponseEntity<>(response.build(), HttpStatus.CREATED);
	}

	//
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Successfully created or updated location"),
			@ApiResponse(code = 401, message = "You are not authorized to create or update the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden")
	})
	@ApiOperation("Create or update a locations for a particular bpartner. If a location exists, then its properties that are *not* specified are left untouched.")
	@PutMapping("{bpartnerIdentifier}/location")
	@Override
	public ResponseEntity<JsonResponseUpsert> createOrUpdateLocation(

			@ApiParam(required = true, value = BPARTER_IDENTIFIER_DOC) //
			@PathVariable("bpartnerIdentifier") //
			@NonNull final String bpartnerIdentifier,

			@RequestBody @NonNull final JsonRequestLocationUpsert jsonLocation)
	{
		final JsonPersisterService persister = jsonServiceFactory.createPersister();
		final Optional<JsonResponseUpsert> jsonLocationId = persister.persistForBPartner(
				bpartnerIdentifier,
				jsonLocation,
				SyncAdvise.builder().ifExists(IfExists.UPDATE_MERGE).ifNotExists(IfNotExists.CREATE).build());

		if (!jsonLocationId.isPresent())
		{
			return new ResponseEntity<JsonResponseUpsert>(
					(JsonResponseUpsert)null,
					HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<JsonResponseUpsert>(
				jsonLocationId.get(),
				HttpStatus.CREATED);
	}

	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Successfully created or updated contact"),
			@ApiResponse(code = 401, message = "You are not authorized to create or update the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The bpartner you were trying to reach is not found")
	})
	@ApiOperation("Create or update a contacts for a particular bpartner. If a contact exists, then its properties that are *not* specified are left untouched.")
	@PutMapping("{bpartnerIdentifier}/contact")
	@Override
	public ResponseEntity<JsonResponseUpsert> createOrUpdateContact(

			@ApiParam(required = true, value = BPARTER_IDENTIFIER_DOC) //
			@PathVariable("bpartnerIdentifier") //
			@NonNull final String bpartnerIdentifier,

			@RequestBody @NonNull final JsonRequestContactUpsert jsonContactUpsert)
	{
		final JsonPersisterService persister = jsonServiceFactory.createPersister();
		final Optional<JsonResponseUpsert> jsonContactId = persister.persistForBPartner(
				bpartnerIdentifier,
				jsonContactUpsert,
				SyncAdvise.CREATE_OR_MERGE);

		if (!jsonContactId.isPresent())
		{
			return new ResponseEntity<JsonResponseUpsert>(
					(JsonResponseUpsert)null,
					HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<JsonResponseUpsert>(
				jsonContactId.get(),
				HttpStatus.CREATED);
	}

}
