package org.folio.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.folio.rest.jaxrs.model.QuickMarcJson;

import io.vertx.core.Context;

public interface MarcRecordsService  {

  /**
   * This method returns QuickMarcJson record from SRS by given record's id
   *
   * @param id record's id
   * @param context Vert.X context
   * @param headers OKAPI headers
   * @return {@link QuickMarcJson} record
   */
  CompletableFuture<QuickMarcJson> getMarcRecordById(String id, Context context, Map<String, String> headers);

  /**
   * This method returns QuickMarcJson record from SRS by corresponding instance's id
   *
   * @param instanceId instance's id
   * @param context Vert.X context
   * @param headers OKAPI headers
   * @return {@link QuickMarcJson} record
   */
  CompletableFuture<QuickMarcJson> getMarcRecordByInstanceId(String instanceId, Context context, Map<String, String> headers);
}
