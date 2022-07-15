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

package de.metas.project.workorder.calendar;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import de.metas.calendar.CalendarEntry;
import de.metas.calendar.CalendarEntryAddRequest;
import de.metas.calendar.CalendarEntryId;
import de.metas.calendar.CalendarEntryUpdateRequest;
import de.metas.calendar.CalendarEntryUpdateResult;
import de.metas.calendar.CalendarGlobalId;
import de.metas.calendar.CalendarQuery;
import de.metas.calendar.CalendarRef;
import de.metas.calendar.CalendarResourceId;
import de.metas.calendar.CalendarResourceRef;
import de.metas.calendar.CalendarService;
import de.metas.calendar.CalendarServiceId;
import de.metas.calendar.simulation.SimulationPlanId;
import de.metas.common.util.CoalesceUtil;
import de.metas.i18n.TranslatableStrings;
import de.metas.logging.LogManager;
import de.metas.project.ProjectId;
import de.metas.project.budget.BudgetProject;
import de.metas.project.budget.BudgetProjectAndResourceId;
import de.metas.project.budget.BudgetProjectResource;
import de.metas.project.budget.BudgetProjectResourceSimulation;
import de.metas.project.budget.BudgetProjectResources;
import de.metas.project.budget.BudgetProjectService;
import de.metas.project.budget.BudgetProjectSimulationPlan;
import de.metas.project.budget.BudgetProjectSimulationRepository;
import de.metas.project.workorder.WOProject;
import de.metas.project.workorder.WOProjectAndResourceId;
import de.metas.project.workorder.WOProjectResource;
import de.metas.project.workorder.WOProjectResourceId;
import de.metas.project.workorder.WOProjectResources;
import de.metas.project.workorder.WOProjectResourcesCollection;
import de.metas.project.workorder.WOProjectService;
import de.metas.project.workorder.WOProjectStep;
import de.metas.project.workorder.WOProjectStepId;
import de.metas.project.workorder.WOProjectSteps;
import de.metas.project.workorder.conflicts.WOProjectConflictService;
import de.metas.resource.Resource;
import de.metas.resource.ResourceGroup;
import de.metas.resource.ResourceService;
import de.metas.uom.IUOMDAO;
import de.metas.user.UserId;
import de.metas.util.Check;
import de.metas.util.Services;
import de.metas.util.time.DurationUtils;
import lombok.NonNull;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.util.lang.OldAndNewValues;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

@Component
public class WOProjectCalendarService implements CalendarService
{
	private static final Logger logger = LogManager.getLogger(WOProjectCalendarService.class);
	private static final CalendarServiceId ID = CalendarServiceId.ofString("WOProject");

	static final CalendarGlobalId CALENDAR_ID = CalendarGlobalId.of(ID, "default");

	private final IUOMDAO uomDAO = Services.get(IUOMDAO.class);
	private final ResourceService resourceService;
	private final BudgetProjectService budgetProjectService;
	private final BudgetProjectSimulationRepository budgetProjectSimulationRepository;
	private final WOProjectService woProjectService;
	private final WOProjectSimulationService woProjectSimulationService;
	private final WOProjectConflictService woProjectConflictService;

	public WOProjectCalendarService(
			final ResourceService resourceService,
			final WOProjectService woProjectService,
			final BudgetProjectService budgetProjectService,
			final BudgetProjectSimulationRepository budgetProjectSimulationRepository,
			final WOProjectSimulationService woProjectSimulationService,
			final WOProjectConflictService woProjectConflictService)
	{
		this.resourceService = resourceService;
		this.budgetProjectService = budgetProjectService;
		this.budgetProjectSimulationRepository = budgetProjectSimulationRepository;
		this.woProjectService = woProjectService;
		this.woProjectSimulationService = woProjectSimulationService;
		this.woProjectConflictService = woProjectConflictService;
	}

	@Override
	public CalendarServiceId getCalendarServiceId()
	{
		return ID;
	}

