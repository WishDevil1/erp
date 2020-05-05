/*
 * #%L
 * de.metas.serviceprovider.base
 * %%
 * Copyright (C) 2019 metas GmbH
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

package de.metas.serviceprovider.issue;

import de.metas.organization.OrgId;
import de.metas.project.ProjectId;
import de.metas.serviceprovider.external.issuedetails.ExternalIssueDetailsRepository;
import de.metas.serviceprovider.milestone.MilestoneId;
import de.metas.serviceprovider.model.I_S_Issue;
import de.metas.uom.UomId;
import de.metas.user.UserId;
import de.metas.util.NumberUtils;
import de.metas.util.Services;
import lombok.NonNull;
import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class IssueRepository
{
	private final IQueryBL queryBL = Services.get(IQueryBL.class);

	private final ExternalIssueDetailsRepository externalIssueDetailsRepository;

	public IssueRepository(final ExternalIssueDetailsRepository externalIssueDetailsRepository)
	{
		this.externalIssueDetailsRepository = externalIssueDetailsRepository;
	}

	public void save(final IssueEntity issueEntity)
	{
		final I_S_Issue record;

		record = InterfaceWrapperHelper.loadOrNew(issueEntity.getIssueId(), I_S_Issue.class);

		record.setAD_Org_ID(issueEntity.getOrgId().getRepoId());
		record.setAD_User_ID(NumberUtils.asInt(issueEntity.getAssigneeId(), -1));
		record.setC_Project_ID(NumberUtils.asInt(issueEntity.getProjectId(), -1));

		record.setName(issueEntity.getName());
		record.setValue(issueEntity.getSearchKey());
		record.setDescription(issueEntity.getDescription());

		record.setIssueType(issueEntity.getType().getValue());
		record.setIsEffortIssue(issueEntity.isEffortIssue());
		record.setProcessed(issueEntity.isProcessed());

		record.setS_Milestone_ID(NumberUtils.asInt(issueEntity.getMilestoneId(), -1));
		record.setEstimatedEffort(issueEntity.getEstimatedEffort());
		record.setBudgetedEffort(issueEntity.getBudgetedEffort());
		record.setEffort_UOM_ID(issueEntity.getEffortUomId().getRepoId());

		record.setExternalId(issueEntity.getExternalIssueId());
		record.setExternalIssueNo(issueEntity.getExternalIssueNo());
		record.setIssueURL(issueEntity.getExternalIssueURL());

		InterfaceWrapperHelper.saveRecord(record);

		final IssueId issueId = IssueId.ofRepoId(record.getS_Issue_ID());

		issueEntity.setIssueId(issueId);

		externalIssueDetailsRepository.persistIssueDetails(issueId, issueEntity.getExternalIssueDetails());
	}

	@NonNull
	public Optional<IssueEntity> getEntityByExternalId(@NonNull final String externalIssueId)
	{
		final I_S_Issue budget_issue = queryBL
				.createQueryBuilder(I_S_Issue.class)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(I_S_Issue.COLUMNNAME_ExternalId, externalIssueId)
				.create()
				.firstOnly(I_S_Issue.class);

		return budget_issue != null ? Optional.of(buildIssueEntity(budget_issue)) : Optional.empty();
	}

	@NonNull
	private IssueEntity buildIssueEntity(final I_S_Issue record)
	{
		final Optional<IssueType> issueType = IssueType.getTypeByValue(record.getIssueType());

		if (!issueType.isPresent())
		{
			throw new AdempiereException("Unknown IssueType!").appendParametersToMessage()
					.setParameter("I_S_Issue", record);
		}

		return IssueEntity.builder()
				.orgId(OrgId.ofRepoId(record.getAD_Org_ID()))
				.projectId(ProjectId.ofRepoId(record.getC_Project_ID()))
				.issueId(IssueId.ofRepoId(record.getS_Issue_ID()))
				.effortUomId(UomId.ofRepoId(record.getEffort_UOM_ID()))
				.milestoneId(MilestoneId.ofRepoIdOrNull(record.getS_Milestone_ID()))
				.assigneeId(UserId.ofRepoIdOrNullIfSystem(record.getAD_User_ID()))
				.name(record.getName())
				.searchKey(record.getValue())
				.description(record.getDescription())
				.type(issueType.get())
				.processed(record.isProcessed())
				.isEffortIssue(record.isEffortIssue())
				.estimatedEffort(record.getEstimatedEffort())
				.budgetedEffort(record.getBudgetedEffort())
				.externalIssueId(record.getExternalId())
				.externalIssueNo(record.getExternalIssueNo())
				.externalIssueURL(record.getIssueURL())
				.externalIssueDetails(externalIssueDetailsRepository.getByIssueId(IssueId.ofRepoId(record.getS_Issue_ID())))
				.build();
	}
}
