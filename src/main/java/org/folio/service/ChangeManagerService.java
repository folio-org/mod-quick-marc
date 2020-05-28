package org.folio.service;

import static org.folio.util.Constants.INSTANCE_ID;
import static org.folio.util.ResourcePathResolver.*;
import static org.folio.util.ServiceUtils.buildQuery;

import io.vertx.core.Context;
import io.vertx.core.json.JsonObject;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;
import org.folio.converter.ParsedRecordToQuickMarcConverter;
import org.folio.converter.QuickMarcToParsedRecordConverter;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.AdditionalInfo;
import org.folio.srs.model.ExternalIdsHolder;
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
        return parsedRecordToQuickMarcConverter.convert(parsedRecordDto.getParsedRecord())
          .withParsedRecordDtoId(parsedRecordDto.getId())
          .withInstanceId(parsedRecordDto.getExternalIdsHolder().getInstanceId())
          .withSuppressDiscovery(parsedRecordDto.getAdditionalInfo().getSuppressDiscovery());
      });
  }

  @Override
  public CompletableFuture<Void> putMarcRecordById(String id, QuickMarcJson quickMarcJson, Context context,
    Map<String, String> headers) {
    CompletableFuture<Void> future = new VertxCompletableFuture<>(context);
    try {
      ParsedRecordDto parsedRecordDto = new ParsedRecordDto()
        .withRecordType(ParsedRecordDto.RecordType.MARC)
        .withParsedRecord(quickMarcToParsedRecordConverter.convert(quickMarcJson))
        .withId(quickMarcJson.getParsedRecordDtoId())
        .withExternalIdsHolder(new ExternalIdsHolder().withInstanceId(quickMarcJson.getInstanceId()))
        .withAdditionalInfo(new AdditionalInfo().withSuppressDiscovery(quickMarcJson.getSuppressDiscovery()));
      handlePutRequest(getResourceByIdPath(CM_RECORDS, id), JsonObject.mapFrom(parsedRecordDto), context, headers)
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

  @Autowired
  public void setParsedRecordToQuickMarcConverter(ParsedRecordToQuickMarcConverter parsedRecordToQuickMarcConverter) {
    this.parsedRecordToQuickMarcConverter = parsedRecordToQuickMarcConverter;
  }

  @Autowired
  public void setQuickMarcToParsedRecordConverter(QuickMarcToParsedRecordConverter quickMarcToParsedRecordConverter) {
    this.quickMarcToParsedRecordConverter = quickMarcToParsedRecordConverter;
  }
}
