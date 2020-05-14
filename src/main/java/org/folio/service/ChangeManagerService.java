package org.folio.service;

import static org.folio.util.Constants.INSTANCE_ID;
import static org.folio.util.ResourcePathResolver.*;
import static org.folio.util.ServiceUtils.buildQuery;

import io.vertx.core.Context;
import io.vertx.core.json.JsonObject;
import org.folio.converter.ParsedRecordToQuickMarcConverter;
import org.folio.converter.QuickMarcToParsedRecordConverter;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecordDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Service
public class ChangeManagerService extends BaseService implements MarcRecordsService {

  private ParsedRecordToQuickMarcConverter parsedRecordToQuickMarcConverter;
  private QuickMarcToParsedRecordConverter quickMarcToParsedRecordConverter;

  @Override
  public CompletableFuture<QuickMarcJson> getMarcRecordByInstanceId(String instanceId, Context context, Map<String, String> headers) {
    return handleGetRequest(getResourcesPath(CM_RECORDS) + buildQuery(INSTANCE_ID, instanceId), context, headers)
      .thenApply(response -> {
        ParsedRecordDto parsedRecordDto = response.mapTo(ParsedRecordDto.class);
        return parsedRecordToQuickMarcConverter.convert(parsedRecordDto.getParsedRecord()).withExternalDtoId(parsedRecordDto.getId());
      });
  }

  @Override
  public CompletableFuture<Void> putMarcRecordById(String id, QuickMarcJson quickMarcJson, Context context,
    Map<String, String> headers) {
    ParsedRecordDto parsedRecordDto = new ParsedRecordDto()
      .withRecordType(ParsedRecordDto.RecordType.MARC)
      .withParsedRecord(quickMarcToParsedRecordConverter.convert(quickMarcJson))
      .withId(quickMarcJson.getExternalDtoId());
    return handlePutRequest(getResourceByIdPath(CM_RECORDS, id), JsonObject.mapFrom(parsedRecordDto), context, headers);
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
