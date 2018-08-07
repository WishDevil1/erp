/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution *
 * This program is free software; you can redistribute it and/or modify it *
 * under the terms version 2 of the GNU General Public License as published *
 * by the Free Software Foundation. This program is distributed in the hope *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. *
 * See the GNU General Public License for more details. *
 * You should have received a copy of the GNU General Public License along *
 * with this program; if not, write to the Free Software Foundation, Inc., *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA. *
 * For the text or an alternative of this public license, you may reach us * *
 *****************************************************************************/
package org.compiere.report;

import org.adempiere.ad.service.ITaskExecutorService;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.util.Check;
import org.adempiere.util.FileUtils;
import org.adempiere.util.Services;
import org.adempiere.util.lang.impl.TableRecordReference;
import org.compiere.print.JRReportViewerProvider;
import org.compiere.report.IJasperServiceRegistry.ServiceType;
import org.compiere.util.Ini;
import org.compiere.util.Util;
import org.slf4j.Logger;

import com.google.common.base.MoreObjects;
import com.google.common.io.Files;

import de.metas.adempiere.report.jasper.OutputType;
import de.metas.adempiere.report.jasper.client.JRClient;
import de.metas.document.engine.IDocument;
import de.metas.document.engine.IDocumentBL;
import de.metas.logging.LogManager;
import de.metas.process.ClientOnlyProcess;
import de.metas.process.JavaProcess;
import de.metas.process.ProcessExecutionResult;
import de.metas.process.ProcessInfo;
import lombok.NonNull;
import net.sf.jasperreports.engine.JasperPrint;

/**
 * Jasper Report Process: this process is used on all AD_Processes which are about creating jasper reports.
 *
 * @author rlemeill originaly coming from an application note from compiere.co.uk --- Modifications: Allow Jasper
 *         Reports to be able to be run on VPN profile (i.e: no direct connection to DB). Implemented ClientProcess for
 *         it to run on client.
 * @author Ashley Ramdass
 * @author tsa
 */
