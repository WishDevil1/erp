package de.metas.acct.acct_simulation;

import de.metas.ui.web.process.adprocess.ViewBasedProcessTemplate;

abstract class AcctSimulationViewBasedAction extends ViewBasedProcessTemplate
{

	@Override
	protected AcctSimulationView getView()
	{
		return AcctSimulationView.cast(super.getView());
	}
}
