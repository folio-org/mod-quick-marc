package org.folio.qm.controller;

import static org.springframework.http.HttpStatus.CREATED;

import java.util.UUID;
import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.domain.dto.ActionStatusDto;
import org.folio.qm.rest.resource.RecordsApi;
import org.folio.qm.service.MarcRecordsService;

@RestController
@RequestMapping(value = "/records-editor")
@RequiredArgsConstructor
public class RecordsEditorApiImpl implements RecordsApi {

  private final MarcRecordsService marcRecordsService;

  @Override
  public ResponseEntity<ActionStatusDto> deleteRecordByExternalId(UUID id) {
    return ResponseEntity.ok(marcRecordsService.deleteRecordByExternalId(id));
  }

  @Override
  public ResponseEntity<ActionStatusDto> getActionStatus(UUID actionId) {
    return ResponseEntity.ok(marcRecordsService.getActionStatusByActionId(actionId));
  }

  @Override
  public ResponseEntity<QuickMarc> getRecordByExternalId(UUID externalId) {
    return ResponseEntity.ok(marcRecordsService.findRecordByExternalId(externalId));
  }

  @Override
  public ResponseEntity<ActionStatusDto> postRecord(@Valid QuickMarc quickMarc) {
    return ResponseEntity.status(CREATED).body(marcRecordsService.createRecord(quickMarc));
  }

  @Override
  public ResponseEntity<ActionStatusDto> putRecord(UUID id, QuickMarc quickMarc) {
    return ResponseEntity.ok(marcRecordsService.updateById(id, quickMarc));
  }

}
