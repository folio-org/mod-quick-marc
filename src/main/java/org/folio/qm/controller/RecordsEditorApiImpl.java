package org.folio.qm.controller;

import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.qm.domain.dto.CreationStatus;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.rest.resource.RecordsEditorApi;
import org.folio.qm.service.MarcRecordsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/records-editor")
@RequiredArgsConstructor
public class RecordsEditorApiImpl implements RecordsEditorApi {

  private final MarcRecordsService marcRecordsService;

  @Override
  public ResponseEntity<QuickMarc> getRecordByExternalId(UUID externalId, String lang) {
    var quickMarc = marcRecordsService.findByExternalId(externalId);
    return ResponseEntity.ok(quickMarc);
  }

  @Override
  public ResponseEntity<CreationStatus> getRecordCreationStatus(UUID qmRecordId) {
    return ResponseEntity.ok(marcRecordsService.getCreationStatusByQmRecordId(qmRecordId));
  }

  @Override
  public ResponseEntity<CreationStatus> recordsPost(@Valid QuickMarc quickMarc) {
    CreationStatus status = marcRecordsService.createNewRecord(quickMarc);
    return ResponseEntity.status(CREATED).body(status);
  }

}
