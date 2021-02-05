package org.folio.qm.controller;

import java.util.UUID;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.rest.resource.RecordsApi;
import org.folio.qm.service.MarcRecordsService;

@RestController
@RequestMapping(value = "/records-editor")
@RequiredArgsConstructor
public class RecordsEditorRecordsApi implements RecordsApi {

  private final MarcRecordsService marcRecordsService;

  @Override
  public ResponseEntity<QuickMarc> getRecordByInstanceId(UUID instanceId, String lang) {
    var quickMarc = marcRecordsService.findByInstanceId(instanceId);
    return ResponseEntity.ok(quickMarc);
  }

  @Override
  public ResponseEntity<Void> recordsPost(@Valid QuickMarc quickMarc) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  @Override
  public ResponseEntity<Void> putRecord(UUID id, QuickMarc quickMarc) {
    marcRecordsService.updateById(id, quickMarc);
    return ResponseEntity.accepted().build();
  }

  @Override
  public ResponseEntity<CreationStatus> getRecordCreationStatus(UUID qmRecordId) {
    return ResponseEntity.ok(marcRecordsService.getCreationStatusByQmRecordId(qmRecordId));
  }
}
