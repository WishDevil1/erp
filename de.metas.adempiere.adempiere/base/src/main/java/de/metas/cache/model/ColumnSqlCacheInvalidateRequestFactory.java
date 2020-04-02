package de.metas.cache.model;

import java.util.List;

import javax.annotation.Nullable;

import org.adempiere.ad.expression.api.IExpressionEvaluator.OnVariableNotFound;
import org.adempiere.ad.expression.api.IStringExpression;
import org.adempiere.ad.expression.api.impl.StringExpressionCompiler;
import org.compiere.util.DB;
import org.compiere.util.Evaluatee;
import org.compiere.util.Evaluatees;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.metas.util.Check;
import lombok.Builder;
import lombok.NonNull;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
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

public class ColumnSqlCacheInvalidateRequestFactory implements ModelCacheInvalidateRequestFactory
{
	private final String targetTableName;
	private final IStringExpression sqlToGetTargetRecordIdBySourceRecordId;

	private static final String EVAL_CTXNAME_Record_ID = "Record_ID";

	@Builder
	private ColumnSqlCacheInvalidateRequestFactory(
			@NonNull final String targetTableName,
			@Nullable final String sqlToGetTargetRecordIdBySourceRecordId)
	{
		this.targetTableName = targetTableName;

		if (Check.isBlank(sqlToGetTargetRecordIdBySourceRecordId))
		{
			this.sqlToGetTargetRecordIdBySourceRecordId = null;
		}
		else
		{
			this.sqlToGetTargetRecordIdBySourceRecordId = StringExpressionCompiler.instance.compile(sqlToGetTargetRecordIdBySourceRecordId);
		}
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
				.omitNullValues()
				.add("targetTableName", targetTableName)
				.add("sqlToGetTargetRecordIdBySourceRecordId", sqlToGetTargetRecordIdBySourceRecordId != null ? sqlToGetTargetRecordIdBySourceRecordId.getExpressionString() : null)
				.toString();
	}

	@Override
	public List<CacheInvalidateRequest> createRequestsFromModel(
			@NonNull final ICacheSourceModel sourceModel,
			@NonNull final ModelCacheInvalidationTiming timing_NOTNUSED)
	{
		final int sourceRecordId = sourceModel.getRecordId();
		return createRequestsFromSourceRecordId(sourceRecordId);
	}

	private List<CacheInvalidateRequest> createRequestsFromSourceRecordId(final int sourceRecordId)
	{
		if (sourceRecordId <= 0)
		{
			return ImmutableList.of();
		}

		if (sqlToGetTargetRecordIdBySourceRecordId == null)
		{
			return ImmutableList.of(CacheInvalidateRequest.allRecordsForTable(targetTableName));

		}
		else
		{
			final ImmutableSet<Integer> targetRecordIds = getTargetRecordIds(sourceRecordId);
			return targetRecordIds.stream()
					.map(targetRecordId -> CacheInvalidateRequest.rootRecord(targetTableName, targetRecordId))
					.collect(ImmutableList.toImmutableList());
		}
	}

	private ImmutableSet<Integer> getTargetRecordIds(final int sourceRecordId)
	{
		final ImmutableSet.Builder<Integer> targetRecordIds = ImmutableSet.builder();
		final Evaluatee evalCtx = Evaluatees.mapBuilder()
				.put(EVAL_CTXNAME_Record_ID, sourceRecordId)
				.build();

		DB.forEachRow(
				sqlToGetTargetRecordIdBySourceRecordId.evaluate(evalCtx, OnVariableNotFound.Fail),
				ImmutableList.of(),
				rs -> {
					final int targetRecordId = rs.getInt(1);
					if (targetRecordId > 0)
					{
						targetRecordIds.add(targetRecordId);
					}
				});

		return targetRecordIds.build();
	}
}
