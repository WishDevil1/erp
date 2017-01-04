package de.metas.dlm.partitioner.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.adempiere.util.lang.ITableRecordReference;

import de.metas.dlm.Partition;
import de.metas.dlm.Partition.WorkQueue;

/*
 * #%L
 * metasfresh-dlm
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

public interface IStorableIterateResult extends IIterateResult
{
	Map<Integer, Set<ITableRecordReference>> getDlmPartitionId2Record();

	Map<String, Collection<ITableRecordReference>> getTableName2Record();

	void clearAfterPartitionStored(Partition partition);

	List<WorkQueue> getQueueRecordsToStore();

	List<WorkQueue> getQueueRecordsToDelete();

	/**
	 * @return the {@link Partition} from the last invokation of {@link #clearAfterPartitionStored(Partition)}, or an empty partition.
	 */
	public Partition getPartition();
}
