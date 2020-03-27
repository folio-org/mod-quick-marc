package org.folio.rest.impl;

import static io.vertx.core.Future.succeededFuture;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.folio.util.ErrorUtils.buildError;
import static org.folio.util.ErrorUtils.buildErrorParameter;
import static org.folio.util.ErrorUtils.buildErrorParameters;
import static org.folio.util.ErrorUtils.buildErrors;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.Errors;
import org.folio.rest.jaxrs.model.Parameter;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.rest.jaxrs.resource.RecordsEditorRecords;
import org.folio.service.MarcRecordsService;
import org.folio.spring.SpringContextUtil;
import org.folio.util.Constants;
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
  public void getRecordsEditorRecords(String id, String instanceId, String lang, Map<String, String> headers, Handler<AsyncResult<Response>> handler, Context context) {
    if(Objects.nonNull(id) && Objects.isNull(instanceId)) {
      service.getMarcRecordById(id, context, headers)
        .thenAccept(body -> handler.handle(succeededFuture(Response.ok(body, APPLICATION_JSON_TYPE).build())))
        .exceptionally(t -> handleErrorResponse(handler, t));
    } else if (Objects.nonNull(instanceId) && Objects.isNull(id)) {
      service.getMarcRecordByInstanceId(instanceId, context, headers)
        .thenAccept(body -> handler.handle(succeededFuture(Response.ok(body, APPLICATION_JSON_TYPE).build())))
        .exceptionally(t -> handleErrorResponse(handler, t));
    } else {
      List<Parameter> parameters = buildErrorParameters(buildErrorParameter(Constants.ID, id), buildErrorParameter(Constants.INSTANCE_ID, instanceId));
      Errors errors = buildErrors(buildError(422, "Illegal query parameters", parameters));
      handler.handle(succeededFuture(GetRecordsEditorRecordsResponse.respond422WithApplicationJson(errors)));
    }
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
