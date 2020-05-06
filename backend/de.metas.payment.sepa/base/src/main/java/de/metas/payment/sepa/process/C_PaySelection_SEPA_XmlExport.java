package de.metas.payment.sepa.process;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.InterfaceWrapperHelper;

import de.metas.document.engine.DocStatus;
import de.metas.i18n.IMsgBL;
import de.metas.payment.sepa.api.ISEPADocumentBL;
import de.metas.payment.sepa.api.SEPACreditTransferXML;
import de.metas.payment.sepa.interfaces.I_C_PaySelection;
import de.metas.payment.sepa.model.I_SEPA_Export;
import de.metas.process.IProcessPrecondition;
import de.metas.process.IProcessPreconditionsContext;
import de.metas.process.JavaProcess;
import de.metas.process.ProcessPreconditionsResolution;
import de.metas.report.ReportResultData;
import de.metas.util.Services;
import de.metas.util.time.SystemTime;

/**
 * Process that creates SEPA xmls in 3 steps:
 *
 * Creates SEPA_Export/SEPA_Export_Lines from C_PaySelection/C_PaySelectionLines Create SEPADocument/lines from the export Marshals the lines into an XML
 *
 * @author ad
 *
 */
public class C_PaySelection_SEPA_XmlExport
		extends JavaProcess
		implements IProcessPrecondition
{
	private static final String MSG_NO_SELECTION = "de.metas.payment.sepa.noPaySelection";

	//
	// services
	private final IMsgBL msgBL = Services.get(IMsgBL.class);
	private final ISEPADocumentBL sepaDocumentBL = Services.get(ISEPADocumentBL.class);

	@Override
	public ProcessPreconditionsResolution checkPreconditionsApplicable(final IProcessPreconditionsContext context)
	{
		if (context == null)
		{
			return ProcessPreconditionsResolution.rejectWithInternalReason("Process " + C_PaySelection_SEPA_XmlExport.class + "only works with context != null");
		}

		final String tableName = context.getTableName();
		if (!I_C_PaySelection.Table_Name.equals(tableName))
		{
			return ProcessPreconditionsResolution.rejectWithInternalReason("Process " + C_PaySelection_SEPA_XmlExport.class + " only works for C_PaySelection");
		}

		final I_C_PaySelection paySelectionHeader = context.getSelectedModel(I_C_PaySelection.class);
		if (paySelectionHeader == null)
		{
			return ProcessPreconditionsResolution.rejectWithInternalReason("No payment selection");
		}

		final DocStatus docStatus = DocStatus.ofNullableCodeOrUnknown(paySelectionHeader.getDocStatus());
		if (!docStatus.isCompletedOrClosed())
		{
			return ProcessPreconditionsResolution.rejectWithInternalReason(
					"Process " + C_PaySelection_SEPA_XmlExport.class + " only works for completed or closed C_PaySelections; C_PaySelection_ID=" + paySelectionHeader.getC_PaySelection_ID());
		}

		return ProcessPreconditionsResolution.accept();
	}

	@Override
	protected String doIt()
	{
		final int recordId = getRecord_ID();
		if (recordId <= 0)
		{
			throw new AdempiereException(msgBL.getMsg(getCtx(), MSG_NO_SELECTION));
		}

		final I_C_PaySelection paySelection = InterfaceWrapperHelper.create(getCtx(), recordId, I_C_PaySelection.class, getTrxName());

		//
		// First, generate the SEPA export as an intermediary step, to use the old framework.
		final I_SEPA_Export sepaExport = sepaDocumentBL.createSEPAExportFromPaySelection(paySelection);

		//
		// After the export header and lines have been created, marshal the document.
		final SEPACreditTransferXML xml = sepaDocumentBL.exportCreditTransferXML(sepaExport);

		paySelection.setLastExport(SystemTime.asTimestamp());
		paySelection.setLastExportBy_ID(getAD_User_ID());
		InterfaceWrapperHelper.saveRecord(paySelection);

		getResult().setReportData(ReportResultData.builder()
				.reportFilename(xml.getFilename())
				.reportContentType(xml.getContentType())
				.reportData(xml.getContent())
				.build());

		return MSG_OK;
	}
}
