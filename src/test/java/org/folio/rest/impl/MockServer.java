package org.folio.rest.impl;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.folio.util.ResourcePathResolver.SRS_RECORDS;
import static org.folio.util.ResourcePathResolver.getResourcesPath;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.folio.srs.model.Record;
import org.folio.srs.model.RecordCollection;
import org.folio.util.ResourcePathResolver;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class MockServer {

  private static final Logger logger = LoggerFactory.getLogger(MockServer.class);

  public static final String BASE_MOCK_DATA_PATH = "mockdata/";
  public static final String SRS_RECORDS_COLLECTION_PATH = BASE_MOCK_DATA_PATH + "srs-records/records.json";

  public static final String EXISTED_ID = "67dfac11-1caf-4470-9ad1-d533f6360bdd";
  public static final String EXISTED_INSTANCE_ID = "54cc0262-76df-4cac-acca-b10e9bc5c79a";
  public static final String ID_BAD_REQUEST_FORMAT = "123-45-678-90-abc";
  protected static final String ID_DOES_NOT_EXIST = "d25498e7-3ae6-45fe-9612-ec99e2700d2f";
  protected static final String ID_FOR_INTERNAL_SERVER_ERROR = "168f8a86-d26c-406e-813f-c7527f241ac3";

  public static final String ID = "id";
  private static final String QUERY = "query";
  public static final String INSTANCE_ID = "instanceId";

  static Table<String, HttpMethod, Map<String, String>> serverRqRs = HashBasedTable.create();

  private final int port;
  private final Vertx vertx;

  MockServer(int port) {
    this.port = port;
    this.vertx = Vertx.vertx();
  }

  void start() throws InterruptedException, ExecutionException, TimeoutException {
    HttpServer server = vertx.createHttpServer();
    CompletableFuture<HttpServer> deploymentComplete = new CompletableFuture<>();
    server.requestHandler(defineRoutes()).listen(port, result -> {
      if(result.succeeded()) {
        deploymentComplete.complete(result.result());
      }
      else {
        deploymentComplete.completeExceptionally(result.cause());
      }
    });
    deploymentComplete.get(60, TimeUnit.SECONDS);
  }

  void close() {
    vertx.close(res -> {
      if (res.failed()) {
        logger.error("Failed to shut down mock server", res.cause());
        fail(res.cause().getMessage());
      } else {
        logger.info("Successfully shut down mock server");
      }
    });
  }

  public static void release() {
    serverRqRs.clear();
  }

  private void processServerResponse(String resource, RoutingContext ctx, int statusCode, String contentType, String body) {
    addServerRqRsData(ctx.request().method(), resource, ctx.request().uri(), body);
    ctx.response()
      .setStatusCode(statusCode)
      .putHeader(HttpHeaders.CONTENT_TYPE, contentType)
      .end(body);
  }

  private static void addServerRqRsData(HttpMethod method, String resource, String endpoint, String data) {
    Map<String, String> entries = serverRqRs.get(resource, method);
    if (entries == null) {
      entries = new HashMap<>();
    }
    entries.put(endpoint, data);
    serverRqRs.put(resource, method, entries);
  }

  private static String getResourceByIdPath(String resource) {
    return ResourcePathResolver.getResourcesPath(resource) + "/:id";
  }

  private Router defineRoutes() {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.get(getResourceByIdPath(SRS_RECORDS)).handler(this::handleGetMarcJsonRecord);
    router.get(getResourcesPath(SRS_RECORDS)).handler(this::handleGetMarcJsonRecords);
    return router;
  }

  private void handleGetMarcJsonRecord(RoutingContext ctx) {
    logger.info("got: " + ctx.request().path());
    String id = ctx.request().getParam(ID);
    logger.info("id: " + id);

    List<Record> records = getSrsRecordsBySearchParameter(ID, id);

    if (ID_BAD_REQUEST_FORMAT.equals(id)) {
      processServerResponse(SRS_RECORDS, ctx, 422, APPLICATION_JSON, "invalid input syntax for type uuid: " + id);
    } else if (ID_FOR_INTERNAL_SERVER_ERROR.equals(id)) {
        processServerResponse(SRS_RECORDS, ctx, 500, TEXT_PLAIN, INTERNAL_SERVER_ERROR.getReasonPhrase());
    } else if (!records.isEmpty()) {
      processServerResponse(SRS_RECORDS, ctx, 200, APPLICATION_JSON, JsonObject.mapFrom(records.get(0)).encodePrettily());
    }  else {
      processServerResponse(SRS_RECORDS, ctx, 404, TEXT_PLAIN, "Record with id '" + id +"' was not found");
    }
  }

  private void handleGetMarcJsonRecords(RoutingContext ctx) {
    logger.info("got: " + ctx.request().path());

    String query = StringUtils.trimToEmpty(ctx.request().getParam(QUERY));
    Matcher matcher = Pattern.compile(".*externalIdsHolder.instanceId==(\\S[^)]+).*").matcher(query);
    final String instanceId = matcher.find() ? matcher.group(1) : EMPTY;

    if (query.contains(ID_FOR_INTERNAL_SERVER_ERROR)) {
      processServerResponse(SRS_RECORDS, ctx, 500, APPLICATION_JSON, INTERNAL_SERVER_ERROR.getReasonPhrase());
    } else {
      try {
        List<Record> records = getSrsRecordsBySearchParameter(INSTANCE_ID, instanceId);
        RecordCollection collection = new RecordCollection()
          .withRecords(records)
          .withTotalRecords(records.size());
        processServerResponse(SRS_RECORDS, ctx, 200, APPLICATION_JSON, JsonObject.mapFrom(collection).encodePrettily());
      } catch (Exception e) {
        processServerResponse(SRS_RECORDS, ctx, 500, APPLICATION_JSON, INTERNAL_SERVER_ERROR.getReasonPhrase());
      }
    }
  }

  private List<Record> getSrsRecordsBySearchParameter(String key, String value) {

    List<Record> records;

    try {
      records = new JsonObject(getMockData(SRS_RECORDS_COLLECTION_PATH)).mapTo(RecordCollection.class).getRecords();
    } catch (IOException e) {
      records = new ArrayList<>();
    }

    if(INSTANCE_ID.equals(key)) {
      records.removeIf(record -> !Objects.equals(record.getExternalIdsHolder().getInstanceId(), value));
    } else if(ID.equals(key)) {
      records.removeIf(record -> !Objects.equals(record.getId(), value));
    } else {
      records.clear();
    }

    return records;
  }

  public static Map<String, String> getRqRsEntries(HttpMethod method, String resource) {
    Map<String, String> entries = serverRqRs.get(resource, method);
    if (entries == null) {
      entries = new HashMap<>();
    }
    return entries;
  }

  private static String getMockData(String path) throws IOException {
    logger.info("Using mock datafile: {}", path);
    try (InputStream stream = MockServer.class.getClassLoader().getResourceAsStream(path)) {
      if (stream != null) {
        return IOUtils.toString(stream, StandardCharsets.UTF_8);
      } else {
        StringBuilder sb = new StringBuilder();
        try (Stream<String> lines = Files.lines(Paths.get(path))) {
          lines.forEach(sb::append);
        }
        return sb.toString();
      }
    }
  }
}
