package org.folio.qm.controller;

import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.qm.domain.dto.AuthoritySearchParameter;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.rest.resource.RecordsEditorApi;
import org.folio.qm.service.FetchRecordService;
import org.folio.qm.service.LinksSuggestionService;
import org.folio.qm.service.MarcRecordsService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(value = "/records-editor")
@RequiredArgsConstructor
public class RecordsEditorApiImpl implements RecordsEditorApi {

  private final MarcRecordsService marcRecordsService;
  private final FetchRecordService fetchRecordService;
  private final LinksSuggestionService linksSuggestionService;

  @Override
  public ResponseEntity<QuickMarcView> createNewRecord(@Valid QuickMarcCreate quickMarc) {
    var newRecord = marcRecordsService.createRecord(quickMarc);
    return ResponseEntity.status(CREATED).body(newRecord);
  }

  @Override
  public ResponseEntity<QuickMarcView> getRecordByExternalId(UUID externalId) {
    var quickMarc = fetchRecordService.fetchByExternalId(externalId);
    return ResponseEntity.ok(quickMarc);
  }

  @Override
  public ResponseEntity<QuickMarcView> linksSuggestionPost(QuickMarcView quickMarcView,
                                                           AuthoritySearchParameter authoritySearchParameter,
                                                           Boolean ignoreAutoLinkingEnabled) {
    var quickMarc = linksSuggestionService.suggestLinks(
      quickMarcView,
      authoritySearchParameter,
      ignoreAutoLinkingEnabled);
    return ResponseEntity.ok(quickMarc);
  }

  @Override
  public ResponseEntity<Void> putRecord(UUID id, QuickMarcEdit quickMarc) {
    marcRecordsService.updateById(id, quickMarc);
    return ResponseEntity.accepted().build();
  }
}
