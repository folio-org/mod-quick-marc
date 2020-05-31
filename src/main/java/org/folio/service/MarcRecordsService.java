package org.folio.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.folio.rest.jaxrs.model.QuickMarcJson;

import io.vertx.core.Context;

public interface MarcRecordsService {

  /**
   * This method returns QuickMarcJson record from SRS by corresponding instance's id
   *
   * @param instanceId instance's id
   * @param context    Vert.X context
   * @param headers    OKAPI headers
   * @return {@link QuickMarcJson} record
   */
  CompletableFuture<QuickMarcJson> getMarcRecordByInstanceId(String instanceId, Context context, Map<String, String> headers);

  /**
   * This method updates QuickMarcJson record
   *
   * @param id            DTO id
   * @param quickMarcJson QuickMarcJson object
   * @param context       Vert.X context
   * @param headers       OKAPI headers
   * @return {@link QuickMarcJson} record
   */
  CompletableFuture<Void> putMarcRecordById(String id, QuickMarcJson quickMarcJson, Context context, Map<String, String> headers);
}
