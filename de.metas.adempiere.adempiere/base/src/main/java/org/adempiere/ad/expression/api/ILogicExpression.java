package org.adempiere.ad.expression.api;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
 * %%
 * Copyright (C) 2015 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import java.util.List;

/**
 * Logic expression
 * 
 * NOTE: business logic expects that implementation of this interface to be immutable.
 * 
 * @author metas-dev <dev@metas-fresh.com>
 *
 */
public interface ILogicExpression extends IExpression<Boolean>
{
	ILogicExpression TRUE = new ConstantLogicExpression(true);
	ILogicExpression FALSE = new ConstantLogicExpression(false);

	@Override
	String getExpressionString();

	@Override
	String getFormatedExpressionString();

	@Override
	List<String> getParameters();

	@Override
	IExpressionEvaluator<ILogicExpression, Boolean> getEvaluator();

	/** Compose this logic expression with the given one, using logic AND and return it */
	ILogicExpression and(ILogicExpression expression);

	/** Compose this logic expression with the given one, using logic OR and return it */
	ILogicExpression or(ILogicExpression expression);
}
