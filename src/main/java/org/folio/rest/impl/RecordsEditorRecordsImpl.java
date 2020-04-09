package org.folio.rest.impl;

import static io.vertx.core.Future.succeededFuture;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.Map;
import java.util.Objects;

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
  public void getRecordsEditorRecords(String instanceId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    if (Objects.nonNull(instanceId)) {
      service.getMarcRecordByInstanceId(instanceId, vertxContext, okapiHeaders)
        .thenAccept(body -> asyncResultHandler.handle(succeededFuture(Response.ok(body, APPLICATION_JSON).build())))
        .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
    } else {
      asyncResultHandler.handle(succeededFuture(GetRecordsEditorRecordsResponse.respond400WithApplicationJson(ErrorUtils.buildError(400, ErrorUtils.ErrorType.EXTERNAL_OR_UNDEFINED, "instanceId parameter is not presented"))));
    }
  }

  @Override
  @Validate
  public void putRecordsEditorRecordsById(String id, QuickMarcJson entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    if (entity.getParsedRecordId().equals(id)) {
      service.putMarcRecordById(id, entity, vertxContext, okapiHeaders)
        .thenAccept(res -> asyncResultHandler.handle(succeededFuture(Response.noContent().build())))
        .exceptionally(throwable -> handleErrorResponse(asyncResultHandler, throwable));
    } else {
      asyncResultHandler.handle(succeededFuture(PutRecordsEditorRecordsByIdResponse.respond400WithApplicationJson(ErrorUtils.buildError(400, "request id and entity id are not equal"))));
    }
  }

  private Void handleErrorResponse(Handler<AsyncResult<Response>> handler, Throwable t) {
    handler.handle(succeededFuture(ErrorUtils.getErrorResponse(t)));
    return null;
  }
}
