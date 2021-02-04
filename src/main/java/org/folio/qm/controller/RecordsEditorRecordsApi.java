package org.folio.qm.controller;

import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.rest.resource.RecordsApi;
import org.folio.qm.service.MarcRecordsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping(value = "/records-editor")
public class RecordsEditorRecordsApi implements RecordsApi {

  private final MarcRecordsService marcRecordsService;

  public RecordsEditorRecordsApi(MarcRecordsService marcRecordsService) {
    this.marcRecordsService = marcRecordsService;
  }

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
}
