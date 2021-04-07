/*
 * #%L
 * de.metas.adempiere.adempiere.base
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

package de.metas.document.references.related_documents;

import org.adempiere.ad.element.api.AdWindowId;

import javax.annotation.Nullable;
import java.util.List;

/**
 *
 * @author Tobias Schoeneberg, www.metas.de - FR [ 2897194 ] Advanced Zoom and RelationTypes
 */
public interface IZoomProvider
{
	/**
	 *
	 * @param source the source we need zoom targets for
	 * @param targetAD_Window_ID optional target window ID; if specified, only those {@link ZoomInfo}s will be returned which have this targetAD_Window_ID.
	 * @return a list of zoom targets. The {@link ZoomInfo#getRecordCount()} of the ZoomInfo's query member might be zero.
	 */
	List<ZoomInfoCandidate> retrieveZoomInfos(IZoomSource source, @Nullable final AdWindowId targetAD_Window_ID);
}