	@Override
	public Stream<CalendarRef> streamAvailableCalendars(final UserId userId)
	{
		final CalendarRef calendar = CalendarRef.builder()
				.calendarId(CALENDAR_ID)
				.name(TranslatableStrings.adElementOrMessage("S_Resource_ID"))
				.resources(getAllResourcesAndGroups())
				.build();

		return Stream.of(calendar);
	}

	private ImmutableSet<CalendarResourceRef> getAllResourcesAndGroups()
	{
		final HashSet<CalendarResourceRef> result = new HashSet<>();
		for (final Resource resource : resourceService.getAllActiveResources())
		{
			result.add(toCalendarResourceRef(resource));
		}

		for (final ResourceGroup resourceGroup : resourceService.getAllActiveGroups())
		{
			result.add(toCalendarResourceRef(resourceGroup));
		}

		return ImmutableSet.copyOf(result);
	}

	private static CalendarResourceRef toCalendarResourceRef(final Resource resource)
	{
		return CalendarResourceRef.builder()
				.calendarResourceId(CalendarResourceId.ofRepoId(resource.getResourceId()))
				.name(resource.getName())
				.parentId(CalendarResourceId.ofNullableRepoId(resource.getResourceGroupId()))
				.build();
	}

	private static CalendarResourceRef toCalendarResourceRef(final ResourceGroup resourceGroup)
	{
		return CalendarResourceRef.builder()
				.calendarResourceId(CalendarResourceId.ofRepoId(resourceGroup.getId()))
				.name(resourceGroup.getName())
				.build();
	}

	@Override
	public Stream<CalendarEntry> query(final CalendarQuery calendarQuery)
	{
		if (!calendarQuery.isMatchingCalendarServiceId(ID))
		{
			return Stream.empty();
		}
		if (!calendarQuery.isMatchingCalendarId(CALENDAR_ID))
		{
			return Stream.empty();
		}

		// TODO consider onlyResourceIds
		// TODO consider date range

		final WOProjectFrontendURLsProvider frontendURLs = new WOProjectFrontendURLsProvider();

		final ArrayList<CalendarEntry> result = new ArrayList<>();
		result.addAll(query_BudgetProjects(calendarQuery, frontendURLs));
		result.addAll(query_WOProjects(calendarQuery, frontendURLs));
		return result.stream();
	}

	private List<CalendarEntry> query_WOProjects(final CalendarQuery calendarQuery, final WOProjectFrontendURLsProvider frontendURLs)
	{
		final ImmutableMap<ProjectId, WOProject> woProjects = Maps.uniqueIndex(woProjectService.getAllActiveProjects(), WOProject::getProjectId);
		final ImmutableMap<WOProjectStepId, WOProjectStep> stepsById = woProjectService.getStepsByProjectIds(woProjects.keySet())
				.values()
				.stream()
				.flatMap(WOProjectSteps::stream)
				.collect(ImmutableMap.toImmutableMap(WOProjectStep::getId, step -> step));

		final WOProjectSimulationPlan simulationPlan = calendarQuery.getSimulationId() != null ? woProjectSimulationService.getSimulationPlanById(calendarQuery.getSimulationId()) : null;

		final WOProjectResourcesCollection allProjectResources = woProjectService.getResourcesByProjectIds(woProjects.keySet());

		return allProjectResources.streamProjectResources()
				.map(resource -> toCalendarEntry(
						simulationPlan != null ? simulationPlan.applyOn(resource) : resource,
						stepsById.get(resource.getStepId()),
						woProjects.get(resource.getProjectId()),
						simulationPlan != null ? simulationPlan.getSimulationPlanId() : null,
						frontendURLs)
				)
				.collect(ImmutableList.toImmutableList());
	}

