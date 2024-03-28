/*
 * #%L
 * de-metas-camel-pcm-file-import
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

package de.metas.camel.externalsystems.pcm.purchaseorder;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public interface ImportConstants
{
	String DEFAULT_UOM_X12DE355_CODE = "PCE";
	String DEFAULT_CURRENCY_CODE = "EUR";
	ZoneId EUROPE_BERLIN = ZoneId.of("Europe/Berlin");
	DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
}
