package org.folio.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.folio.client.HttpClient;
import org.folio.exception.HttpException;
import org.folio.rest.tools.client.Response;
import org.springframework.beans.factory.annotation.Autowired;

import io.vertx.core.Context;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

public abstract class BaseService {

  private static final String EXCEPTION_CALLING_ENDPOINT_MSG = "Exception calling {} {}";
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  @Autowired
  private HttpClient httpClient;

  CompletableFuture<JsonObject> handleGetRequest(String endpoint, Context context, Map<String, String> headers) {
    CompletableFuture<JsonObject> future = new VertxCompletableFuture<>(context);
    logger.info("Calling GET {}", endpoint);
    httpClient.request(HttpMethod.GET, endpoint, headers)
      .thenApply(response -> {
        logger.debug("Validating response for GET {}", endpoint);
        return validateAndGetResponseBody(response);
      })
      .thenAccept(body -> {
        logger.debug("The response body for GET {}: {}", endpoint, body.encode());
        future.complete(body);
      })
      .handle((obj, thr) -> {
        if (thr != null) {
          logger.error(EXCEPTION_CALLING_ENDPOINT_MSG, HttpMethod.GET, endpoint);
          future.completeExceptionally(thr);
        }
        return null;
      });
    return future;
  }

  CompletableFuture<Void> handlePutRequest(String endpoint, JsonObject jsonObject, Context context, Map<String, String> headers) {
    CompletableFuture<Void> future = new VertxCompletableFuture<>(context);
    logger.info("Calling PUT {} with body: {}", endpoint, jsonObject.encode());
    httpClient.request(HttpMethod.PUT, jsonObject.toBuffer(), endpoint, headers)
      .thenApply(response -> {
        logger.debug("Validating response for PUT {}", endpoint);
        return validateAndGetResponseBody(response);
      })
      .thenAccept(body -> {
        logger.info("'PUT {}' request successfully processed", endpoint);
        future.complete(null);
      })
      .exceptionally(throwable -> {
        logger.error(EXCEPTION_CALLING_ENDPOINT_MSG, HttpMethod.PUT, endpoint);
        future.completeExceptionally(throwable);
        return null;
      });
    return future;
  }

  private JsonObject validateAndGetResponseBody(Response response) {
    int code = response.getCode();
    if (!Response.isSuccess(code)) {
      throw new CompletionException(new HttpException(code, response.getError()));
    }
    return response.getBody();
  }
}
