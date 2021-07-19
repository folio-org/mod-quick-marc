package org.folio.qm.controller;

import static org.springframework.http.HttpStatus.CREATED;

import java.util.UUID;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.rest.resource.RecordsApi;
import org.folio.qm.rest.resource.RecordsAsyncApi;
import org.folio.qm.service.CacheService;
import org.folio.qm.service.MarcRecordsService;

@RestController
@RequestMapping(value = "/records-editor")
@RequiredArgsConstructor
public class RecordsEditorRecordsApi implements RecordsApi, RecordsAsyncApi {

  private final MarcRecordsService marcRecordsService;
  private final CacheService<DeferredResult> cacheService;

  @Override
  public ResponseEntity<QuickMarc> getRecordByInstanceId(UUID instanceId, String lang) {
    var quickMarc = marcRecordsService.findByInstanceId(instanceId);
    return ResponseEntity.ok(quickMarc);
  }

  @Override
  public ResponseEntity<CreationStatus> recordsPost(@Valid QuickMarc quickMarc) {
    CreationStatus status = marcRecordsService.createNewInstance(quickMarc);
    return ResponseEntity.status(CREATED).body(status);
  }

  @Override
  public DeferredResult<ResponseEntity<Void>> putRecord(UUID id, QuickMarc quickMarc) {
    var deferredResult = new DeferredResult<ResponseEntity<Void>>();
    cacheService.putToCache(id.toString(), deferredResult);
    marcRecordsService.updateById(id, quickMarc);
    return deferredResult;
  }

  @Override
  public ResponseEntity<CreationStatus> getRecordCreationStatus(UUID qmRecordId) {
    return ResponseEntity.ok(marcRecordsService.getCreationStatusByQmRecordId(qmRecordId));
  }
}
