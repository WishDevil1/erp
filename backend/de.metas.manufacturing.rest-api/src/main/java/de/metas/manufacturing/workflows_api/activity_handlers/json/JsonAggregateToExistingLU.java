package de.metas.manufacturing.workflows_api.activity_handlers.json;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class JsonAggregateToExistingLU
{
	@NonNull String huBarcode;
	int tuPIItemProductId;
}
