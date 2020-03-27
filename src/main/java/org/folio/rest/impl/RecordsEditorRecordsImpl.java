package org.folio.rest.impl;

import static io.vertx.core.Future.succeededFuture;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.rest.jaxrs.resource.RecordsEditorRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;

public class RecordsEditorRecordsImpl implements RecordsEditorRecords {

  private static final Logger logger = LoggerFactory.getLogger(RecordsEditorRecordsImpl.class);
  private static final String RESPONSE_MOCK = "quick_marc_json.json";

  @Override
  @Validate
  public void putRecordsEditorRecordsById(String id, QuickMarcJson entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    asyncResultHandler.handle(succeededFuture(PutRecordsEditorRecordsByIdResponse.respond500WithTextPlain("Is not implemented yet")));
  }

  @Override
  @Validate
  public void getRecordsEditorRecordsById(String id, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    try {
      asyncResultHandler.handle(succeededFuture(Response.ok(getMockData(RESPONSE_MOCK), APPLICATION_JSON).build()));
    } catch (IOException e) {
      asyncResultHandler.handle(succeededFuture(Response.serverError().build()));
    }
  }

  protected static String getMockData(String path) throws IOException {
    logger.info("Using mock datafile: {}", path);
    try (InputStream resourceAsStream = RecordsEditorRecordsImpl.class.getClassLoader().getResourceAsStream(path)) {
      return IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
    }
  }
}
