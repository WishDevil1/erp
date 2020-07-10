package de.metas.ui.web.document.references;

import java.time.Duration;

import javax.annotation.Nullable;

import de.metas.i18n.ITranslatableString;
import de.metas.ui.web.document.filter.DocumentFilter;
import de.metas.util.lang.Priority;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

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

@Value
public final class DocumentReference
{
	private final DocumentReferenceId id;
	private final String internalName;
	private final ITranslatableString caption;
	private final DocumentReferenceTargetWindow targetWindow;
	private final Priority priority;
	private final int documentsCount;
	private final DocumentFilter filter;
	private final Duration loadDuration;

	@Builder
	private DocumentReference(
			@NonNull final DocumentReferenceId id,
			@Nullable final String internalName,
			@NonNull final ITranslatableString caption,
			@NonNull final DocumentReferenceTargetWindow targetWindow,
			@NonNull final Priority priority,
			final int documentsCount,
			@NonNull final DocumentFilter filter,
			@Nullable final Duration loadDuration)
	{
		this.id = id;
		this.internalName = internalName;
		this.caption = caption;
		this.targetWindow = targetWindow;
		this.priority = priority;
		this.documentsCount = documentsCount;
		this.filter = filter;
		this.loadDuration = loadDuration;
	}

	public String getCaption(final String adLanguage)
	{
		return caption.translate(adLanguage);
	}
}
