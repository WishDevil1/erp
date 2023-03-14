package org.adempiere.model;

import com.google.common.collect.ImmutableList;
import de.metas.adempiere.model.I_C_Order;
import de.metas.document.dimension.OrderLineDimensionFactory;
import de.metas.order.OrderFreightCostsService;
import de.metas.order.OrderLineId;
import de.metas.util.Check;
import de.metas.util.collections.CompositePredicate;
import lombok.NonNull;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.Adempiere;
import org.compiere.SpringContextHolder;
import org.compiere.model.I_C_OrderLine;
import org.compiere.model.I_C_Order_CompensationGroup;
import org.compiere.model.PO;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.adempiere.model.InterfaceWrapperHelper.load;
import static org.adempiere.model.InterfaceWrapperHelper.newInstance;

public class MOrderLinePOCopyRecordSupport extends GeneralCopyRecordSupport
{
	private static final String DYNATTR_OrderCompensationGroupIdsMap = "OrderCompensationGroupIdsMap";
	private static final String DYNATTR_ClonedOrderLinesInfo = "ClonedOrderLinesInfo";

	private final OrderLineDimensionFactory orderLineDimensionFactory = SpringContextHolder.instance.getBean(OrderLineDimensionFactory.class);

	/**
	 * Skip predicates: if it's evaluated <code>true</code> (i.e. {@link Predicate#test(Object)} returns true) then the order line will NOT copied.
	 */
	private static final CompositePredicate<I_C_OrderLine> skipPredicates = new CompositePredicate<I_C_OrderLine>()
			.addPredicate(MOrderLinePOCopyRecordSupport::isNotFreightCost);

	private static boolean isNotFreightCost(final @NonNull I_C_OrderLine orderLine)
	{
		final OrderFreightCostsService ordersFreightCostService = Adempiere.getBean(OrderFreightCostsService.class);
		return !ordersFreightCostService.isFreightCostOrderLine(orderLine);
	}

	/**
	 * Add a skip filter.
	 * <p>
	 * In case given skip filter evaluates the order line as true (i.e. {@link Predicate#test(Object)} returns true) then the order line will NOT copied.
	 */
	public static void addSkipPredicate(final Predicate<I_C_OrderLine> skipPredicate)
	{
		skipPredicates.addPredicate(skipPredicate);
	}

	/**
	 * @return true if the record shall be copied
	 */
	public static boolean isCopyRecord(final I_C_OrderLine orderLine)
	{
		return skipPredicates.isEmpty() || skipPredicates.test(orderLine);
	}

	@Override
	public void copyRecord(final PO po, final String trxName)
	{
		final I_C_OrderLine orderLine = InterfaceWrapperHelper.create(po, I_C_OrderLine.class);

		// Check if we shall skip this record
		if (!isCopyRecord(orderLine))
		{
			return;
		}

		// delegate to super
		super.copyRecord(po, trxName);
	}

	@Override
	protected void onRecordCopied(final PO to, final PO from)
	{
		final I_C_OrderLine toOrderLine = InterfaceWrapperHelper.create(to, I_C_OrderLine.class);
		final I_C_OrderLine fromOrderLine = InterfaceWrapperHelper.create(from, I_C_OrderLine.class);
		onOrderLineCopied(toOrderLine, fromOrderLine);
	}

	private void onOrderLineCopied(final I_C_OrderLine toOrderLine, final I_C_OrderLine fromOrderLine)
	{
		toOrderLine.setC_Order_CompensationGroup_ID(getOrCloneOrderCompensationGroup(fromOrderLine.getC_Order_CompensationGroup_ID()));

		orderLineDimensionFactory.updateRecord(toOrderLine, orderLineDimensionFactory.getFromRecord(fromOrderLine));
	}

	@Override
	protected void onRecordAndChildrenCopied(final PO to, final PO from)
	{
		final I_C_OrderLine toOrderLine = InterfaceWrapperHelper.create(to, I_C_OrderLine.class);
		final I_C_OrderLine fromOrderLine = InterfaceWrapperHelper.create(from, I_C_OrderLine.class);
		onRecordAndChildrenCopied(toOrderLine, fromOrderLine);
	}

