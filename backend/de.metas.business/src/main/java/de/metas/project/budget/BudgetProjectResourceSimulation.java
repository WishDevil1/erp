package de.metas.project.budget;

import de.metas.calendar.simulation.SimulationPlanId;
import de.metas.calendar.util.CalendarDateRange;
import de.metas.util.Check;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.Nullable;

@Value
@Builder(toBuilder = true)
public class BudgetProjectResourceSimulation
{
	@NonNull BudgetProjectAndResourceId projectAndResourceId;

	@NonNull CalendarDateRange dateRange;

	public BudgetProjectResourceId getProjectResourceId() {return getProjectAndResourceId().getProjectResourceId();}

	public static BudgetProjectResourceSimulation reduce(@Nullable BudgetProjectResourceSimulation simulation, @NonNull UpdateRequest updateRequest)
	{
		final BudgetProjectResourceSimulationBuilder builder;
		if (simulation == null)
		{
			builder = builder()
					.projectAndResourceId(updateRequest.getProjectAndResourceId());
		}
		else
		{
			Check.assumeEquals(simulation.getProjectAndResourceId(), updateRequest.getProjectAndResourceId(), "expected same projectAndResourceId: {}, {}", simulation, updateRequest);

			builder = simulation.toBuilder();
		}

		return builder
				.dateRange(updateRequest.getDateRange())
				.build();

	}

	public BudgetProjectResource applyOn(@NonNull final BudgetProjectResource resource)
	{
		Check.assumeEquals(resource.getProjectAndResourceId(), projectAndResourceId, "expected same project and projectResourceId: {}, {}", resource, this);

		return resource.toBuilder()
				.dateRange(dateRange)
				.build();
	}

	//
	//
	//

	@Value
	@Builder
	public static class UpdateRequest
	{
		@NonNull SimulationPlanId simulationId;
		@NonNull BudgetProjectAndResourceId projectAndResourceId;

		@NonNull CalendarDateRange dateRange;
	}
}
