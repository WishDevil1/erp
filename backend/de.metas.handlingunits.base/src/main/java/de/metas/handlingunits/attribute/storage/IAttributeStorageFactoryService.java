package de.metas.handlingunits.attribute.storage;

/*
 * #%L
 * de.metas.handlingunits.base
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import org.adempiere.util.ISingletonService;

import de.metas.handlingunits.attribute.IHUAttributesDAO;
import de.metas.handlingunits.storage.IHUStorageFactory;

public interface IAttributeStorageFactoryService extends ISingletonService
{
	/** Create a "standard" attribute storage factory, ready to use. */
	IAttributeStorageFactory createHUAttributeStorageFactory();
	
	/**
	 * Calls {@link #createHUAttributeStorageFactory(IHUAttributesDAO)} with the default {@link IHUAttributesDAO} implementation (no decoupled or on-commit saves).
	 * <p>
	 */
	IAttributeStorageFactory createHUAttributeStorageFactory(IHUStorageFactory huStorageFactory);

	IAttributeStorageFactory createHUAttributeStorageFactory(
			IHUStorageFactory huStorageFactory,
			IHUAttributesDAO huAttributesDAO);

	/**
	 * Creates a {@link IAttributeStorageFactory} which still lacks a {@link IHUStorageFactory}.<br>
	 * The returned factory is not usable unless that huStorage factory was also set via {@link IAttributeStorageFactory#setHUStorageFactory(IHUStorageFactory)}.
	 */
	IAttributeStorageFactory prepareHUAttributeStorageFactory(IHUAttributesDAO huAttributesDAO);

	void addAttributeStorageFactory(Class<? extends IAttributeStorageFactory> attributeStorageFactoryClass);
}