	private List<CalendarEntry> query_BudgetProjects(final CalendarQuery calendarQuery, final WOProjectFrontendURLsProvider frontendURLs)
	{
		final ImmutableMap<ProjectId, BudgetProject> budgetProjects = Maps.uniqueIndex(budgetProjectService.getAllActiveProjects(), BudgetProject::getProjectId);
		final Map<ProjectId, BudgetProjectResources> budgetsByProjectId = budgetProjectService.getBudgetsByProjectIds(budgetProjects.keySet());

		final BudgetProjectSimulationPlan simulationPlan = calendarQuery.getSimulationId() != null ? budgetProjectSimulationRepository.getSimulationPlanById(calendarQuery.getSimulationId()) : null;

		return budgetsByProjectId.values()
				.stream()
				.flatMap(budgets -> budgets.getBudgets().stream())
				.map(budget -> toCalendarEntry(
						simulationPlan != null ? simulationPlan.applyOn(budget) : budget,
						budgetProjects.get(budget.getProjectId()),
						simulationPlan != null ? simulationPlan.getSimulationPlanId() : null,
						frontendURLs)
				)
				.collect(ImmutableList.toImmutableList());
	}

	private CalendarEntry toCalendarEntry(
			@NonNull final BudgetProjectResource budget,
			@NonNull final BudgetProject project,
			@Nullable final SimulationPlanId simulationId,
			@NonNull final WOProjectFrontendURLsProvider frontendURLs)
	{
		return CalendarEntry.builder()
				.entryId(BudgetAndWOCalendarEntryIdConverters.from(budget.getProjectId(), budget.getId()))
				.simulationId(simulationId)
				.resourceId(CalendarResourceId.ofRepoId(CoalesceUtil.coalesceNotNull(budget.getResourceId(), budget.getResourceGroupId())))
				.title(TranslatableStrings.builder()
						.append(project.getName())
						.append(" - ")
						.appendQty(budget.getPlannedDuration().toBigDecimal(), budget.getPlannedDuration().getUOMSymbol())
						.build())
				.description(TranslatableStrings.anyLanguage(budget.getDescription()))
				.dateRange(budget.getDateRange())
				.editable(simulationId != null)
				.color("#89D72D") // metasfresh green
				.url(frontendURLs.getFrontendURL(budget.getProjectId()).orElse(null))
				.build();
	}

	private CalendarEntry toCalendarEntry(
			@NonNull final WOProjectResource resource,
			@NonNull final WOProjectStep step,
			@NonNull final WOProject project,
			@Nullable final SimulationPlanId simulationId,
			@NonNull final WOProjectFrontendURLsProvider frontendURLs)
	{
		final int durationInt = DurationUtils.toInt(resource.getDuration(), resource.getDurationUnit());
		final String durationUomSymbol = getTemporalUnitSymbolOrEmpty(resource.getDurationUnit());

		return CalendarEntry.builder()
				.entryId(BudgetAndWOCalendarEntryIdConverters.from(resource.getWOProjectAndResourceId()))
				.simulationId(simulationId)
				.resourceId(CalendarResourceId.ofRepoId(resource.getResourceId()))
				.title(TranslatableStrings.builder()
						.append(project.getName())
						.append(" - ")
						.append(step.getSeqNo() + "_" + step.getName())
						.append(" - ")
						.appendQty(durationInt, durationUomSymbol)
						.build()
				)
				.description(TranslatableStrings.anyLanguage(resource.getDescription()))
				.dateRange(resource.getDateRange())
				.editable(simulationId != null)
				.color("#FFCF60") // orange-ish
				.url(frontendURLs.getFrontendURL(resource.getProjectId()).orElse(null))
				.build();
	}

	private String getTemporalUnitSymbolOrEmpty(final @NonNull TemporalUnit temporalUnit)
	{
		try
		{
			return StringUtils.trimToEmpty(uomDAO.getByTemporalUnit(temporalUnit).getUOMSymbol());
		}
		catch (final Exception ex)
		{
			logger.warn("Failed to get UOM Symbol for TemporalUnit: {}", temporalUnit, ex);
			return "";
		}
	}

