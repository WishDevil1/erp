package de.metas.report.rest;

import java.util.Map;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.Null;
import org.compiere.util.Trace;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.metas.Profiles;
import de.metas.i18n.ITranslatableString;
import de.metas.i18n.Language;
import de.metas.logging.LogManager;
import de.metas.report.server.IReportServer;
import de.metas.report.server.JsonReportError;
import de.metas.report.server.LocalReportServer;
import de.metas.report.server.OutputType;
import de.metas.util.Check;
import de.metas.util.GuavaCollectors;
import de.metas.util.lang.ReferenceListAwareEnum;
import de.metas.util.lang.RepoIdAware;
import lombok.NonNull;

@RestController
@RequestMapping(value = ReportRestController.ENDPOINT)
@Profile(Profiles.PROFILE_ReportService)
public class ReportRestController
{
	private static final Logger logger = LogManager.getLogger(ReportRestController.class);

	public static final String SERVLET_ROOT = "/adempiereJasper";
	static final String ENDPOINT = SERVLET_ROOT + "/ReportServlet";

	private final LocalReportServer server = new LocalReportServer();

	@GetMapping
	public ResponseEntity<Object> report(
			@RequestParam(name = "AD_Process_ID", required = false) final int processId,
			@RequestParam(name = "AD_PInstance_ID", required = false) final int pinstanceId,
			@RequestParam(name = "AD_Language", required = false) final String adLanguage,
			@RequestParam(name = "output", required = false) final String outputStr)
	{
		try (final MDCCloseable c1 = MDC.putCloseable("AD_Process_ID", String.valueOf(processId));
				final MDCCloseable c2 = MDC.putCloseable("AD_PInstance_ID", String.valueOf(pinstanceId));
				final MDCCloseable c3 = MDC.putCloseable("output", String.valueOf(outputStr)))
		{
			final OutputType outputType = outputStr == null ? IReportServer.DEFAULT_OutputType : OutputType.valueOf(outputStr);
			final String reportContentType = outputType.getContentType();
			final String reportFilename = "report." + outputType.getFileExtension();

			final byte[] reportData = server.report(processId, pinstanceId, adLanguage, outputType);

			final HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType(reportContentType));
			headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + reportFilename + "\"");
			headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

			return ResponseEntity.ok()
					.headers(headers)
					.body(reportData);
		}
		catch (final Throwable ex)
		{
			logger.error("Failed creating report for processId={}, pinstanceId={}, adLanguage={}, outputType={}",
					processId, pinstanceId, adLanguage, outputStr,
					ex);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(toJsonReportError(ex, adLanguage));
		}
	}

	private static JsonReportError toJsonReportError(final Throwable throwable, final String adLanguage)
	{
		final Throwable cause = AdempiereException.extractCause(throwable);

		final String adLanguageEffective = !Check.isBlank(adLanguage)
				? adLanguage
				: Language.getBaseAD_Language();

		return JsonReportError.builder()
				.message(AdempiereException.extractMessageTrl(cause).translate(adLanguageEffective))
				.stackTrace(Trace.toOneLineStackTraceString(cause.getStackTrace()))
				.parameters(extractParameters(throwable, adLanguageEffective))
				.build();
	}

	private static Map<String, String> extractParameters(@NonNull final Throwable throwable, @NonNull final String adLanguage)
	{
		return AdempiereException.extractParameters(throwable)
				.entrySet()
				.stream()
				.map(e -> GuavaCollectors.entry(e.getKey(), convertParameterToJson(e.getValue(), adLanguage)))
				.collect(GuavaCollectors.toImmutableMap());
	}

	@NonNull
	private static String convertParameterToJson(final Object value, final String adLanguage)
	{
		if (Null.isNull(value))
		{
			return "<null>";
		}
		else if (value instanceof ITranslatableString)
		{
			return ((ITranslatableString)value).translate(adLanguage);
		}
		else if (value instanceof RepoIdAware)
		{
			return String.valueOf(((RepoIdAware)value).getRepoId());
		}
		else if (value instanceof ReferenceListAwareEnum)
		{
			return ((ReferenceListAwareEnum)value).getCode();
		}
		else
		{
			return value.toString();
		}
	}
}
