package org.folio.qm.controller;

import lombok.RequiredArgsConstructor;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.folio.qm.domain.dto.ValidationResult;
import org.folio.qm.rest.resource.RecordsValidatorApi;
import org.folio.qm.service.ValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(value = "/records-editor")
@RequiredArgsConstructor
public class RecordsValidatorApiImpl implements RecordsValidatorApi {

  private final ValidationService validationService;

  @Override
  public ResponseEntity<ValidationResult> validatePost(ValidatableRecord validatableRecord) {
    var validationIssues = validationService.validate(validatableRecord);
    return ResponseEntity.ok(new ValidationResult().issues(validationIssues));
  }
}
