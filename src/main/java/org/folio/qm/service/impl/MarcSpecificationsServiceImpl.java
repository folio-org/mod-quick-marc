package org.folio.qm.service.impl;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.domain.dto.MarcFormat;
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
  private static final Map<MarcFormat, RecordType> TYPE_MAP = Map.of(
    MarcFormat.AUTHORITY, RecordType.MARC_AUTHORITY,
    MarcFormat.BIBLIOGRAPHIC, RecordType.MARC_BIBLIOGRAPHIC,
    MarcFormat.HOLDINGS, RecordType.MARC_HOLDINGS
  );

  private final MarcSpecificationRepository marcSpecificationRepository;

  @Override
  public MarcSpecification findByMarcFormatAndFieldTag(MarcFormat marcFormat, String fieldTag) {
    RecordType recordType = TYPE_MAP.get(marcFormat);
    return marcSpecificationRepository.findByRecordTypeAndFieldTag(recordType, fieldTag)
        .orElseThrow(() -> new NotFoundException(String.format(MESSAGE_TEMPLATE, recordType, fieldTag)));
  }
}
