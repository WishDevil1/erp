package de.metas.acct.api.impl;

import com.google.common.collect.ImmutableList;
import de.metas.acct.api.FactAcctQuery;
import de.metas.acct.api.IFactAcctDAO;
import de.metas.acct.api.IFactAcctListenersService;
import de.metas.document.engine.IDocument;
import de.metas.util.Check;
import de.metas.util.Services;
import lombok.NonNull;
import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.dao.IQueryBuilder;
import org.adempiere.ad.table.api.IADTableDAO;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.ad.trx.api.ITrxManager;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.lang.impl.TableRecordReference;
import org.compiere.model.IQuery;
import org.compiere.model.I_Fact_Acct;
import org.compiere.util.Env;

import java.util.List;
import java.util.Properties;

import static org.adempiere.model.InterfaceWrapperHelper.load;

public class FactAcctDAO implements IFactAcctDAO
{
	private final IQueryBL queryBL = Services.get(IQueryBL.class);
	private final IADTableDAO adTableDAO = Services.get(IADTableDAO.class);
	private final ITrxManager trxManager = Services.get(ITrxManager.class);
	private final IFactAcctListenersService factAcctListenersService = Services.get(IFactAcctListenersService.class);

	@Override
	public I_Fact_Acct getById(final int factAcctId)
	{
		return load(factAcctId, I_Fact_Acct.class);
	}

	@Override
	public int deleteForDocument(final IDocument document)
	{
		final int countDeleted = retrieveQueryForDocument(document)
				.create()
				.deleteDirectly();

		factAcctListenersService.fireAfterUnpost(document);

		return countDeleted;
	}

	@Override
	public int deleteForDocumentModel(@NonNull final Object documentObj)
	{
		final int adTableId = InterfaceWrapperHelper.getModelTableId(documentObj);
		final int recordId = InterfaceWrapperHelper.getId(documentObj);
		final int countDeleted = retrieveQueryForDocument(Env.getCtx(), adTableId, recordId, ITrx.TRXNAME_ThreadInherited)
				.create()
				.deleteDirectly();

		factAcctListenersService.fireAfterUnpost(documentObj);

		return countDeleted;
	}

	@Override
	public int deleteForRecordRef(@NonNull final TableRecordReference recordRef)
	{
		final int adTableId = recordRef.getAD_Table_ID();
		final int recordId = recordRef.getRecord_ID();
		final int countDeleted = retrieveQueryForDocument(Env.getCtx(), adTableId, recordId, ITrx.TRXNAME_ThreadInherited)
				.create()
				.deleteDirectly();

		factAcctListenersService.fireAfterUnpost(recordRef);

		return countDeleted;
	}

	@Override
	public IQueryBuilder<I_Fact_Acct> retrieveQueryForDocument(@NonNull final IDocument document)
	{
		final Properties ctx = document.getCtx();
		final String trxName = document.get_TrxName();
		final int adTableId = document.get_Table_ID();
		final int recordId = document.get_ID();
		return retrieveQueryForDocument(ctx, adTableId, recordId, trxName);
	}

	private IQueryBuilder<I_Fact_Acct> retrieveQueryForDocument(final Properties ctx, final int adTableId, final int recordId, final String trxName)
	{
		return queryBL
				.createQueryBuilder(I_Fact_Acct.class, ctx, trxName)
				.addEqualsFilter(I_Fact_Acct.COLUMNNAME_AD_Table_ID, adTableId)
				.addEqualsFilter(I_Fact_Acct.COLUMNNAME_Record_ID, recordId)
				.orderBy()
				.addColumn(I_Fact_Acct.COLUMNNAME_Fact_Acct_ID) // make sure we have a predictable order
				.endOrderBy();
	}

