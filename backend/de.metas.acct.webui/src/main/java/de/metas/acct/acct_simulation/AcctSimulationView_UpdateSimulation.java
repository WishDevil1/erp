package de.metas.acct.acct_simulation;

class AcctSimulationView_UpdateSimulation extends AcctSimulationViewBasedAction
{
	@Override
	protected String doIt()
	{
		getView().updateSimulation();
		return MSG_OK;
	}
}
