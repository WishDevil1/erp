/*
 * #%L
 * de.metas.contracts
 * %%
 * Copyright (C) 2023 metas GmbH
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

package de.metas.contracts.modular.interim.invoice.service;

import de.metas.contracts.modular.interim.invoice.InterimInvoiceFlatrateTerm;
import de.metas.contracts.modular.interim.invoice.InterimInvoiceFlatrateTermId;
import de.metas.contracts.modular.interim.invoice.InterimInvoiceFlatrateTermLine;
import de.metas.contracts.modular.interim.invoice.InterimInvoiceFlatrateTermLineId;
import de.metas.inout.InOutAndLineId;
import de.metas.inout.InOutLineId;
import de.metas.invoicecandidate.InvoiceCandidateId;
import de.metas.util.ISingletonService;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.Collection;

public interface IInterimInvoiceFlatrateTermLineDAO extends ISingletonService
{
	InterimInvoiceFlatrateTermLine getById(InterimInvoiceFlatrateTermLineId id);

	InterimInvoiceFlatrateTermLineId save(InterimInvoiceFlatrateTermLine interimInvoiceFlatrateTermLine);

	InterimInvoiceFlatrateTermLineId createInterimInvoiceLine(InterimInvoiceFlatrateTerm interimInvoiceFlatrateTerm,InOutAndLineId inOutAndLineId);

	@Nullable
	InterimInvoiceFlatrateTermLineId getByInOutLineId(@NonNull InOutLineId inOutLineId);

	void setInvoiceLineToLines(InvoiceCandidateId invoiceCandidateId, final InterimInvoiceFlatrateTermId id);

	Collection<InterimInvoiceFlatrateTermLine> getByInterimInvoiceFlatrateTermId(@NonNull final InterimInvoiceFlatrateTermId id);
}
