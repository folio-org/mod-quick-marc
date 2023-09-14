package org.folio.qm.controller;

import lombok.RequiredArgsConstructor;
import org.folio.qm.domain.dto.MarcSpec;
import org.folio.qm.domain.entity.MarcSpecification;
import org.folio.qm.domain.entity.RecordType;
import org.folio.qm.rest.resource.MarcSpecificationsApi;
import org.folio.qm.service.MarcSpecificationsService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(value = "/marc-specifications")
@RequiredArgsConstructor
public class MarcSpecificationsApiImpl implements MarcSpecificationsApi {

  private final MarcSpecificationsService marcSpecificationsService;

  @Override
  public ResponseEntity<MarcSpec> getMarcSpecification(String recordType, String fieldTag) {
    MarcSpecification marcSpecification =
      marcSpecificationsService.findByMarcFormatAndFieldTag(RecordType.fromValue(recordType), fieldTag);
    return ResponseEntity.ok(marcSpecification.getMarcSpec());
  }
}
