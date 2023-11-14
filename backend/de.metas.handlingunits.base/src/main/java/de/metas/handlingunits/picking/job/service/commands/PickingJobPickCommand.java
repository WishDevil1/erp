package de.metas.handlingunits.picking.job.service.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.metas.handlingunits.HuId;
import de.metas.handlingunits.IHUCapacityBL;
import de.metas.handlingunits.IHUContext;
import de.metas.handlingunits.IHUPIItemProductBL;
import de.metas.handlingunits.IHUStatusBL;
import de.metas.handlingunits.IHandlingUnitsBL;
import de.metas.handlingunits.IHandlingUnitsDAO;
import de.metas.handlingunits.allocation.transfer.HUTransformService;
import de.metas.handlingunits.attribute.storage.IAttributeStorage;
import de.metas.handlingunits.attribute.weightable.IWeightable;
import de.metas.handlingunits.attribute.weightable.Weightables;
import de.metas.handlingunits.generichumodel.HUType;
import de.metas.handlingunits.model.I_M_HU;
import de.metas.handlingunits.model.X_M_HU;
import de.metas.handlingunits.picking.PackToSpec;
import de.metas.handlingunits.picking.PickFrom;
import de.metas.handlingunits.picking.PickingCandidateId;
import de.metas.handlingunits.picking.PickingCandidateService;
import de.metas.handlingunits.picking.QtyRejectedReasonCode;
import de.metas.handlingunits.picking.QtyRejectedWithReason;
import de.metas.handlingunits.picking.candidate.commands.PackToHUsProducer;
import de.metas.handlingunits.picking.candidate.commands.PickHUResult;
import de.metas.handlingunits.picking.candidate.commands.ProcessPickingCandidatesRequest;
import de.metas.handlingunits.picking.job.model.HUInfo;
import de.metas.handlingunits.picking.job.model.LocatorInfo;
import de.metas.handlingunits.picking.job.model.PickingJob;
import de.metas.handlingunits.picking.job.model.PickingJobLine;
import de.metas.handlingunits.picking.job.model.PickingJobLineId;
import de.metas.handlingunits.picking.job.model.PickingJobStep;
import de.metas.handlingunits.picking.job.model.PickingJobStepId;
import de.metas.handlingunits.picking.job.model.PickingJobStepPickFrom;
import de.metas.handlingunits.picking.job.model.PickingJobStepPickFromKey;
import de.metas.handlingunits.picking.job.model.PickingJobStepPickedTo;
import de.metas.handlingunits.picking.job.model.PickingJobStepPickedToHU;
import de.metas.handlingunits.picking.job.repository.PickingJobRepository;
import de.metas.handlingunits.picking.requests.PickRequest;
import de.metas.handlingunits.qrcodes.leich_und_mehl.LMQRCode;
import de.metas.handlingunits.qrcodes.model.HUQRCode;
import de.metas.handlingunits.qrcodes.model.IHUQRCode;
import de.metas.handlingunits.qrcodes.service.HUQRCodesService;
import de.metas.handlingunits.storage.IHUStorageFactory;
import de.metas.inout.ShipmentScheduleId;
import de.metas.picking.api.PickingSlotId;
import de.metas.product.ProductId;
import de.metas.quantity.Quantity;
import de.metas.quantity.Quantitys;
import de.metas.uom.IUOMConversionBL;
import de.metas.util.Check;
import de.metas.util.Services;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.adempiere.ad.trx.api.ITrxManager;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.PlainContextAware;
import org.adempiere.warehouse.LocatorId;
import org.adempiere.warehouse.api.IWarehouseBL;
import org.compiere.model.I_C_UOM;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class PickingJobPickCommand
{
	//
	// Services
	@NonNull private final ITrxManager trxManager = Services.get(ITrxManager.class);
	@NonNull private final IHandlingUnitsBL handlingUnitsBL = Services.get(IHandlingUnitsBL.class);
	@NonNull private final IWarehouseBL warehouseBL = Services.get(IWarehouseBL.class);
	@NonNull private final IUOMConversionBL uomConversionBL = Services.get(IUOMConversionBL.class);
	@NonNull private final IHandlingUnitsDAO handlingUnitsDAO = Services.get(IHandlingUnitsDAO.class);
	@NonNull private final IHUStatusBL huStatusBL = Services.get(IHUStatusBL.class);
	@NonNull private final HUTransformService huTransformService = HUTransformService.newInstance();
	@NonNull private final PickingJobRepository pickingJobRepository;
	@NonNull private final PickingCandidateService pickingCandidateService;
	@NonNull private final HUQRCodesService huQRCodesService;
	@NonNull private final PackToHUsProducer packToHUsProducer;

	//
	// Params
	@NonNull private final PickingJobLineId lineId;
	@NonNull private final PickingJobStepPickFromKey stepPickFromKey;
	@Nullable private final IHUQRCode pickFromHUQRCode;
	@NonNull private final Quantity qtyToPick;
	@Nullable private final QtyRejectedWithReason qtyRejected;
	@Nullable private final Quantity catchWeight;
	private final boolean isPickWholeTU;

	//
	// State
	@NonNull private PickingJob pickingJob;
	@Nullable private PickingJobStepId stepId;

	@Builder
	private PickingJobPickCommand(
			final @NonNull PickingJobRepository pickingJobRepository,
			final @NonNull PickingCandidateService pickingCandidateService,
			final @NonNull HUQRCodesService huQRCodesService,
			//
			final @NonNull PickingJob pickingJob,
			final @NonNull PickingJobLineId pickingJobLineId,
			final @Nullable PickingJobStepId pickingJobStepId,
			final @Nullable PickingJobStepPickFromKey pickFromKey,
			final @Nullable IHUQRCode pickFromHUQRCode,
			final @NonNull BigDecimal qtyToPickBD,
			final @Nullable BigDecimal qtyRejectedBD,
			final @Nullable QtyRejectedReasonCode qtyRejectedReasonCode,
			final @Nullable BigDecimal catchWeightBD,
			final boolean isPickWholeTU)
	{
		Check.assumeGreaterOrEqualToZero(qtyToPickBD, "qtyToPickBD");
		validateCatchWeight(catchWeightBD, pickFromHUQRCode);

		this.pickingJobRepository = pickingJobRepository;
		this.pickingCandidateService = pickingCandidateService;
		this.huQRCodesService = huQRCodesService;
		this.packToHUsProducer = PackToHUsProducer.builder()
				.handlingUnitsBL(handlingUnitsBL)
				.huPIItemProductBL(Services.get(IHUPIItemProductBL.class))
				.huCapacityBL(Services.get(IHUCapacityBL.class))
				.build();

		this.pickingJob = pickingJob;
		this.lineId = pickingJobLineId;
		this.stepId = pickingJobStepId;
		this.stepPickFromKey = pickFromKey != null ? pickFromKey : PickingJobStepPickFromKey.MAIN;
		this.pickFromHUQRCode = pickFromHUQRCode;

		final PickingJobLine line = pickingJob.getLineById(lineId);
		final PickingJobStep step = pickingJobStepId != null ? pickingJob.getStepById(pickingJobStepId) : null;
		final I_C_UOM uom = line.getUOM();
		this.isPickWholeTU = isPickWholeTU;
		this.qtyToPick =  Quantity.of(qtyToPickBD, uom);

		if (qtyRejectedReasonCode != null)
		{
			final Quantity qtyRejected = qtyRejectedBD != null
					? Quantity.of(qtyRejectedBD, uom)
					: computeQtyRejected(line, step, this.stepPickFromKey, this.qtyToPick);

			this.qtyRejected = QtyRejectedWithReason.of(qtyRejected, qtyRejectedReasonCode);
		}
		else
		{
			this.qtyRejected = null;
		}

		this.catchWeight = line.getCatchUomId() != null && catchWeightBD != null
				? Quantitys.create(catchWeightBD, line.getCatchUomId())
				: null;
		if (this.catchWeight != null && !this.catchWeight.isPositive())
		{
			throw new AdempiereException("Catch Weight shall be positive");
		}
	}

	private static Quantity computeQtyRejected(
			@NonNull final PickingJobLine line,
			@Nullable final PickingJobStep step,
			@Nullable final PickingJobStepPickFromKey pickFromKey,
			@NonNull final Quantity qtyPicked)
	{
		if (step != null)
		{
			if (pickFromKey == null || pickFromKey.isMain())
			{
				return step.getQtyToPick().subtract(qtyPicked);
			}
			else
			{
				// NOTE: because, in case of alternatives, we don't know which is the qty scheduled to pick
				// we cannot calculate the qtyRejected
				throw new AdempiereException("Cannot calculate QtyRejected in case of alternatives");
			}
		}
		else
		{
			return line.getQtyToPick().subtract(qtyPicked);
		}
	}

	public PickingJob execute()
	{
		pickingJob.assertNotProcessed();
		return trxManager.callInThreadInheritedTrx(this::executeInTrx);
	}

	private PickingJob executeInTrx()
	{
		createStepIfNeeded();

		final ImmutableList<PickedToHU> pickedHUs = createAndProcessPickingCandidate();

		final PickingJobStepId stepId = Check.assumeNotNull(this.stepId, "step exists");
		pickingJob = pickingJob.withChangedStep(
				stepId,
				step -> updateStepFromPickingCandidate(step, pickedHUs));

		pickingJobRepository.save(pickingJob);

		return pickingJob;
	}

	private void createStepIfNeeded()
	{
		if (stepId != null)
		{
			return;
		}

		final PickingJobStepId newStepId = pickingJobRepository.newPickingJobStepId();

		final PickingJobLine line = pickingJob.getLineById(lineId);
		final HUQRCode pickFromHUQRCode = getPickFromHUQRCode();
		final HuId pickFromHUId = huQRCodesService.getHuIdByQRCode(pickFromHUQRCode);
		final LocatorId pickFromLocatorId = handlingUnitsBL.getLocatorId(pickFromHUId);

		pickingJob = pickingJob.withNewStep(
				PickingJob.AddStepRequest.builder()
						.isGeneratedOnFly(true)
						.newStepId(newStepId)
						.lineId(line.getId())
						.qtyToPick(qtyToPick)
						.pickFromLocator(LocatorInfo.builder()
								.id(pickFromLocatorId)
								.caption(warehouseBL.getLocatorNameById(pickFromLocatorId))
								.build())
						.pickFromHU(HUInfo.builder()
								.id(pickFromHUId)
								.qrCode(pickFromHUQRCode)
								.build())
						.packToSpec(PackToSpec.VIRTUAL)
						.build()
		);

		this.stepId = newStepId;
	}

	private HUQRCode getPickFromHUQRCode()
	{
		final IHUQRCode pickFromHUQRCode = Check.assumeNotNull(this.pickFromHUQRCode, "HU QR code shall be provided");

		if (pickFromHUQRCode instanceof HUQRCode)
		{
			return (HUQRCode)pickFromHUQRCode;
		}
		else if (pickFromHUQRCode instanceof LMQRCode)
		{
			final LMQRCode lmQRCode = (LMQRCode)pickFromHUQRCode;
			final String lotNumber = lmQRCode.getLotNumber();
			return handlingUnitsBL.getFirstHuIdByExternalLotNo(lmQRCode.getLotNumber())
					.map(huQRCodesService::getQRCodeByHuId)
					.orElseThrow(() -> new AdempiereException("No HU associated with external lot number: " + lotNumber));
		}
		else
		{
			throw new AdempiereException("HU QR code not supported: " + pickFromHUQRCode);
		}
	}

	private ImmutableList<PickedToHU> createAndProcessPickingCandidate()
	{
		final ImmutableList<PickedToHU> pickedToHUs = splitOutPickToHUs();
		if (pickedToHUs.isEmpty())
		{
			return ImmutableList.of();
		}

		final ShipmentScheduleId shipmentScheduleId = getShipmentScheduleId();
		final PickingSlotId pickingSlotId = pickingJob.getPickingSlotId().orElse(null);
		for (final PickedToHU pickedToHU : pickedToHUs)
		{
			final PickHUResult pickResult = pickingCandidateService.pickHU(PickRequest.builder()
					.shipmentScheduleId(shipmentScheduleId)
					.pickFrom(PickFrom.ofHuId(pickedToHU.getActuallyPickedToHUId()))
					.packToSpec(pickedToHU.getPickToSpecUsed())
					.qtyToPick(pickedToHU.getQtyPicked())
					.pickingSlotId(pickingSlotId)
					.autoReview(true)
					.build());

			pickedToHU.setPickingCandidateId(pickResult.getPickingCandidateId());
		}

		pickingCandidateService.process(ProcessPickingCandidatesRequest.builder()
				.pickingCandidateIds(extractPickingCandidateIds(pickedToHUs))
				.alwaysPackEachCandidateInItsOwnHU(true)
				.build());

		return pickedToHUs;
	}

	private ShipmentScheduleId getShipmentScheduleId()
	{
		if (stepId != null)
		{
			return pickingJob.getStepById(stepId).getShipmentScheduleId();
		}
		else
		{
			return Check.assumeNotNull(pickingJob.getLineById(lineId).getShipmentScheduleId(), "line has shipment schedule set");
		}
	}

	private ImmutableList<PickedToHU> splitOutPickToHUs()
	{
		if (qtyToPick.isZero())
		{
			return ImmutableList.of();
		}

		final PickingJobStepId stepId = Check.assumeNotNull(this.stepId, "step exists");
		final PickingJobStep step = pickingJob.getStepById(stepId);
		step.getPickFrom(stepPickFromKey).assertNotPicked();

		final ProductId productId = step.getProductId();
		final PackToSpec packToSpec = step.getPackToSpec();
		final PickingJobStepPickFrom pickFrom = step.getPickFrom(stepPickFromKey);
		final HUInfo pickFromHU = pickFrom.getPickFromHU();
		final LocatorId pickFromLocatorId = pickFrom.getPickFromLocatorId();

		final PackToHUsProducer.PackToInfo packToInfo = packToHUsProducer.extractPackToInfo(
				packToSpec,
				pickingJob.getDeliveryBPLocationId(),
				pickFromLocatorId);

		trxManager.assertThreadInheritedTrxExists();
		final IHUContext huContext = handlingUnitsBL.createMutableHUContextForProcessing();

		final List<I_M_HU> packedHUs = isPickWholeTU
				? ImmutableList.of(pickWholeTU(productId, huContext, pickFromHU.getId()))
				: packToHUsProducer.packToHU(
				huContext,
				pickFromHU.getId(),
				packToInfo,
				productId,
				qtyToPick,
				lineId.toTableRecordReference(),
				true);

		if (packedHUs.isEmpty())
		{
			throw new AdempiereException("Cannot pack to HUs from " + pickFromHU + " using " + packToInfo + ", qtyToPick=" + qtyToPick);
		}
		else if (packedHUs.size() == 1)
		{
			final I_M_HU packedHU = packedHUs.get(0);
			updateHUWeightFromCatchWeight(packedHU, huContext, productId);

			final IHUStorageFactory huStorageFactory = huContext.getHUStorageFactory();
			final Quantity pickedQty = isPickWholeTU
					? huStorageFactory.getStorage(packedHU).getQuantity(productId).orElseThrow(() -> new AdempiereException("Qty not found!"))
					: qtyToPick;

			return ImmutableList.of(PickedToHU.builder()
											.pickedFromHU(pickFromHU)
											.pickFromLocatorId(pickFromLocatorId)
											.actuallyPickedToHUId(HuId.ofRepoId(packedHU.getM_HU_ID()))
											.pickToSpecUsed(PackToSpec.ofGenericPackingInstructionsId(handlingUnitsBL.getEffectivePackingInstructionsId(packedHU)))
											.qtyPicked(pickedQty)
											.build());
		}
		else
		{
			if (catchWeight != null)
			{
				throw new AdempiereException("Cannot apply catch weight when receiving more than one HU");
			}

			final IHUStorageFactory huStorageFactory = huContext.getHUStorageFactory();
			final ImmutableList.Builder<PickedToHU> result = ImmutableList.builder();
			for (final I_M_HU packedHU : packedHUs)
			{
				final Quantity qtyPicked = huStorageFactory.getStorage(packedHU).getQuantity(productId, qtyToPick.getUOM());
				result.add(PickedToHU.builder()
								   .pickedFromHU(pickFromHU)
								   .pickFromLocatorId(pickFromLocatorId)
								   .actuallyPickedToHUId(HuId.ofRepoId(packedHU.getM_HU_ID()))
								   .pickToSpecUsed(PackToSpec.ofGenericPackingInstructionsId(handlingUnitsBL.getEffectivePackingInstructionsId(packedHU)))
								   .qtyPicked(qtyPicked)
								   .build());
			}

			return result.build();
		}
	}

	private void updateHUWeightFromCatchWeight(final I_M_HU hu, final IHUContext huContext, final ProductId productId)
	{
		if (catchWeight == null)
		{
			return;
		}

		final IAttributeStorage huAttributes = huContext.getHUAttributeStorageFactory().getAttributeStorage(hu);
		huAttributes.setSaveOnChange(true);

		final IWeightable weightable = Weightables.wrap(huAttributes);
		final Quantity catchWeightConv = uomConversionBL.convertQuantityTo(catchWeight, productId, weightable.getWeightNetUOM());
		weightable.setWeightNet(catchWeightConv.toBigDecimal());
	}

	private PickingJobStep updateStepFromPickingCandidate(
			@NonNull final PickingJobStep step,
			@NonNull final ImmutableList<PickedToHU> pickedHUs)
	{
		final PickingJobStepPickedTo picked = toPickingJobStepPickedTo(pickedHUs);
		return step.reduceWithPickedEvent(stepPickFromKey, picked);
	}

	private PickingJobStepPickedTo toPickingJobStepPickedTo(@NonNull final ImmutableList<PickedToHU> pickedHUs)
	{
		return PickingJobStepPickedTo.builder()
				.qtyRejected(qtyRejected)
				.actualPickedHUs(pickedHUs.stream()
						.map(PickingJobPickCommand::toPickingJobStepPickedToHU)
						.collect(ImmutableList.toImmutableList())
				)
				.build();
	}

	private I_M_HU pickWholeTU(
			@NonNull final ProductId productId,
			@NonNull final IHUContext huContext,
			@NonNull final HuId pickFromHUId)
	{
		Check.assume(isPickWholeTU, "isPickWholeTU must be true");

		final I_M_HU pickFromHU = handlingUnitsBL.getById(pickFromHUId);

		if (handlingUnitsBL.isLoadingUnit(pickFromHU))
		{
			final I_M_HU firstIncludedTU = handlingUnitsDAO.retrieveIncludedHUs(pickFromHU)
					.stream()
					.filter(huStatusBL::isStatusActive)
					.filter(includedHU -> HUType.TransportUnit == HUType.ofCodeOrNull(handlingUnitsBL.getEffectivePIVersion(includedHU).getHU_UnitType()))
					.filter(includedTU -> huContext.getHUStorageFactory().isSingleProductStorageMatching(includedTU, productId))
					.findFirst()
					.orElseThrow(() -> new AdempiereException("No included HU found for LU=" + pickFromHU.getM_HU_ID()));

			final I_M_HU splitOutTURecord = huTransformService.splitOutTURecord(firstIncludedTU);
			handlingUnitsBL.setHUStatus(splitOutTURecord, PlainContextAware.newWithThreadInheritedTrx(), X_M_HU.HUSTATUS_Picked);

			return splitOutTURecord;
		}
		else if (HUType.TransportUnit == HUType.ofCodeOrNull(handlingUnitsBL.getEffectivePIVersion(pickFromHU).getHU_UnitType()))
		{
			final I_M_HU splitOutTURecord = huTransformService.splitOutTURecord(pickFromHU);
			handlingUnitsBL.setHUStatus(splitOutTURecord, PlainContextAware.newWithThreadInheritedTrx(), X_M_HU.HUSTATUS_Picked);

			return splitOutTURecord;
		}
		else
		{
			throw new AdempiereException("CUs are not supported when isPickWholeTU=true");
		}
	}

	private static PickingJobStepPickedToHU toPickingJobStepPickedToHU(final PickedToHU pickedHU)
	{
		return PickingJobStepPickedToHU.builder()
				.pickFromHUId(pickedHU.getPickedFromHU().getId())
				.actualPickedHUId(pickedHU.getActuallyPickedToHUId())
				.qtyPicked(pickedHU.getQtyPicked())
				.pickingCandidateId(Objects.requireNonNull(pickedHU.getPickingCandidateId()))
				.build();
	}

	private static ImmutableSet<PickingCandidateId> extractPickingCandidateIds(final ImmutableList<PickedToHU> pickedToHUs)
	{
		return pickedToHUs
				.stream()
				.map(PickedToHU::getPickingCandidateId)
				.peek(Objects::requireNonNull)
				.collect(ImmutableSet.toImmutableSet());
	}

	private static void validateCatchWeight(final @Nullable BigDecimal catchWeightBD, @Nullable final IHUQRCode pickFromHUQRCode)
	{
		if (!(pickFromHUQRCode instanceof LMQRCode))
		{
			return;
		}

		if (catchWeightBD == null)
		{
			throw new AdempiereException("catchWeightBD must be present when picking via LMQRCode")
					.appendParametersToMessage()
					.setParameter("LMQRCode", pickFromHUQRCode)
					.setParameter("catchWeightBD", catchWeightBD);
		}

		if (((LMQRCode)pickFromHUQRCode).getWeight().compareTo(catchWeightBD) != 0)
		{
			throw new AdempiereException("catchWeightBD must match the LMQRCode.Weight")
					.appendParametersToMessage()
					.setParameter("LMQRCode", pickFromHUQRCode)
					.setParameter("catchWeightBD", catchWeightBD);
		}
	}

	//
	// -------------------------------------
	//
	@Data
	@Builder
	private static class PickedToHU
	{
		@NonNull final HUInfo pickedFromHU;
		@NonNull final LocatorId pickFromLocatorId;

		@NonNull final PackToSpec pickToSpecUsed;
		@NonNull final HuId actuallyPickedToHUId;

		@NonNull final Quantity qtyPicked;

		@Nullable PickingCandidateId pickingCandidateId;
	}
}