	@Override
	public CalendarEntry addEntry(final CalendarEntryAddRequest request)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public CalendarEntryUpdateResult updateEntry(final CalendarEntryUpdateRequest request)
	{
		CALENDAR_ID.assertEqualsTo(request.getCalendarId());

		if (request.getSimulationId() == null)
		{
			throw new AdempiereException("Changing entries outside of simulation is not allowed");
		}

		return BudgetAndWOCalendarEntryIdConverters.withProjectResourceId(
				request.getEntryId(),
				budgetProjectAndResourceId -> updateEntry_BudgetProjectResource(request, budgetProjectAndResourceId),
				projectAndResourceId -> updateEntry_WOProjectResource(request, projectAndResourceId));
	}

	private CalendarEntryUpdateResult updateEntry_BudgetProjectResource(@NonNull final CalendarEntryUpdateRequest request, @NonNull final BudgetProjectAndResourceId projectAndResourceId)
	{
		final BudgetProject project = budgetProjectService.getById(projectAndResourceId.getProjectId())
				.orElseThrow(() -> new AdempiereException("No Budget Project found for " + projectAndResourceId.getProjectId()));

		final BudgetProjectResource actualBudget = budgetProjectService.getBudgetsById(projectAndResourceId.getProjectId(), projectAndResourceId.getProjectResourceId());

		final WOProjectFrontendURLsProvider frontendURLs = new WOProjectFrontendURLsProvider();

		final OldAndNewValues<CalendarEntry> result = budgetProjectSimulationRepository
				.createOrUpdate(
						BudgetProjectResourceSimulation.UpdateRequest.builder()
								.simulationId(Check.assumeNotNull(request.getSimulationId(), "simulationId is set: {}", request))
								.projectAndResourceId(projectAndResourceId)
								.dateRange(CoalesceUtil.coalesceNotNull(request.getDateRange(), actualBudget.getDateRange()))
								.build())
				.map(simulation -> simulation != null ? simulation.applyOn(actualBudget) : actualBudget)
				.map(budget -> toCalendarEntry(
						actualBudget,
						project,
						request.getSimulationId(),
						frontendURLs));

		return CalendarEntryUpdateResult.ofChangedEntry(result);
	}

	private CalendarEntryUpdateResult updateEntry_WOProjectResource(
			@NonNull final CalendarEntryUpdateRequest request,
			@NonNull final WOProjectAndResourceId projectAndResourceId)
	{
		final SimulationPlanId simulationId = Check.assumeNotNull(request.getSimulationId(), "simulationId shall be set: {}", request);

		final WOProjectResources projectResources = woProjectService.getResourcesByProjectId(projectAndResourceId.getProjectId());

		final WOProjectSimulationPlanEditor simulationEditor = WOProjectSimulationPlanEditor.builder()
				.project(woProjectService.getById(projectAndResourceId.getProjectId()))
				.steps(woProjectService.getStepsByProjectId(projectAndResourceId.getProjectId()))
				.projectResources(projectResources)
				.currentSimulationPlan(woProjectSimulationService.getSimulationPlanById(simulationId))
				.build();

		if (request.getDateRange() != null)
		{
			final WOProjectStepId stepId = projectResources.getStepId(projectAndResourceId.getProjectResourceId());
			simulationEditor.changeResourceDateRangeAndShiftSteps(projectAndResourceId, request.getDateRange(), stepId);
		}
		if (!Check.isBlank(request.getTitle()))
		{
			throw new AdempiereException("Changing title is not supported yet");
		}
		if (!Check.isBlank(request.getDescription()))
		{
			throw new AdempiereException("Changing description is not supported yet");

		}

		//
		// Save changed plan to database
		final WOProjectSimulationPlan simulation = simulationEditor.toNewSimulationPlan();
		woProjectSimulationService.savePlan(simulation);

		//
		// Check conflicts
		woProjectConflictService.checkSimulationConflicts(simulation, simulationEditor.getAffectedResourceIds());
		// TODO: send conflicts changes to websockets

		//
		// toCalendarEntry converter:
		final WOProjectFrontendURLsProvider frontendURLs = new WOProjectFrontendURLsProvider();
		final Function<WOProjectResource, CalendarEntry> toCalendarEntry = woProjectResource -> toCalendarEntry(
				woProjectResource,
				simulationEditor.getStepById(woProjectResource.getStepId()),
				simulationEditor.getProjectById(woProjectResource.getProjectId()),
				simulationEditor.getSimulationPlanId(),
				frontendURLs);

		//
		return CalendarEntryUpdateResult.builder()
				.changedEntry(simulationEditor.mapProjectResourceInitialAndNow(projectAndResourceId.getProjectResourceId(), toCalendarEntry))
				.otherChangedEntries(
						simulationEditor.streamChangedProjectResourceIds()
								.filter(projectResourceId -> !WOProjectResourceId.equals(projectResourceId, projectAndResourceId.getProjectResourceId()))
								.map(projectResourceId -> simulationEditor.mapProjectResourceInitialAndNow(projectResourceId, toCalendarEntry))
								.collect(ImmutableList.toImmutableList())
				)
				.build();
	}

