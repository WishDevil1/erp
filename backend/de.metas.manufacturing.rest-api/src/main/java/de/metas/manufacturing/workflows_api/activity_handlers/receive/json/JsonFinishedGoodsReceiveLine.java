package de.metas.manufacturing.workflows_api.activity_handlers.receive.json;

import de.metas.manufacturing.workflows_api.activity_handlers.issue.json.JsonHazardSymbol;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
@Jacksonized
public class JsonFinishedGoodsReceiveLine
{
	@NonNull String id;

	@NonNull String productName;
	@NonNull String uom;
	@NonNull List<JsonHazardSymbol> hazardSymbols;

	@NonNull BigDecimal qtyToReceive;
	@NonNull BigDecimal qtyReceived;

	@Nullable JsonHUQRCodeTarget currentReceivingHU;

	@NonNull JsonNewLUTargetsList availableReceivingTargets;
}
