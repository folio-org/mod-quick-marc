package org.folio.service;

import static org.folio.util.ResourcePathResolver.CM_RECORDS;
import static org.folio.util.ResourcePathResolver.getResourceByIdPath;
import static org.folio.util.ResourcePathResolver.getResourcesPath;
import static org.folio.util.ServiceUtils.buildQuery;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecordDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import io.vertx.core.Context;
import io.vertx.core.json.JsonObject;

@Service
public class ChangeManagerService extends BaseService implements MarcRecordsService {

  public static final String INSTANCE_ID = "instanceId";

  @Autowired
  private Converter<ParsedRecordDto, QuickMarcJson> parsedRecordToQuickMarcConverter;
  @Autowired
  private Converter<QuickMarcJson, ParsedRecordDto> quickMarcToParsedRecordConverter;

  @Override
  public CompletableFuture<QuickMarcJson> getMarcRecordByInstanceId(String instanceId, Context context, Map<String, String> headers) {
    CompletableFuture<QuickMarcJson> future = new CompletableFuture<>();
    try {
      handleGetRequest(getResourcesPath(CM_RECORDS) + buildQuery(INSTANCE_ID, instanceId), headers)
        .thenApply(
            parsedRecordDtoJson -> parsedRecordToQuickMarcConverter.convert(parsedRecordDtoJson.mapTo(ParsedRecordDto.class)))
        .thenAccept(future::complete)
        .exceptionally(e -> {
          future.completeExceptionally(e);
          return null;
        });
    } catch (Exception e) {
      future.completeExceptionally(e);
    }
    return future;
  }

  @Override
  public CompletableFuture<Void> putMarcRecordById(String id, QuickMarcJson quickMarcJson, Context context, Map<String, String> headers) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    try {
      ParsedRecordDto parsedRecordDto = quickMarcToParsedRecordConverter.convert(quickMarcJson);
      handlePutRequest(getResourceByIdPath(CM_RECORDS, id), JsonObject.mapFrom(parsedRecordDto), headers)
        .thenAccept(future::complete)
        .exceptionally(e -> {
          future.completeExceptionally(e);
          return null;
        });
    } catch (Exception e) {
      future.completeExceptionally(e);
    }
    return future;
  }
}
