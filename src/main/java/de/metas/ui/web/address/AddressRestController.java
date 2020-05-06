package de.metas.ui.web.address;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.metas.ui.web.address.json.JSONAddressLayout;
import de.metas.ui.web.address.json.JSONCreateAddressRequest;
import de.metas.ui.web.config.WebConfig;
import de.metas.ui.web.session.UserSession;
import de.metas.ui.web.window.controller.Execution;
import de.metas.ui.web.window.datatypes.LookupValue;
import de.metas.ui.web.window.datatypes.LookupValuesList;
import de.metas.ui.web.window.datatypes.json.JSONDocument;
import de.metas.ui.web.window.datatypes.json.JSONDocumentChangedEvent;
import de.metas.ui.web.window.datatypes.json.JSONDocumentLayoutOptions;
import de.metas.ui.web.window.datatypes.json.JSONDocumentOptions;
import de.metas.ui.web.window.datatypes.json.JSONLookupValue;
import de.metas.ui.web.window.datatypes.json.JSONLookupValuesList;
import de.metas.ui.web.window.model.Document;
import de.metas.ui.web.window.model.IDocumentChangesCollector;
import io.swagger.annotations.Api;

/*
 * #%L
 * metasfresh-webui-api
 * %%
 * Copyright (C) 2016 metas GmbH
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

@Api
@RestController
@RequestMapping(value = AddressRestController.ENDPOINT)
public class AddressRestController
{
	public static final String ENDPOINT = WebConfig.ENDPOINT_ROOT + "/address";

	@Autowired
	private UserSession userSession;

	@Autowired
	AddressRepository addressRepo;

	private JSONDocumentOptions newJsonDocumentOpts()
	{
		return JSONDocumentOptions.of(userSession);
	}

	private JSONDocumentLayoutOptions newJsonDocumentLayoutOpts()
	{
		return JSONDocumentLayoutOptions.of(userSession);
	}

	@RequestMapping(value = { "", "/" }, method = RequestMethod.POST)
	public JSONDocument createAddressDocument(@RequestBody final JSONCreateAddressRequest request)
	{
		userSession.assertLoggedIn();

		return Execution.callInNewExecution("createAddressDocument", () -> {
			final Document addressDoc = addressRepo.createNewFrom(request.getTemplateId());
			return JSONDocument.ofDocument(addressDoc, newJsonDocumentOpts());
		});
	}

	/**
	 * @param docId_NOTUSED not used but we need this parameter to be consistent with all other APIs that we have.
	 */
	@RequestMapping(value = "/{docId}/layout", method = RequestMethod.GET)
	public JSONAddressLayout getLayout(@PathVariable("docId") final int docId_NOTUSED)
	{
		userSession.assertLoggedIn();

		return JSONAddressLayout.of(addressRepo.getLayout(), newJsonDocumentLayoutOpts());
	}

	@RequestMapping(value = "/{docId}", method = RequestMethod.GET)
	public JSONDocument getAddressDocument(@PathVariable("docId") final int docId)
	{
		userSession.assertLoggedIn();

		final Document addressDoc = addressRepo.getAddressDocumentForReading(docId);
		return JSONDocument.ofDocument(addressDoc, newJsonDocumentOpts());
	}

	@RequestMapping(value = "/{docId}", method = RequestMethod.PATCH)
	public List<JSONDocument> processChanges(
			@PathVariable("docId") final int docId //
			, @RequestBody final List<JSONDocumentChangedEvent> events //
	)
	{
		userSession.assertLoggedIn();

		return Execution.callInNewExecution("processChanges", () -> {
			final IDocumentChangesCollector changesCollector = Execution.getCurrentDocumentChangesCollectorOrNull();
			addressRepo.processAddressDocumentChanges(docId, events, changesCollector);
			return JSONDocument.ofEvents(changesCollector, newJsonDocumentOpts());
		});
	}

	@RequestMapping(value = "/{docId}/field/{attributeName}/typeahead", method = RequestMethod.GET)
	public JSONLookupValuesList getAttributeTypeahead(
			@PathVariable("docId") final int docId //
			, @PathVariable("attributeName") final String attributeName //
			, @RequestParam(name = "query", required = true) final String query //
	)
	{
		userSession.assertLoggedIn();

		return addressRepo.getAddressDocumentForReading(docId)
				.getFieldLookupValuesForQuery(attributeName, query)
				.transform(this::toJSONLookupValuesList);
	}

	private JSONLookupValuesList toJSONLookupValuesList(final LookupValuesList lookupValuesList)
	{
		return JSONLookupValuesList.ofLookupValuesList(lookupValuesList, userSession.getAD_Language());
	}

	private JSONLookupValue toJSONLookupValue(final LookupValue lookupValue)
	{
		return JSONLookupValue.ofLookupValue(lookupValue, userSession.getAD_Language());
	}

	@RequestMapping(value = "/{docId}/field/{attributeName}/dropdown", method = RequestMethod.GET)
	public JSONLookupValuesList getAttributeDropdown(
			@PathVariable("docId") final int docId //
			, @PathVariable("attributeName") final String attributeName //
	)
	{
		userSession.assertLoggedIn();

		return addressRepo.getAddressDocumentForReading(docId)
				.getFieldLookupValues(attributeName)
				.transform(this::toJSONLookupValuesList);
	}

	@PostMapping(value = "/{docId}/complete")
	public JSONLookupValue complete(@PathVariable("docId") final int docId)
	{
		userSession.assertLoggedIn();

		return Execution.callInNewExecution("complete", () -> addressRepo
				.complete(docId)
				.transform(this::toJSONLookupValue));
	}
}
