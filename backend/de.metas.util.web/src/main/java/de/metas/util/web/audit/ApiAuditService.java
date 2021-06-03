/*
 * #%L
 * de.metas.util.web
 * %%
 * Copyright (C) 2021 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

package de.metas.util.web.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import de.metas.JsonObjectMapperHolder;
import de.metas.audit.ApiAuditLoggable;
import de.metas.audit.HttpMethod;
import de.metas.audit.common.HttpHeadersWrapper;
import de.metas.audit.config.ApiAuditConfig;
import de.metas.audit.config.ApiAuditConfigId;
import de.metas.audit.config.ApiAuditConfigRepository;
import de.metas.audit.config.NotificationTriggerType;
import de.metas.audit.request.ApiRequestAudit;
import de.metas.audit.request.ApiRequestAuditId;
import de.metas.audit.request.ApiRequestAuditRepository;
import de.metas.audit.request.Status;
import de.metas.audit.request.log.ApiAuditRequestLogDAO;
import de.metas.audit.response.ApiResponseAudit;
import de.metas.audit.response.ApiResponseAuditRepository;
import de.metas.common.rest_api.common.JsonMetasfreshId;
import de.metas.i18n.AdMessageKey;
import de.metas.notification.INotificationBL;
import de.metas.notification.UserNotificationRequest;
import de.metas.organization.OrgId;
import de.metas.user.UserId;
import de.metas.util.Check;
import de.metas.util.Loggables;
import de.metas.util.NumberUtils;
import de.metas.util.Services;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.util.lang.IAutoCloseable;
import org.compiere.model.I_API_Request_Audit;
import org.compiere.util.Env;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@Service
public class ApiAuditService
{
	public static final String API_FILTER_REQUEST_ID_HEADER = "X-ApiFilter-Request-ID";

	private static final AdMessageKey MSG_SUCCESSFUL_API_INVOCATION =
			AdMessageKey.of("de.metas.util.web.audit.successful_invocation");

	private static final AdMessageKey MSG_API_INVOCATION_FAILED =
			AdMessageKey.of("de.metas.util.web.audit.invocation_failed");

	private final INotificationBL notificationBL = Services.get(INotificationBL.class);

	private final ApiAuditConfigRepository apiAuditConfigRepository;
	private final ApiRequestAuditRepository apiRequestAuditRepository;
	private final ApiResponseAuditRepository apiResponseAuditRepository;
	private final ApiAuditRequestLogDAO apiAuditRequestLogDAO;

	private final WebClient webClient;
	private final ConcurrentHashMap<UserId, HttpCallScheduler> callerId2Scheduler;
	private final ObjectMapper objectMapper;

	public ApiAuditService(
			@NonNull final ApiAuditConfigRepository apiAuditConfigRepository,
			@NonNull final ApiRequestAuditRepository apiRequestAuditRepository,
			@NonNull final ApiResponseAuditRepository apiResponseAuditRepository,
			@NonNull final ApiAuditRequestLogDAO apiAuditRequestLogDAO)
	{
		this.apiAuditConfigRepository = apiAuditConfigRepository;
		this.apiRequestAuditRepository = apiRequestAuditRepository;
		this.apiResponseAuditRepository = apiResponseAuditRepository;
		this.apiAuditRequestLogDAO = apiAuditRequestLogDAO;
		this.webClient = WebClient.create();
		this.callerId2Scheduler = new ConcurrentHashMap<>();
		this.objectMapper = JsonObjectMapperHolder.newJsonObjectMapper();
	}

	public boolean wasAlreadyFiltered(@NonNull final HttpServletRequest httpServletRequest)
	{
		return httpServletRequest.getHeader(API_FILTER_REQUEST_ID_HEADER) != null;
	}

	@NonNull
	public Optional<ApiRequestAuditId> extractApiRequestAuditId(@NonNull final HttpServletRequest httpServletRequest)
	{
		return Optional.ofNullable(httpServletRequest.getHeader(API_FILTER_REQUEST_ID_HEADER))
				.map(requestId -> ApiRequestAuditId.ofRepoId(NumberUtils.asInt(requestId, -1)));
	}

	public ApiAuditLoggable createLogger(@NonNull final ApiRequestAuditId apiRequestAuditId)
	{
		return ApiAuditLoggable.builder()
				.clientId(Env.getClientId())
				.userId(Env.getLoggedUserId())
				.apiRequestAuditId(apiRequestAuditId)
				.apiAuditRequestLogDAO(apiAuditRequestLogDAO)
				.bufferSize(100)
				.build();
	}

	@NonNull
	public Optional<ApiAuditConfig> getMatchingAuditConfig(@NonNull final ServletRequest request)
	{
		final CustomHttpRequestWrapper requestWrapper = new CustomHttpRequestWrapper((HttpServletRequest)request, objectMapper);

		if (Check.isBlank(requestWrapper.getFullPath()) || Check.isBlank(requestWrapper.getHttpMethodString()))
		{
			return Optional.empty();
		}

		final OrgId orgId = Env.getOrgId();
		final ImmutableList<ApiAuditConfig> apiAuditConfigs = apiAuditConfigRepository.getAllConfigsByOrgId(orgId);

		return apiAuditConfigs.stream()
				.filter(config -> config.matchesRequest(requestWrapper.getFullPath(), requestWrapper.getHttpMethodString()))
				.min(Comparator.comparingInt(ApiAuditConfig::getSeqNo));
	}

	public void processHttpCall(
			@NonNull final HttpServletRequest request,
			@NonNull final HttpServletResponse response,
			@NonNull final ApiAuditConfig apiAuditConfig)
	{
		final OrgId orgId = apiAuditConfig.getOrgId();

		final CustomHttpRequestWrapper httpRequest = new CustomHttpRequestWrapper(request, objectMapper);

		final ApiRequestAudit requestAudit = logRequest(httpRequest, apiAuditConfig.getApiAuditConfigId(), orgId);

		final ApiAuditLoggable apiAuditLoggable = createLogger(requestAudit.getIdNotNull());

		try (final IAutoCloseable loggableRestorer = Loggables.temporarySetLoggable(apiAuditLoggable))
		{
			final CompletableFuture<ApiResponse> actualRestApiResponseCF = new CompletableFuture<>();

			final FutureCompletionContext futureCompletionContext = FutureCompletionContext.builder()
					.apiAuditConfig(apiAuditConfig)
					.apiAuditLoggable(apiAuditLoggable)
					.apiRequestAudit(requestAudit)
					.orgId(orgId)
					.build();

			actualRestApiResponseCF
					.whenComplete((apiResponse, throwable) -> handleFutureCompletion(apiResponse, throwable, futureCompletionContext));

			final Supplier<ApiResponse> callEndpointSupplier = () -> executeHttpCall(requestAudit);

			final ScheduleRequest scheduleRequest = new ScheduleRequest(actualRestApiResponseCF, callEndpointSupplier);

			final UserId callerUserId = Env.getLoggedUserId();

			final HttpCallScheduler httpCallScheduler = callerId2Scheduler.computeIfAbsent(callerUserId, (userId) -> new HttpCallScheduler());

			httpCallScheduler.schedule(scheduleRequest);

			handleSuccessfulResponse(apiAuditConfig, requestAudit, actualRestApiResponseCF, response);
		}
		catch (final Exception e)
		{
			apiAuditLoggable.addLog(e.getMessage(), e);
			throw AdempiereException.wrapIfNeeded(e);
		}
		finally
		{
			apiAuditLoggable.flush();
		}
	}

	@NonNull
	public ApiResponse executeHttpCall(@NonNull final ApiRequestAudit apiRequestAudit)
	{
		if (apiRequestAudit.getMethod() == null || apiRequestAudit.getPath() == null)
		{
			throw new AdempiereException("Missing essential data from the given ApiRequestAudit!")
					.setParameter("Method", apiRequestAudit.getMethod())
					.setParameter("Path", apiRequestAudit.getPath());
		}

		final org.springframework.http.HttpMethod httpMethod = org.springframework.http.HttpMethod.resolve(apiRequestAudit.getMethod().getCode());

		final WebClient.RequestBodyUriSpec uriSpec = webClient.method(httpMethod);

		final HttpHeaders httpHeaders = new HttpHeaders();
		apiRequestAudit.getRequestHeaders(objectMapper)
				.map(HttpHeadersWrapper::getKeyValueHeaders)
				.ifPresent(httpHeaders::addAll);

		httpHeaders.add(API_FILTER_REQUEST_ID_HEADER, String.valueOf(apiRequestAudit.getIdNotNull().getRepoId()));

		httpHeaders
				.forEach((key, value) -> uriSpec.header(key, value.toArray(new String[0])));

		final URI uri = URI.create(apiRequestAudit.getPath());

		final WebClient.RequestBodySpec bodySpec = uriSpec.uri(uri);

		if (Check.isNotBlank(apiRequestAudit.getBody()))
		{
			bodySpec.body(Mono.just(apiRequestAudit.getRequestBody(objectMapper)), Object.class);
		}

		return bodySpec.exchangeToMono(cr -> cr
				.bodyToMono(Object.class)

				.defaultIfEmpty(ApiResponse.of(cr.rawStatusCode(), cr.headers().asHttpHeaders(), null))

				.map(body -> ApiResponse.of(cr.rawStatusCode(), cr.headers().asHttpHeaders(), body)))
				.block();
	}

	public void logResponse(
			@NonNull final ApiResponse apiResponse,
			@NonNull final ApiRequestAuditId apiRequestAuditId,
			@NonNull final OrgId orgId)
	{
		try
		{
			final String bodyAsString = apiResponse.getBody() != null
					? objectMapper.writeValueAsString(apiResponse.getBody())
					: null;

			final LinkedMultiValueMap<String, String> responseHeadersMultiValueMap = new LinkedMultiValueMap<>();

			if (apiResponse.getHttpHeaders() != null)
			{
				apiResponse.getHttpHeaders().forEach(responseHeadersMultiValueMap::addAll);
			}

			final String responseHeaders = responseHeadersMultiValueMap.isEmpty()
					? null
					: objectMapper.writeValueAsString(HttpHeadersWrapper.of(responseHeadersMultiValueMap));

			final ApiResponseAudit apiResponseAudit = ApiResponseAudit.builder()
					.orgId(orgId)
					.apiRequestAuditId(apiRequestAuditId)
					.body(bodyAsString)
					.httpCode(String.valueOf(apiResponse.getStatusCode()))
					.time(Instant.now())
					.httpHeaders(responseHeaders)
					.build();

			apiResponseAuditRepository.save(apiResponseAudit);
		}
		catch (final JsonProcessingException e)
		{
			final AdempiereException exception = AdempiereException.wrapIfNeeded(e)
					.appendParametersToMessage()
					.setParameter("ApiResponse", apiResponse);

			Loggables.addLog("Error when trying to parse the api response body!", exception);
		}
	}

	@NonNull
	public ApiRequestAudit updateRequestStatus(
			@NonNull final Status status,
			@NonNull final ApiRequestAudit apiRequestAudit)
	{
		final ApiRequestAudit updateApiRequestAudit = apiRequestAudit.toBuilder()
				.status(status)
				.build();

		return apiRequestAuditRepository.save(updateApiRequestAudit);
	}

	public void notifyUserInCharge(
			@NonNull final ApiAuditConfig apiAuditConfig,
			@NonNull final ApiRequestAudit apiRequestAudit,
			final boolean isError)
	{
		if (apiAuditConfig.getUserInChargeId() == null
				|| apiAuditConfig.getNotifyUserInCharge() == null
				|| apiAuditConfig.getNotifyUserInCharge().equals(NotificationTriggerType.NEVER)
				|| (NotificationTriggerType.ONLY_ON_ERROR.equals(apiAuditConfig.getNotifyUserInCharge())
				&& !isError))
		{
			Loggables.addLog("Notification skipped due to ApiAuditConfig! ApiAuditConfigId = {}, NotifyUserInChargeTrigger = {}"
					, apiAuditConfig.getUserInChargeId(), apiAuditConfig.getNotifyUserInCharge());
			return;
		}

		final AdMessageKey messageKey = isError ? MSG_API_INVOCATION_FAILED : MSG_SUCCESSFUL_API_INVOCATION;

		final UserNotificationRequest.TargetRecordAction targetRecordAction = UserNotificationRequest
				.TargetRecordAction
				.of(I_API_Request_Audit.Table_Name, apiRequestAudit.getIdNotNull().getRepoId());

		notificationBL.send(UserNotificationRequest.builder()
									.recipientUserId(apiAuditConfig.getUserInChargeId())
									.contentADMessage(messageKey)
									.contentADMessageParam(apiRequestAudit.getPath())
									.targetAction(targetRecordAction)
									.build());
	}

	private ApiRequestAudit logRequest(
			@NonNull final CustomHttpRequestWrapper customHttpRequest,
			@NonNull final ApiAuditConfigId apiAuditConfigId,
			@NonNull final OrgId orgId)
	{
		try
		{
			final LinkedMultiValueMap<String, String> requestHeadersMultiValueMap = new LinkedMultiValueMap<>();
			customHttpRequest.getAllHeaders().forEach(requestHeadersMultiValueMap::addAll);

			final String requestHeaders = requestHeadersMultiValueMap.isEmpty()
					? null
					: objectMapper.writeValueAsString(HttpHeadersWrapper.of(requestHeadersMultiValueMap));

			final ApiRequestAudit apiRequestAudit = ApiRequestAudit.builder()
					.apiAuditConfigId(apiAuditConfigId)
					.orgId(orgId)
					.roleId(Env.getLoggedRoleId())
					.userId(Env.getLoggedUserId())
					.status(Status.RECEIVED)
					.body(customHttpRequest.getRequestBodyAsString())
					.method(HttpMethod.ofCodeOptional(customHttpRequest.getHttpMethodString()).orElse(null))
					.path(customHttpRequest.getFullPath())
					.remoteAddress(customHttpRequest.getRemoteAddr())
					.remoteHost(customHttpRequest.getRemoteHost())
					.time(Instant.now())
					.httpHeaders(requestHeaders)
					.build();

			return apiRequestAuditRepository.save(apiRequestAudit);
		}
		catch (final JsonProcessingException e)
		{
			throw new AdempiereException("Failed to create ApiRequestAudit!", e)
					.appendParametersToMessage()
					.setParameter("Path", customHttpRequest.getFullPath())
					.setParameter("Method", customHttpRequest.getHttpMethodString());

		}
	}

	private void handleFutureCompletion(
			@Nullable final ApiResponse apiResponse,
			@Nullable final Throwable throwable,
			@NonNull final FutureCompletionContext completionContext)
	{
		try (final IAutoCloseable loggableRestorer = Loggables.temporarySetLoggable(completionContext.getApiAuditLoggable()))
		{
			if (apiResponse != null)
			{
				logResponse(apiResponse, completionContext.getApiRequestAudit().getIdNotNull(), completionContext.getOrgId());

				final Status requestStatus = apiResponse.getStatusCode() / 100 > HttpStatus.OK.series().value()
						? Status.ERROR
						: Status.PROCESSED;

				updateRequestStatus(requestStatus, completionContext.getApiRequestAudit());

				final boolean isError = Status.ERROR.equals(requestStatus);
				notifyUserInCharge(completionContext.getApiAuditConfig(), completionContext.getApiRequestAudit(), isError);

				Loggables.addLog("Request routed successfully!");
			}
			else
			{
				updateRequestStatus(Status.ERROR, completionContext.getApiRequestAudit());

				final boolean isError = true;
				notifyUserInCharge(completionContext.getApiAuditConfig(), completionContext.getApiRequestAudit(), isError);

				Loggables.addLog("Error encountered while routing the request!", throwable);
			}
		}
	}

	private void handleSuccessfulResponse(
			@NonNull final ApiAuditConfig apiAuditConfig,
			@NonNull final ApiRequestAudit apiRequestAudit,
			@NonNull final CompletableFuture<ApiResponse> actualRestApiResponseCF,
			@NonNull final HttpServletResponse httpServletResponse) throws IOException, InterruptedException, ExecutionException, TimeoutException
	{
		final ApiResponse actualAPIResponse = apiAuditConfig.isInvokerWaitsForResponse()
				? actualRestApiResponseCF.get(300, TimeUnit.SECONDS)
				: getGenericNoWaitResponse();

		final JsonApiResponse apiResponse = JsonApiResponse.builder()
				.requestId(JsonMetasfreshId.of(apiRequestAudit.getIdNotNull().getRepoId()))
				.endpointResponse(actualAPIResponse.getBody())
				.build();

		if (actualAPIResponse.getHttpHeaders() != null)
		{
			actualAPIResponse.getHttpHeaders()
					.forEach((key, values) -> values.forEach(value -> httpServletResponse.addHeader(key, value)));
		}

		httpServletResponse.resetBuffer();
		httpServletResponse.setStatus(actualAPIResponse.getStatusCode());
		httpServletResponse.getWriter().write(objectMapper.writeValueAsString(apiResponse));
		httpServletResponse.flushBuffer();
	}

	@NonNull
	private ApiResponse getGenericNoWaitResponse()
	{
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

		return ApiResponse.of(HttpStatus.ACCEPTED.value(), httpHeaders, null);
	}

	@Value
	@Builder
	private static class FutureCompletionContext
	{
		@NonNull
		ApiAuditLoggable apiAuditLoggable;

		@NonNull
		ApiRequestAudit apiRequestAudit;

		@NonNull
		OrgId orgId;

		@NonNull
		ApiAuditConfig apiAuditConfig;
	}
}
