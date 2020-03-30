package org.folio.service;

import static org.folio.util.Constants.INSTANCE_ID;
import static org.folio.util.ErrorUtils.buildError;
import static org.folio.util.ErrorUtils.buildErrorParameter;
import static org.folio.util.ErrorUtils.buildErrorParameters;
import static org.folio.util.ResourcePathResolver.SRS_RECORDS;
import static org.folio.util.ResourcePathResolver.getResourcesPath;
import static org.folio.util.ServiceUtils.buildQuery;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.folio.HttpStatus;
import org.folio.converter.RecordToQuickMarcConverter;
import org.folio.exception.HttpException;
import org.folio.rest.jaxrs.model.Error;
import org.folio.rest.jaxrs.model.Parameter;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.RecordCollection;
import org.folio.util.ErrorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.vertx.core.Context;
import io.vertx.core.json.JsonObject;

@Service
public class SourceRecordStorageServiceImpl extends BaseServiceImpl implements MarcRecordsService {

  @Autowired
  private RecordToQuickMarcConverter converter;

  @Override
  public CompletableFuture<QuickMarcJson> getMarcRecordByInstanceId(String instanceId, Context context, Map<String, String> headers) {
    return handleGetRequest(getResourcesPath(SRS_RECORDS) + buildQuery("query","externalIdsHolder.instanceId==" + instanceId), context, headers)
      .thenApply(records -> {
        RecordCollection collection = records.mapTo(RecordCollection.class);
        if (collection.getTotalRecords() == 0) {
          int code = HttpStatus.HTTP_NOT_FOUND.toInt();
          List<Parameter> parameters = buildErrorParameters(buildErrorParameter(INSTANCE_ID, instanceId));
          Error error = buildError(code, ErrorUtils.ErrorType.INTERNAL,"Record with id=" + instanceId + " not found", parameters);
          throw new CompletionException(new HttpException(code, JsonObject.mapFrom(error)));
        }
        return converter.convert(collection.getRecords().get(0).getParsedRecord());
      });
  }
}