	@Override
	public List<I_Fact_Acct> retrieveForDocumentLine(final String tableName, final int recordId, @NonNull final Object documentLine)
	{
		final int adTableId = adTableDAO.retrieveTableId(tableName);
		final int lineId = InterfaceWrapperHelper.getId(documentLine);

		final IQueryBuilder<I_Fact_Acct> queryBuilder = queryBL
				.createQueryBuilder(I_Fact_Acct.class, documentLine)
				.addEqualsFilter(I_Fact_Acct.COLUMNNAME_AD_Table_ID, adTableId)
				.addEqualsFilter(I_Fact_Acct.COLUMNNAME_Record_ID, recordId)
				.addEqualsFilter(I_Fact_Acct.COLUMNNAME_Line_ID, lineId);

		// make sure we have a predictable order
		queryBuilder.orderBy()
				.addColumn(I_Fact_Acct.COLUMNNAME_Fact_Acct_ID);

		return queryBuilder.create().list();
	}

	@Override
	public void updateDocStatusForDocument(final IDocument document)
	{
		final String docStatus = document.getDocStatus();
		retrieveQueryForDocument(document)
				.create()
				.updateDirectly()
				.addSetColumnValue(I_Fact_Acct.COLUMNNAME_DocStatus, docStatus)
				.execute();
	}

	@Override
	public int updateActivityForDocumentLine(final Properties ctx, final int adTableId, final int recordId, final int lineId, final int activityId)
	{
		// Make sure we are updating the Fact_Acct records in a transaction
		trxManager.assertThreadInheritedTrxExists();

		return queryBL.createQueryBuilder(I_Fact_Acct.class, ctx, ITrx.TRXNAME_ThreadInherited)
				.addEqualsFilter(I_Fact_Acct.COLUMNNAME_AD_Table_ID, adTableId)
				.addEqualsFilter(I_Fact_Acct.COLUMNNAME_Record_ID, recordId)
				.addEqualsFilter(I_Fact_Acct.COLUMNNAME_Line_ID, lineId)
				.addNotEqualsFilter(I_Fact_Acct.COLUMNNAME_C_Activity_ID, activityId)
				.create()
				.updateDirectly()
				.addSetColumnValue(I_Fact_Acct.COLUMNNAME_C_Activity_ID, activityId)
				.execute();
	}

	@Override
	public List<I_Fact_Acct> list(@NonNull final List<FactAcctQuery> queries)
	{
		final IQuery<I_Fact_Acct> query = queries.stream()
				.map(this::toSqlQuery)
				.reduce(IQuery.unionDistict())
				.orElse(null);
		if (query == null)
		{
			return ImmutableList.of();
		}

		return query.list();
	}

	@Override
	public List<I_Fact_Acct> list(@NonNull final FactAcctQuery query)
	{
		return toSqlQuery(query).list();
	}

	private IQuery<I_Fact_Acct> toSqlQuery(@NonNull final FactAcctQuery query)
	{
		final IQueryBuilder<I_Fact_Acct> queryBuilder = queryBL.createQueryBuilder(I_Fact_Acct.class)
				.orderBy(I_Fact_Acct.COLUMNNAME_Fact_Acct_ID);

		if (query.getAcctSchemaId() != null)
		{
			queryBuilder.addEqualsFilter(I_Fact_Acct.COLUMNNAME_C_AcctSchema_ID, query.getAcctSchemaId());
		}
		if (!Check.isBlank(query.getAccountConceptualName()))
		{
			queryBuilder.addEqualsFilter(I_Fact_Acct.COLUMNNAME_AccountConceptualName, query.getAccountConceptualName());
		}
		if (query.getTableName() != null)
		{
			queryBuilder.addEqualsFilter(I_Fact_Acct.COLUMNNAME_AD_Table_ID, adTableDAO.retrieveAdTableId(query.getTableName()));
		}
		if (query.getRecordId() > 0)
		{
			queryBuilder.addEqualsFilter(I_Fact_Acct.COLUMNNAME_Record_ID, query.getRecordId());
		}
		if (query.getLineId() > 0)
		{
			queryBuilder.addEqualsFilter(I_Fact_Acct.COLUMNNAME_Line_ID, query.getLineId());
		}

		return queryBuilder.create();
	}
}
