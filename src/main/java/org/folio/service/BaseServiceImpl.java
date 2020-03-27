package org.folio.service;

import static org.folio.rest.RestVerticle.OKAPI_HEADER_TENANT;
import static org.folio.util.Constants.EMPTY_STRING;
import static org.folio.util.Constants.OKAPI_URL;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.folio.exception.HttpException;
import org.folio.rest.tools.client.HttpClientFactory;
import org.folio.rest.tools.client.Response;
import org.folio.rest.tools.client.interfaces.HttpClientInterface;
import org.folio.rest.tools.utils.TenantTool;

import io.vertx.core.Context;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

public class BaseServiceImpl {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private static final String EXCEPTION_CALLING_ENDPOINT_MSG = "Exception calling {} {}";

  CompletableFuture<JsonObject> handleGetRequest(String endpoint, Context context, Map<String, String> headers) {
    HttpClientInterface client = getHttpClient(headers);
    CompletableFuture<JsonObject> future = new VertxCompletableFuture<>(context);
    try {
      logger.info("Calling GET {}", endpoint);
      client.request(HttpMethod.GET, endpoint, headers)
        .thenApply(response -> {
          logger.debug("Validating response for GET {}", endpoint);
          return validateAndGetResponseBody(response);
        })
        .thenAccept(body -> {
          if (logger.isInfoEnabled()) {
            logger.info("The response body for GET {}: {}", endpoint, body.encodePrettily());
          }
          future.complete(body);
        })
        .exceptionally(t -> {
          logger.error(EXCEPTION_CALLING_ENDPOINT_MSG, HttpMethod.GET, endpoint);
          future.completeExceptionally(t);
          return null;
        });
    } catch (Exception e) {
      logger.error(EXCEPTION_CALLING_ENDPOINT_MSG, e, HttpMethod.GET, endpoint);
      future.completeExceptionally(e);
    } finally {
      client.closeClient();
    }
    return future;
  }

  String buildQuery(String query) {
    return "?query=" + encodeQuery(query);
  }

  private HttpClientInterface getHttpClient(Map<String, String> okapiHeaders) {
    final String okapiURL = okapiHeaders.getOrDefault(OKAPI_URL, EMPTY_STRING);
    final String tenantId = TenantTool.calculateTenantId(okapiHeaders.get(OKAPI_HEADER_TENANT));
    return HttpClientFactory.getHttpClient(okapiURL, tenantId);
  }

  private JsonObject validateAndGetResponseBody(Response response) {
    int code = response.getCode();
    if (!Response.isSuccess(code)) {
      throw new CompletionException(new HttpException(code, response.getError()));
    }
    return response.getBody();
  }

  private String encodeQuery(String query) {
    try {
      return URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      logger.error("Error happened while attempting to encode '{}'", e, query);
      throw new CompletionException(e);
    }
  }

}