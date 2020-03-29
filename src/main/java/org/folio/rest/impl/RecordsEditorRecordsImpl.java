package org.folio.rest.impl;

import static io.vertx.core.Future.succeededFuture;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.util.Map;

import javax.ws.rs.core.Response;

import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.rest.jaxrs.resource.RecordsEditorRecords;
import org.folio.service.MarcRecordsService;
import org.folio.spring.SpringContextUtil;
import org.folio.util.ErrorUtils;
import org.springframework.beans.factory.annotation.Autowired;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public class RecordsEditorRecordsImpl implements RecordsEditorRecords {

  @Autowired
  private MarcRecordsService service;

  public RecordsEditorRecordsImpl() {
    SpringContextUtil.autowireDependencies(this, Vertx.currentContext());
  }

  @Override
  @Validate
  public void getRecordsEditorRecordsByInstanceId(String instanceId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    service.getMarcRecordByInstanceId(instanceId, vertxContext, okapiHeaders)
      .thenAccept(body -> asyncResultHandler.handle(succeededFuture(Response.ok(body, APPLICATION_JSON_TYPE).build())))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  @Validate
  public void putRecordsEditorRecordsById(String id, String lang, QuickMarcJson entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    asyncResultHandler.handle(succeededFuture(PutRecordsEditorRecordsByIdResponse.respond500WithTextPlain("Is not implemented yet")));
  }

  private Void handleErrorResponse(Handler<AsyncResult<Response>> handler, Throwable t) {
    handler.handle(succeededFuture(ErrorUtils.getErrorResponse(t)));
    return null;
  }
}