	private void onRecordAndChildrenCopied(final I_C_OrderLine toOrderLine, final I_C_OrderLine fromOrderLine)
	{
		InterfaceWrapperHelper
				.computeDynAttributeIfAbsent(getTargetOrder(), DYNATTR_ClonedOrderLinesInfo, ClonedOrderLinesInfo::new)
				.addOriginalToClonedOrderLineMapping(
						OrderLineId.ofRepoId(fromOrderLine.getC_OrderLine_ID()),
						OrderLineId.ofRepoId(toOrderLine.getC_OrderLine_ID()));
	}

	@Nullable
	public static ClonedOrderLinesInfo getClonedOrderLinesInfo(final org.compiere.model.I_C_Order targetOrder)
	{
		return InterfaceWrapperHelper.getDynAttribute(targetOrder, DYNATTR_ClonedOrderLinesInfo);
	}

	private I_C_Order getTargetOrder() {return Check.assumeNotNull(getParentModel(I_C_Order.class), "target order is not null");}

	private int getOrCloneOrderCompensationGroup(final int orderCompensationGroupId)
	{
		if (orderCompensationGroupId <= 0)
		{
			return -1;
		}

		final I_C_Order toOrder = getTargetOrder();
		Map<Integer, Integer> orderCompensationGroupIdsMap = InterfaceWrapperHelper.getDynAttribute(toOrder, DYNATTR_OrderCompensationGroupIdsMap);
		if (orderCompensationGroupIdsMap == null)
		{
			orderCompensationGroupIdsMap = new HashMap<>();
			InterfaceWrapperHelper.setDynAttribute(toOrder, DYNATTR_OrderCompensationGroupIdsMap, orderCompensationGroupIdsMap);
		}

		final int toOrderId = toOrder.getC_Order_ID();
		final int orderCompensationGroupIdNew = orderCompensationGroupIdsMap.computeIfAbsent(orderCompensationGroupId, k -> cloneOrderCompensationGroup(orderCompensationGroupId, toOrderId));
		return orderCompensationGroupIdNew;
	}

	private static int cloneOrderCompensationGroup(final int orderCompensationGroupId, final int toOrderId)
	{
		final I_C_Order_CompensationGroup orderCompensationGroup = load(orderCompensationGroupId, I_C_Order_CompensationGroup.class);
		final I_C_Order_CompensationGroup orderCompensationGroupNew = newInstance(I_C_Order_CompensationGroup.class);
		InterfaceWrapperHelper.copyValues(orderCompensationGroup, orderCompensationGroupNew);
		orderCompensationGroupNew.setC_Order_ID(toOrderId);
		orderCompensationGroupNew.setPP_Product_BOM_ID(-1); // don't copy the Quotation BOM; another one has to be created
		InterfaceWrapperHelper.save(orderCompensationGroupNew);
		return orderCompensationGroupNew.getC_Order_CompensationGroup_ID();
	}

	@Override
	public List<CopyRecordSupportTableInfo> getSuggestedChildren(final PO po, final List<CopyRecordSupportTableInfo> suggestedChildren)
	{
		return ImmutableList.of();
	}

	//
	//
	//

	static class ClonedOrderLinesInfo
	{
		private final HashMap<OrderLineId, OrderLineId> original2targetOrderLineIds = new HashMap<>();

		public void addOriginalToClonedOrderLineMapping(@NonNull final OrderLineId originalOrderLineId, @NonNull final OrderLineId targetOrderLineId)
		{
			original2targetOrderLineIds.put(originalOrderLineId, targetOrderLineId);
		}

		public OrderLineId getTargetOrderLineId(@NonNull final OrderLineId originalOrderLineId)
		{
			final OrderLineId targetOrderLineId = original2targetOrderLineIds.get(originalOrderLineId);
			if (targetOrderLineId == null)
			{
				throw new AdempiereException("No target order line found for " + originalOrderLineId);
			}
			return targetOrderLineId;
		}
	}
}
