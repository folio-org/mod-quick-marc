package org.folio.service;

import static org.folio.util.Constants.INSTANCE_ID;
import static org.folio.util.ErrorUtils.buildError;
import static org.folio.util.ErrorUtils.buildErrorParameter;
import static org.folio.util.ErrorUtils.buildErrorParameters;
import static org.folio.util.ErrorUtils.buildErrors;
import static org.folio.util.ResourcePathResolver.SRS_RECORDS;
import static org.folio.util.ResourcePathResolver.getResourceByIdPath;
import static org.folio.util.ResourcePathResolver.getResourcesPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.folio.HttpStatus;
import org.folio.converter.RecordToQuickMarcConverter;
import org.folio.exception.HttpException;
import org.folio.rest.jaxrs.model.Error;
import org.folio.rest.jaxrs.model.Errors;
import org.folio.rest.jaxrs.model.Parameter;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.Record;
import org.folio.srs.model.RecordCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.vertx.core.Context;
import io.vertx.core.json.JsonObject;

@Service
public class MarcRecordServiceImpl extends BaseServiceImpl implements MarcRecordsService {

  @Autowired
  private RecordToQuickMarcConverter converter;

  @Override
  public CompletableFuture<QuickMarcJson> getMarcRecordById(String id, Context context, Map<String, String> headers) {
    return handleGetRequest(getResourceByIdPath(SRS_RECORDS, id), context, headers).thenApply(record -> converter.convert(record.mapTo(Record.class)));
  }

  @Override
  public CompletableFuture<QuickMarcJson> getMarcRecordByInstanceId(String instanceId, Context context, Map<String, String> headers) {
    return handleGetRequest(getResourcesPath(SRS_RECORDS) + buildQuery( "externalIdsHolder.instanceId==" + instanceId), context, headers)
      .thenApply(records -> {
        RecordCollection collection = records.mapTo(RecordCollection.class);
        if (collection.getTotalRecords() == 0) {
          int code = 404;
          List<Parameter> parameters = buildErrorParameters(buildErrorParameter(INSTANCE_ID, instanceId));
          Errors errors = buildErrors(buildError(code, "Illegal query parameters", parameters));
          throw new CompletionException(new HttpException(code, JsonObject.mapFrom(errors)));
        }
        return converter.convert(collection.getRecords().get(0));
      });
  }
}
