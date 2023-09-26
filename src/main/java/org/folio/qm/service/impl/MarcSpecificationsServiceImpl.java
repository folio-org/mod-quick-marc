package org.folio.qm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.domain.entity.MarcSpecification;
import org.folio.qm.domain.entity.RecordType;
import org.folio.qm.domain.repository.MarcSpecificationRepository;
import org.folio.qm.service.MarcSpecificationsService;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class MarcSpecificationsServiceImpl implements MarcSpecificationsService {

  private static final String MESSAGE_TEMPLATE = "Marc specification for [%s] record type and [%s] field tag not found";

  private final MarcSpecificationRepository marcSpecificationRepository;

  @Override
  public MarcSpecification findByRecordTypeAndFieldTag(RecordType recordType, String fieldTag) {
    return marcSpecificationRepository.findByRecordTypeAndFieldTag(recordType, fieldTag)
        .orElseThrow(() -> new NotFoundException(String.format(MESSAGE_TEMPLATE, recordType, fieldTag)));
  }
}
