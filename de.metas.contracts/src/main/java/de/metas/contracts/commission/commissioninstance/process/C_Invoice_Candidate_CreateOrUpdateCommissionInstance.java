package de.metas.contracts.commission.commissioninstance.process;

import java.util.Iterator;

import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.dao.IQueryBuilder;
import org.adempiere.ad.dao.IQueryFilter;
import org.compiere.SpringContextHolder;
import org.compiere.model.IQuery;
import org.slf4j.Logger;
import org.slf4j.MDC.MDCCloseable;

import ch.qos.logback.classic.Level;
import de.metas.contracts.commission.commissioninstance.services.InvoiceCandidateFacadeService;
import de.metas.error.AdIssueId;
import de.metas.error.IErrorManager;
import de.metas.invoicecandidate.InvoiceCandidateId;
import de.metas.invoicecandidate.model.I_C_Invoice_Candidate;
import de.metas.logging.LogManager;
import de.metas.logging.TableRecordMDC;
import de.metas.process.IProcessPrecondition;
import de.metas.process.IProcessPreconditionsContext;
import de.metas.process.JavaProcess;
import de.metas.process.ProcessPreconditionsResolution;
import de.metas.process.RunOutOfTrx;
import de.metas.util.Loggables;
import de.metas.util.Services;
import lombok.NonNull;
import lombok.Value;

/*
 * #%L
 * de.metas.contracts
 * %%
 * Copyright (C) 2020 metas GmbH
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

public class C_Invoice_Candidate_CreateOrUpdateCommissionInstance
		extends JavaProcess
		implements IProcessPrecondition
{
	private final InvoiceCandidateFacadeService invoiceCandidateFacadeService = SpringContextHolder.instance.getBean(InvoiceCandidateFacadeService.class);

	private final IErrorManager errorManager = Services.get(IErrorManager.class);

	private static final Logger logger = LogManager.getLogger(C_Invoice_Candidate_CreateOrUpdateCommissionInstance.class);

	@Override
	public ProcessPreconditionsResolution checkPreconditionsApplicable(@NonNull final IProcessPreconditionsContext context)
	{
		if (context.isNoSelection())
		{
			return ProcessPreconditionsResolution.rejectBecauseNoSelection();
		}
		return ProcessPreconditionsResolution.accept();
	}

	@Override
	@RunOutOfTrx // each invoice candidate is processed in its own transaction
	protected String doIt() throws Exception
	{
		final IQueryFilter<I_C_Invoice_Candidate> selectedICsFilter = getProcessInfo().getQueryFilterOrElseFalse();
		logger.debug("selectedICsFilter={}", selectedICsFilter);

		Loggables.withLogger(logger, Level.DEBUG).addLog("Processing sales order InvoiceCandidates");
		final Iterator<InvoiceCandidateId> salesOrderIcIds = createInvoiceCandidateIdIterator(selectedICsFilter);
		final Result salesOrderIcResult = processInvoiceCandidates(salesOrderIcIds);
		Loggables.withLogger(logger, Level.DEBUG).addLog("Processed {} InvoiceCandidates; anyException={}", salesOrderIcResult.getCounter(), salesOrderIcResult.isAnyException());

		return MSG_OK;
	}

	private Iterator<InvoiceCandidateId> createInvoiceCandidateIdIterator(@NonNull final IQueryFilter<I_C_Invoice_Candidate> selectedICsFilter)
	{
		final IQueryBuilder<I_C_Invoice_Candidate> queryBuilder = Services.get(IQueryBL.class)
				.createQueryBuilder(I_C_Invoice_Candidate.class)
				.addOnlyActiveRecordsFilter()
				.filter(selectedICsFilter);

		final Iterator<InvoiceCandidateId> icIds = queryBuilder
				.create()
				.setOption(IQuery.OPTION_GuaranteedIteratorRequired, true)
				.setOption(IQuery.OPTION_IteratorBufferSize, 1000)
				.iterateIds(InvoiceCandidateId::ofRepoId);
		return icIds;
	}

	private Result processInvoiceCandidates(@NonNull final Iterator<InvoiceCandidateId> invoiceCandidateIds)
	{
		int counter = 0;
		boolean anyException = false;
		while (invoiceCandidateIds.hasNext())
		{
			final InvoiceCandidateId invoiceCandidateId = invoiceCandidateIds.next();
			try (final MDCCloseable invoiceCandidateIdMDC = TableRecordMDC.putTableRecordReference(I_C_Invoice_Candidate.Table_Name, invoiceCandidateId))
			{
				trxManager.runInNewTrx(() -> {
					logger.debug("Processing invoiceCandidate");
					invoiceCandidateFacadeService.syncICToCommissionInstance(invoiceCandidateId, false/* candidateDeleted */);
				});
				counter++;
			}
			catch (final RuntimeException e)
			{
				anyException = true;
				final AdIssueId adIssueId = errorManager.createIssue(e);
				Loggables.withLogger(logger, Level.DEBUG)
						.addLog("C_Invoice_Candidate_ID={}: Caught {} and created AD_Issue_ID={}; exception-message={}",
								invoiceCandidateId.getRepoId(), e.getClass(), adIssueId.getRepoId(), e.getLocalizedMessage());
			}
		}
		return new Result(counter, anyException);
	}

	@Value
	private static class Result
	{
		int counter;
		boolean anyException;
	}
}
