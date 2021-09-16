/*
 * #%L
 * de.metas.workflow.rest-api
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

package de.metas.workflow.rest_api.service;

import de.metas.user.UserId;
import de.metas.workflow.rest_api.model.WFActivity;
import de.metas.workflow.rest_api.model.WFProcess;
import de.metas.workflow.rest_api.model.WFProcessHeaderProperties;
import de.metas.workflow.rest_api.model.WFProcessId;
import de.metas.workflow.rest_api.model.WorkflowLauncher;
import de.metas.workflow.rest_api.model.WorkflowLauncherProviderId;
import lombok.NonNull;

import java.util.List;

public interface WorkflowLauncherProvider
{
	WorkflowLauncherProviderId getId();

	List<WorkflowLauncher> provideLaunchers(final UserId userId);

	WFProcess startWorkflow(WorkflowStartRequest request);

	WFProcess getWFProcessById(WFProcessId wfProcessId);

	default WFProcessHeaderProperties getHeaderProperties(@NonNull final WFProcess wfProcess, @NonNull final WFActivity wfActivity)
	{
		return WFProcessHeaderProperties.EMPTY;
	}
}
