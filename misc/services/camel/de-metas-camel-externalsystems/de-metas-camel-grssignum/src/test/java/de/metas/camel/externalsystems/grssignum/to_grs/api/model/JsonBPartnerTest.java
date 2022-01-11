/*
 * #%L
 * de-metas-camel-grssignum
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

package de.metas.camel.externalsystems.grssignum.to_grs.api.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableList;
import de.metas.common.rest_api.common.JsonMetasfreshId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class JsonBPartnerTest
{
	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	@Test
	public void testDeserializeWithUnknownField() throws Exception
	{
		//given
		final String candidate = "{\n"
				+ "    \"FLAG\": 100,\n"
				+ "    \"MID\": \"HOM\",\n"
				+ "    \"DUMMYVALUE\": \"TEST\",\n"
				+ "    \"MKREDID\": \"102\",\n"
				+ "    \"KURZBEZEICHNUNG\": \"test company name\",\n"
				+ "    \"NAMENSZUSATZ\": \"test name2\",\n"
				+ "    \"ADRESSE 1\": \"test address1\",\n"
				+ "    \"ADRESSE 2\": \"test address2\",\n"
				+ "    \"ADRESSE 3\": \"test address3\",\n"
				+ "    \"ADRESSE 4\": \"test address4\",\n"
				+ "    \"PLZ\": \"test postal\",\n"
				+ "    \"ORT\": \"test city\",\n"
				+ "    \"LANDESCODE\": \"test country code\",\n"
				+ "    \"GLN\": \"test gln\",\n"
				+ "    \"INAKTIV\": 0,\n"
				+ "    \"KONTAKTE\":[\n "
				+ "        {\n"
				+ "   				 \"METASFRESHID\": 100,\n"
				+ "   				 \"NACHNAME\": \"LASTNAME\",\n"
				+ "   				 \"DUMMYVALUE\": \"TEST\",\n"
				+ "    				 \"VORNAME\": \"FIRSTNAME\",\n"
				+ "    				 \"ANREDE\": \"GREETING\",\n"
				+ "   				 \"TITEL\": \"TITLE\",\n"
				+ "    				 \"POSITION\": \"POSITION\",\n"
				+ "    				 \"EMAIL\": \"EMAIL\",\n"
				+ "   				 \"TELEFON\": \"TELEFON\",\n"
				+ "   				 \"MOBIL\": \"TELEFON2\",\n"
				+ "   				 \"FAX\": \"FAX\",\n"
				+ "    				 \"ROLLEN\":[\n "
				+ "       					 {\n"
				+ "									\"ROLLE\": \"ROLE1\"\n"
				+ "        					 },\n"
				+ "       					 {\n"
				+ "									\"ROLLE\": \"ROLE2\" \n"
				+ "       					 }\n"
				+ "    					]\n"
				+ "			}\n"
				+ "    	]\n"
				+ "}";

		//when
		final JsonBPartner partner = objectMapper.readValue(candidate, JsonBPartner.class);

		//then
		final JsonBPartnerContact contact = JsonBPartnerContactTest.createJsonBPartnerContact();

		final JsonBPartner expectedBPartner = JsonBPartner.builder()
				.flag(100)
				.id("102")
				.name("test company name")
				.name2("test name2")
				.address1("test address1")
				.address2("test address2")
				.address3("test address3")
				.address4("test address4")
				.postal("test postal")
				.city("test city")
				.countryCode("test country code")
				.gln("test gln")
				.inactive(0)
				.tenantId("HOM")
				.contact(contact)
				.build();

		assertThat(partner).isEqualTo(expectedBPartner);
	}

	@Test
	public void serialize_deserialize_test() throws Exception
	{
		//given
		final JsonBPartnerContactRole role1 = JsonBPartnerContactRole.builder()
				.role("ROLE1")
				.build();

		final JsonBPartnerContactRole role2 = JsonBPartnerContactRole.builder()
				.role("ROLE2")
				.build();
		final List<JsonBPartnerContactRole> roles = ImmutableList.of(role1, role2);

		final JsonBPartnerContact contact = JsonBPartnerContact.builder()
				.metasfreshId(JsonMetasfreshId.of(100))
				.lastName("LASTNAME")
				.firstName("FIRSTNAME")
				.greeting("GREETING")
				.title("TITLE")
				.position("POSITION")
				.email("EMAIL")
				.phone("TELEFON")
				.fax("FAX")
				.phone2("TELEFON2")
				.contactRoles(roles)
				.build();

		final JsonBPartner bPartner = JsonBPartner.builder()
				.flag(100)
				.id("102")
				.name("test company name")
				.name2("test name2")
				.address1("test address1")
				.address2("test address2")
				.address3("test address3")
				.address4("test address4")
				.postal("test postal")
				.city("test city")
				.countryCode("test country code")
				.gln("test gln")
				.inactive(0)
				.tenantId("HOM")
				.contact(contact)
				.build();

		//when
		final String serialized = objectMapper.writeValueAsString(bPartner);

		final JsonBPartner deserialized = objectMapper.readValue(serialized, JsonBPartner.class);

		//then
		assertThat(deserialized).isEqualTo(bPartner);
	}
}
