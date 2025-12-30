package org.folio.qm.controller;

import static org.springframework.http.HttpStatus.CREATED;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.qm.domain.dto.AuthoritySearchParameter;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.folio.qm.domain.dto.ValidationResult;
import org.folio.qm.rest.resource.RecordsEditorApi;
import org.folio.qm.service.change.ChangeRecordServiceRegistry;
import org.folio.qm.service.fetch.FetchRecordService;
import org.folio.qm.service.links.LinksSuggestionService;
import org.folio.qm.service.validation.ValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class RecordsEditorApiImpl implements RecordsEditorApi {

  private final ValidationService validationService;
  private final FetchRecordService fetchRecordService;
  private final LinksSuggestionService linksSuggestionService;
  private final ChangeRecordServiceRegistry changeRecordServiceRegistry;

  @Override
  public ResponseEntity<QuickMarcView> createNewRecord(QuickMarcCreate quickMarc) {
    var changeRecordService = changeRecordServiceRegistry.get(quickMarc.getMarcFormat());
    var newRecord = changeRecordService.create(quickMarc);
    return ResponseEntity.status(CREATED).body(newRecord);
  }

  @Override
  public ResponseEntity<QuickMarcView> getRecordByExternalId(UUID externalId) {
    var quickMarc = fetchRecordService.fetchByExternalId(externalId);
    return ResponseEntity.ok(quickMarc);
  }

  @Override
  public ResponseEntity<QuickMarcView> suggestLinks(QuickMarcView quickMarcView,
                                                    AuthoritySearchParameter authoritySearchParameter,
                                                    Boolean ignoreAutoLinkingEnabled) {
    var quickMarc = linksSuggestionService.suggestLinks(
      quickMarcView,
      authoritySearchParameter,
      ignoreAutoLinkingEnabled);
    return ResponseEntity.ok(quickMarc);
  }

  @Override
  public ResponseEntity<Void> updateRecord(UUID id, QuickMarcEdit quickMarc) {
    var changeRecordService = changeRecordServiceRegistry.get(quickMarc.getMarcFormat());
    changeRecordService.update(id, quickMarc);
    return ResponseEntity.accepted().build();
  }

  @Override
  public ResponseEntity<ValidationResult> validateRecord(ValidatableRecord validatableRecord) {
    var validationIssues = validationService.validate(validatableRecord);
    return ResponseEntity.ok(new ValidationResult().issues(validationIssues));
  }
}
