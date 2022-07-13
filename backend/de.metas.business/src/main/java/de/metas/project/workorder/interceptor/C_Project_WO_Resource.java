package de.metas.project.workorder.interceptor;

import de.metas.calendar.CalendarEntryId;
import de.metas.calendar.MultiCalendarService;
import de.metas.product.ResourceId;
import de.metas.project.workorder.WOProjectAndStepId;
import de.metas.project.workorder.WOProjectResourceRepository;
import de.metas.project.workorder.WOProjectService;
import de.metas.project.workorder.calendar.BudgetAndWOCalendarEntryIdConverters;
import de.metas.project.workorder.conflicts.WOProjectConflictService;
import de.metas.util.Services;
import lombok.NonNull;
import org.adempiere.ad.modelvalidator.ModelChangeType;
import org.adempiere.ad.modelvalidator.annotations.Interceptor;
import org.adempiere.ad.modelvalidator.annotations.ModelChange;
import org.adempiere.ad.trx.api.ITrxManager;
import org.adempiere.ad.trx.api.OnTrxMissingPolicy;
import org.adempiere.model.InterfaceWrapperHelper;
import org.compiere.model.I_C_Project_WO_Resource;
import org.compiere.model.ModelValidator;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
@Interceptor(I_C_Project_WO_Resource.class)
public class C_Project_WO_Resource
{
	private final ITrxManager trxManager = Services.get(ITrxManager.class);
	private final WOProjectService woProjectService;
	private final WOProjectConflictService woProjectConflictService;
	private final MultiCalendarService multiCalendarService;

	public C_Project_WO_Resource(
			@NonNull final WOProjectService woProjectService,
			@NonNull final WOProjectConflictService woProjectConflictService,
			@NonNull final MultiCalendarService multiCalendarService)
	{
		this.woProjectService = woProjectService;
		this.woProjectConflictService = woProjectConflictService;
		this.multiCalendarService = multiCalendarService;
	}

	@ModelChange(timings = { ModelValidator.TYPE_BEFORE_NEW, ModelValidator.TYPE_BEFORE_CHANGE })
	public void beforeSave(@NonNull final I_C_Project_WO_Resource record)
	{
		// validate
		WOProjectResourceRepository.fromRecord(record);
	}

	@ModelChange(timings = { ModelValidator.TYPE_AFTER_NEW, ModelValidator.TYPE_AFTER_CHANGE })
	public void afterSave(@NonNull final I_C_Project_WO_Resource record, @NonNull final ModelChangeType changeType)
	{
		notifyIfUserChange(record, changeType);
		updateStepDatesAfterCommit(WOProjectAndStepId.ofRepoId(record.getC_Project_ID(), record.getC_Project_WO_Step_ID()));
		checkConflictsAfterCommitIfUserChange(record);
	}

	@ModelChange(timings = ModelValidator.TYPE_AFTER_DELETE)
	public void afterDelete(@NonNull final I_C_Project_WO_Resource record, @NonNull final ModelChangeType changeType)
	{
		notifyIfUserChange(record, changeType);
		updateStepDatesAfterCommit(WOProjectAndStepId.ofRepoId(record.getC_Project_ID(), record.getC_Project_WO_Step_ID()));
		checkConflictsAfterCommitIfUserChange(record);
	}

	private void updateStepDatesAfterCommit(@NonNull final WOProjectAndStepId stepId)
	{
		trxManager.getThreadInheritedTrx(OnTrxMissingPolicy.Fail)
				.getPropertyAndProcessAfterCommit(
						"C_Project_WO_Resource.updateStepDatesAfterCommit",
						HashSet::new,
						woProjectService::updateStepsDateRange
				)
				.add(stepId);
	}

	private void notifyIfUserChange(
			@NonNull final I_C_Project_WO_Resource record,
			@NonNull final ModelChangeType changeType)
	{
		if (!InterfaceWrapperHelper.isUIAction(record))
		{
			return;
		}

		final CalendarEntryId entryId = extractCalendarEntryId(record);
		if (changeType.isNewOrChange() && record.isActive())
		{
			multiCalendarService.notifyEntryUpdatedByUser(entryId);
		}
		else
		{
			multiCalendarService.notifyEntryDeletedByUser(entryId);
		}
	}

	@NonNull
	private static CalendarEntryId extractCalendarEntryId(final I_C_Project_WO_Resource record)
	{
		return BudgetAndWOCalendarEntryIdConverters.from(WOProjectResourceRepository.fromRecord(record).getWOProjectAndResourceId());
	}

	private void checkConflictsAfterCommitIfUserChange(@NonNull final I_C_Project_WO_Resource record)
	{
		if (!InterfaceWrapperHelper.isUIAction(record))
		{
			return;
		}

		trxManager.getThreadInheritedTrx(OnTrxMissingPolicy.Fail)
				.getPropertyAndProcessAfterCommit(
						"C_Project_WO_Resource.checkConflicts",
						HashSet::new,
						woProjectConflictService::checkConflicts
				)
				.add(getResourceId(record));

	}

	private static ResourceId getResourceId(final I_C_Project_WO_Resource record)
	{
		return WOProjectResourceRepository.fromRecord(record).getResourceId();
	}
}