@ClientOnlyProcess
public class ReportStarter extends JavaProcess
// implements IProcess
{
	// services
	private static final Logger log = LogManager.getLogger(ReportStarter.class);
	private static JRReportViewerProvider swingJRReportViewerProvider = new SwingJRViewerProvider();

	/**
	 * Start Jasper reporting process. Based on {@link ProcessInfo#isPrintPreview()}, it will:
	 * <ul>
	 * <li>directly print the report
	 * <li>will open the report viewer and it will display the report
	 * </ul>
	 */
	@Override
	protected String doIt() throws Exception
	{
		final ProcessInfo pi = getProcessInfo();

		final ReportPrintingInfo reportPrintingInfo = extractReportPrintingInfo(pi);

		//
		// Create report and print it directly
		if (!reportPrintingInfo.isPrintPreview())
		{
			if (reportPrintingInfo.isForceSync())
			{
				// gh #1160 if the caller want ou to execute synchronously, then do just that
				startProcess0(pi, reportPrintingInfo);
			}
			else
			{
				// task 08283: direct print can be done in background; no need to let the user wait for this
				Services.get(ITaskExecutorService.class).submit(
						() -> startProcess0(pi, reportPrintingInfo) //
						, ReportStarter.class.getSimpleName() //
				);
			}
		}
		//
		// Create report and preview
		else
		{
			startProcessPrintPreview(reportPrintingInfo);
		}
		return MSG_OK;
	}

	private static JRReportViewerProvider viewerProvider = null;

	/**
	 * Set jasper report viewer provider.
	 *
	 * @param provider
	 */
	public static void setNonSwingViewerProvider(@NonNull final JRReportViewerProvider provider)
	{
		viewerProvider = provider;
	}

	private void startProcessDirectPrint(final ReportPrintingInfo reportPrintingInfo)
	{
		final ProcessInfo pi = reportPrintingInfo.getProcessInfo();
		final JRClient jrClient = JRClient.get();
		final JasperPrint jasperPrint = jrClient.createJasperPrint(pi);
		log.info("ReportStarter.startProcess print report: {}", jasperPrint.getName());

		//
		// 08284: if we work without preview, use the mass printing framework
		final IJasperServiceRegistry jasperServiceFactory = Services.get(IJasperServiceRegistry.class);
		final IJasperService jasperService = jasperServiceFactory.getJasperService(ServiceType.MASS_PRINT_FRAMEWORK);
		final boolean displayPrintDialog = false;
		jasperService.print(jasperPrint, pi, displayPrintDialog);
	}

	private void startProcessPrintPreview(final ReportPrintingInfo reportPrintingInfo) throws Exception
	{
		final ProcessInfo processInfo = reportPrintingInfo.getProcessInfo();

		//
		// Get Jasper report viewer provider
		final JRReportViewerProvider jrReportViewerProvider = getJRReportViewerProviderOrNull();
		final OutputType desiredOutputType = jrReportViewerProvider == null ? null : jrReportViewerProvider.getDesiredOutputType();

		//
		// Based on reporting system type, determine: output type
		final ReportingSystemType reportingSystemType = reportPrintingInfo.getReportingSystemType();
		final OutputType outputType;
		switch (reportingSystemType)
		{
			//
			// Jasper reporting
			case Jasper:
				outputType = Util.coalesce(desiredOutputType, processInfo.getJRDesiredOutputType(), OutputType.PDF);
				break;

			//
			// Excel reporting
			case Excel:
				outputType = OutputType.XLS;
				break;

			default:
				throw new AdempiereException("Unknown " + ReportingSystemType.class + ": " + reportingSystemType);
		}

		//
		// Generate report data
		log.info("ReportStarter.startProcess run report: reportingSystemType={}, title={}, outputType={}", reportingSystemType, processInfo.getTitle(), outputType);
		final JRClient jrClient = JRClient.get();
		final byte[] reportData = jrClient.report(processInfo, outputType);

		//
		// Set report data to process execution result
		final ProcessExecutionResult processExecutionResult = processInfo.getResult();
		final String reportFilename = extractReportFilename(processInfo, outputType);
		final String reportContentType = outputType.getContentType();
		processExecutionResult.setReportData(reportData, reportFilename, reportContentType);

		//
		// Print preview (if swing client)
		if (Ini.isClient() && swingJRReportViewerProvider != null)
		{
			swingJRReportViewerProvider.openViewer(reportData, outputType, processInfo);
		}
	}

	private static final String extractReportFilename(final ProcessInfo pi, final OutputType outputType)
	{
		final String fileBasename = Util.firstValidValue(
				basename -> !Check.isEmpty(basename, true),
				() -> extractReportBasename_IfDocument(pi),
				() -> pi.getTitle(),
				() -> "report_" + pi.getAD_PInstance_ID());

		final String fileExtension = outputType.getFileExtension();

		final String filename = fileBasename.trim() + "." + fileExtension;
		return FileUtils.stripIllegalCharacters(filename);
	}

	private static String extractReportBasename_IfDocument(final ProcessInfo pi)
	{
		final TableRecordReference recordRef = pi.getRecordRefOrNull();
		if (recordRef == null)
		{
			return null;
		}

		final Object record = recordRef.getModel();
		final IDocument document = Services.get(IDocumentBL.class).getDocumentOrNull(record);
		if (document == null)
		{
			return null;
		}

		return document.getDocumentInfo();
	}

	private ReportPrintingInfo extractReportPrintingInfo(final ProcessInfo pi)
	{
		final ReportPrintingInfo info = new ReportPrintingInfo();
		info.setProcessInfo(pi);
		info.setPrintPreview(pi.isPrintPreview());
		info.setForceSync(!pi.isAsync()); // gh #1160 if the process info says "sync", then sync it is

		//
		// Determine the ReportingSystem type based on report template file extension
		// TODO: make it more general and centralized with the other reporting code
		final String reportTemplate = pi.getReportTemplate().orElseThrow(() -> new AdempiereException("No report template defined for " + pi));
		final String reportFileExtension = Files.getFileExtension(reportTemplate).toLowerCase();
		if ("jasper".equalsIgnoreCase(reportFileExtension)
				|| "jrxml".equalsIgnoreCase(reportFileExtension))
		{
			info.setReportingSystemType(ReportingSystemType.Jasper);
		}
		else if ("xls".equalsIgnoreCase(reportFileExtension))
		{
			info.setReportingSystemType(ReportingSystemType.Excel);
			info.setPrintPreview(true); // TODO: atm only print preview is supported
		}

		return info;
	}

	/**
	 *
	 * @return {@link JRReportViewerProvider} or null
	 */
	private JRReportViewerProvider getJRReportViewerProviderOrNull()
	{
		if (Ini.isClient())
		{
			return swingJRReportViewerProvider;
		}
		else
		{
			return viewerProvider;
		}
	}

	private void startProcess0(final ProcessInfo pi, final ReportPrintingInfo reportPrintingInfo)
	{
		try
		{
			log.info("Doing direct print without preview: {}", reportPrintingInfo);
			startProcessDirectPrint(reportPrintingInfo);
		}
		catch (final Exception e)
		{
			throw AdempiereException.wrapIfNeeded(e);
		}
	}

	private static enum ReportingSystemType
	{
		Jasper, Excel,
	};

	private static final class ReportPrintingInfo
	{
		private ProcessInfo processInfo;
		private ReportingSystemType reportingSystemType;
		private boolean printPreview;

		private boolean forceSync = false;

		@Override
		public String toString()
		{
			return MoreObjects.toStringHelper(this)
					.omitNullValues()
					.add("reportingSystemType", reportingSystemType)
					.add("printPreview", printPreview)
					.add("forceSync", forceSync)
					.add("processInfo", processInfo)
					.toString();
		}

		public void setProcessInfo(final ProcessInfo processInfo)
		{
			this.processInfo = processInfo;
		}

		public ProcessInfo getProcessInfo()
		{
			return processInfo;
		}

		public void setReportingSystemType(final ReportingSystemType reportingSystemType)
		{
			this.reportingSystemType = reportingSystemType;
		}

		public ReportingSystemType getReportingSystemType()
		{
			return reportingSystemType;
		}

		public void setPrintPreview(final boolean printPreview)
		{
			this.printPreview = printPreview;
		}

		public boolean isPrintPreview()
		{
			return printPreview;
		}

		/**
		 * Even if {@link #isPrintPreview()} is {@code false}, we do <b>not</b> print in a background thread, if this is false.
		 */
		public boolean isForceSync()
		{
			return forceSync;
		}

		public void setForceSync(boolean forceSync)
		{
			this.forceSync = forceSync;
		}
	}
}
