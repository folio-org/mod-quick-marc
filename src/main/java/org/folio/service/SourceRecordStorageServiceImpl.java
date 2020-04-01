package org.folio.service;

import static org.folio.util.Constants.INSTANCE_ID;
import static org.folio.util.ErrorUtils.buildError;
import static org.folio.util.ErrorUtils.buildErrorParameter;
import static org.folio.util.ErrorUtils.buildErrorParameters;
import static org.folio.util.ResourcePathResolver.CHANGE_MANAGER;
import static org.folio.util.ResourcePathResolver.SRS_RECORDS;
import static org.folio.util.ResourcePathResolver.getResourceByIdPath;
import static org.folio.util.ResourcePathResolver.getResourcesPath;
import static org.folio.util.ServiceUtils.buildQuery;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.folio.HttpStatus;
import org.folio.converter.ParsedRecordToQuickMarcConverter;
import org.folio.converter.QuickMarcToParsedRecordConverter;
import org.folio.exception.HttpException;
import org.folio.rest.jaxrs.model.Error;
import org.folio.rest.jaxrs.model.Parameter;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecordDto;
import org.folio.srs.model.Record;
import org.folio.srs.model.RecordCollection;
import org.folio.util.ErrorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.vertx.core.Context;
import io.vertx.core.json.JsonObject;

@Service
public class SourceRecordStorageServiceImpl extends BaseServiceImpl implements MarcRecordsService {
  private ParsedRecordToQuickMarcConverter parsedRecordToQuickMarcConverter;
  private QuickMarcToParsedRecordConverter quickMarcToParsedRecordConverter;

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
        Record record = collection.getRecords().get(0);
        return parsedRecordToQuickMarcConverter.convert(record.getParsedRecord()).withExternalDtoId(record.getId());
      });
  }

  @Override
  public CompletableFuture<Void> putMarcRecordById(String id, QuickMarcJson quickMarcJson, Context context,
    Map<String, String> headers) {
    Record record = new Record()
      .withRecordType(ParsedRecordDto.RecordType.MARC)
      .withParsedRecord(quickMarcToParsedRecordConverter.convert(quickMarcJson))
      .withId(quickMarcJson.getExternalDtoId());
    return handlePutRequest(getResourceByIdPath(CHANGE_MANAGER, id), JsonObject.mapFrom(record), context, headers);
  }

  @Autowired
  public void setParsedRecordToQuickMarcConverter(ParsedRecordToQuickMarcConverter parsedRecordToQuickMarcConverter) {
    this.parsedRecordToQuickMarcConverter = parsedRecordToQuickMarcConverter;
  }

  @Autowired
  public void setQuickMarcToParsedRecordConverter(QuickMarcToParsedRecordConverter quickMarcToParsedRecordConverter) {
    this.quickMarcToParsedRecordConverter = quickMarcToParsedRecordConverter;
  }
}
