package de.metas.handlingunits.picking.plan.generator.pickFromHUs;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode
@ToString
public class AlternativePickFromsList
{
	public static final AlternativePickFromsList EMPTY = new AlternativePickFromsList(ImmutableList.of());

	private final ImmutableList<AlternativePickFrom> pickFroms;

	private AlternativePickFromsList(@NonNull final List<AlternativePickFrom> pickFroms)
	{
		this.pickFroms = ImmutableList.copyOf(pickFroms);
	}

	public static AlternativePickFromsList ofList(@NonNull final List<AlternativePickFrom> list)
	{
		return !list.isEmpty() ? new AlternativePickFromsList(list) : EMPTY;
	}
}
