/*
 * #%L
 * de.metas.business
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

package de.metas.invoice.filter;

import java.util.Set;

public interface IGenerateInvoiceCandidateForModelFilter
{
	/**
	 * @return the classnames - like e.g. `I_C_Order.class` - of the models with which this filter can be called. 
	 */
	Set<Class<?>> getSupportedTypes();

	/**
	 * @return true if an invoice candidate should be created for the given {@code model}. 
	 */
	boolean isEligible(Object model);
}