	@Override
	public void deleteEntryById(final @NonNull CalendarEntryId entryId, @Nullable SimulationPlanId simulationId)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public CalendarEntry getEntryById(
			@NonNull final CalendarEntryId entryId,
			@Nullable final SimulationPlanId simulationId)
	{
		return BudgetAndWOCalendarEntryIdConverters.withProjectResourceId(
				entryId,
				budgetProjectAndResourceId -> getEntryByBudgetResourceId(budgetProjectAndResourceId, simulationId),
				budgetProjectAndResourceId -> getEntryByWOProjectResourceId(budgetProjectAndResourceId, simulationId));
	}

	private CalendarEntry getEntryByBudgetResourceId(
			@NonNull final BudgetProjectAndResourceId budgetProjectAndResourceId,
			@Nullable final SimulationPlanId simulationId)
	{
		BudgetProjectResource budget = budgetProjectService.getBudgetsById(budgetProjectAndResourceId);
		if (simulationId != null)
		{
			budget = budgetProjectSimulationRepository.getSimulationPlanById(simulationId).applyOn(budget);
		}

		final BudgetProject project = budgetProjectService.getById(budgetProjectAndResourceId.getProjectId())
				.orElseThrow(() -> new AdempiereException("No project found for " + budgetProjectAndResourceId));
		final WOProjectFrontendURLsProvider frontendUrls = new WOProjectFrontendURLsProvider();

		return toCalendarEntry(budget, project, simulationId, frontendUrls);
	}

	private CalendarEntry getEntryByWOProjectResourceId(
			@NonNull final WOProjectAndResourceId woProjectAndResourceId,
			@Nullable final SimulationPlanId simulationId)
	{
		final ProjectId projectId = woProjectAndResourceId.getProjectId();
		final WOProjectResourceId projectResourceId = woProjectAndResourceId.getProjectResourceId();

		final WOProjectResources projectResources = woProjectService.getResourcesByProjectId(projectId);
		final WOProjectStepId stepId = projectResources.getStepId(projectResourceId);

		WOProjectResource resource = projectResources.getById(projectResourceId);
		if (simulationId != null)
		{
			resource = woProjectSimulationService.getSimulationPlanById(simulationId).applyOn(resource);
		}

		final WOProjectFrontendURLsProvider frontendUrls = new WOProjectFrontendURLsProvider();

		return toCalendarEntry(
				resource,
				woProjectService.getStepsByProjectId(projectId).getById(stepId),
				woProjectService.getById(projectId),
				simulationId,
				frontendUrls);
	}
}
