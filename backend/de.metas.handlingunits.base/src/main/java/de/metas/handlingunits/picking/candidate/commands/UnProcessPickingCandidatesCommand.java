package de.metas.handlingunits.picking.candidate.commands;

import com.google.common.collect.ImmutableSet;
import de.metas.handlingunits.HuId;
import de.metas.handlingunits.IHUContext;
import de.metas.handlingunits.IHUContextFactory;
import de.metas.handlingunits.IHandlingUnitsBL;
import de.metas.handlingunits.allocation.impl.AllocationUtils;
import de.metas.handlingunits.allocation.impl.HUListAllocationSourceDestination;
import de.metas.handlingunits.allocation.impl.HULoader;
import de.metas.handlingunits.model.I_M_HU;
import de.metas.handlingunits.picking.PickingCandidate;
import de.metas.handlingunits.picking.PickingCandidateId;
import de.metas.handlingunits.picking.PickingCandidateRepository;
import de.metas.handlingunits.shipmentschedule.api.IHUShipmentScheduleBL;
import de.metas.inoutcandidate.ShipmentScheduleId;
import de.metas.inoutcandidate.model.I_M_ShipmentSchedule;
import de.metas.product.ProductId;
import de.metas.quantity.Quantity;
import de.metas.util.Check;
import de.metas.util.Services;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import org.adempiere.ad.trx.api.ITrxManager;
import org.adempiere.exceptions.AdempiereException;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class UnProcessPickingCandidatesCommand
{
	private final ITrxManager trxManager = Services.get(ITrxManager.class);
	private final IHUContextFactory huContextFactory = Services.get(IHUContextFactory.class);
	private final IHandlingUnitsBL handlingUnitsBL = Services.get(IHandlingUnitsBL.class);
	private final IHUShipmentScheduleBL huShipmentScheduleBL = Services.get(IHUShipmentScheduleBL.class);
	private final PickingCandidateRepository pickingCandidateRepository;

	private final ImmutableSet<PickingCandidateId> pickingCandidateIds;

	private final HashMap<ShipmentScheduleId, I_M_ShipmentSchedule> shipmentSchedulesCache = new HashMap<>();

	@Builder
	private UnProcessPickingCandidatesCommand(
			@NonNull final PickingCandidateRepository pickingCandidateRepository,
			@NonNull @Singular final Set<PickingCandidateId> pickingCandidateIds)
	{
		Check.assumeNotEmpty(pickingCandidateIds, "pickingCandidateIds is not empty");
		this.pickingCandidateRepository = pickingCandidateRepository;
		this.pickingCandidateIds = ImmutableSet.copyOf(pickingCandidateIds);
	}

	public UnProcessPickingCandidatesResult execute()
	{
		final List<PickingCandidate> pickingCandidates = pickingCandidateRepository.getByIds(pickingCandidateIds);
		pickingCandidates.forEach(this::assertEligibleForProcessing);

		trxManager.runInThreadInheritedTrx(() -> pickingCandidates.forEach(this::processInTrx));

		return UnProcessPickingCandidatesResult.builder()
				.pickingCandidates(pickingCandidates)
				.build();
	}

	private void assertEligibleForProcessing(final PickingCandidate pickingCandidate)
	{
		pickingCandidate.assertProcessed();
	}

	private void processInTrx(@NonNull final PickingCandidate pickingCandidate)
	{
		if (pickingCandidate.isRejectedToPick())
		{
			// TODO: impl
			throw new AdempiereException("Unprocessing not supported");
		}
		else if (pickingCandidate.getPickFrom().isPickFromHU())
		{
			processInTrx_unpackHU(pickingCandidate);
		}
		else if (pickingCandidate.getPickFrom().isPickFromPickingOrder())
		{
			// TODO: impl
			throw new AdempiereException("Unprocessing not supported");
		}
		else
		{
			throw new AdempiereException("Unknow " + pickingCandidate.getPickFrom());
		}
	}

	private void processInTrx_unpackHU(@NonNull final PickingCandidate pickingCandidate)
	{
		final HuId packedToHUId = pickingCandidate.getPackedToHuId();
		if (packedToHUId == null)
		{
			return;
		}

		final HuId pickFromHUId = pickingCandidate.getPickFrom().getHuId();
		final I_M_HU pickFromHU = handlingUnitsBL.getById(pickFromHUId);

		//
		// Case: When PickFrom HU was destroyed
		// * replace the pick From HU with the actually packed HU
		final boolean usePackedHUIdAsPickFrom;
		if (handlingUnitsBL.isDestroyed(pickFromHU))
		{
			usePackedHUIdAsPickFrom = true;
		}
		//
		// Case: When PickFrom HU was used as the PackTo HU
		else if(HuId.equals(pickFromHUId, packedToHUId))
		{
			usePackedHUIdAsPickFrom = true;
		}
		//
		// Case: When Pick From HU is not destroyed
		// * just move back the products from our packed HU back to pick From HU
		else
		{
			final ProductId productId = getProductId(pickingCandidate);
			final Quantity qtyPicked = pickingCandidate.getQtyPicked();
			final PickingCandidateId pickingCandidateId = Objects.requireNonNull(pickingCandidate.getId());

			final IHUContext huContext = huContextFactory.createMutableHUContextForProcessing();

			HULoader.builder()
					.source(HUListAllocationSourceDestination.ofHUId(packedToHUId).setDestroyEmptyHUs(true))
					.destination(HUListAllocationSourceDestination.of(pickFromHU))
					.allowPartialUnloads(false)
					.allowPartialLoads(false)
					.forceLoad(true)
					.load(AllocationUtils.builder()
							.setHUContext(huContext)
							.setProduct(productId)
							.setQuantity(qtyPicked)
							.setFromReferencedTableRecord(pickingCandidateId.toTableRecordReference())
							.setForceQtyAllocation(true)
							.create());

			// NOTE: don't expect packedTOHUId to be empty/destroyed because it might be we packed several lines in same box

			usePackedHUIdAsPickFrom = false;
		}

		pickingCandidate.changeStatusToDraft(usePackedHUIdAsPickFrom);
		pickingCandidateRepository.save(pickingCandidate);

		huShipmentScheduleBL.deleteByTopLevelHUAndShipmentScheduleId(packedToHUId, pickingCandidate.getShipmentScheduleId());
	}

	@NonNull
	private ProductId getProductId(final PickingCandidate pickingCandidate)
	{
		final I_M_ShipmentSchedule shipmentSchedule = getShipmentScheduleById(pickingCandidate.getShipmentScheduleId());
		return ProductId.ofRepoId(shipmentSchedule.getM_Product_ID());
	}

	private I_M_ShipmentSchedule getShipmentScheduleById(@NonNull final ShipmentScheduleId shipmentScheduleId)
	{
		return shipmentSchedulesCache.computeIfAbsent(shipmentScheduleId, huShipmentScheduleBL::getById);
	}
}
