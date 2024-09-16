package org.folio.qm.converter;

import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.folio.qm.domain.dto.ValidatableRecordFieldsInner;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MarcQmToValidatableRecordConverter implements Converter<BaseMarcRecord, ValidatableRecord> {

  @Override
  public ValidatableRecord convert(BaseMarcRecord baseMarcRecord) {
    var validatableRecordFields = baseMarcRecord.getFields().stream()
      .map(fieldItem -> new ValidatableRecordFieldsInner()
        .tag(fieldItem.getTag())
        .content(fieldItem.getContent())
        .indicators(fieldItem.getIndicators()))
      .toList();

    var validatableRecord = new ValidatableRecord();
    validatableRecord.setMarcFormat(baseMarcRecord.getMarcFormat());
    validatableRecord.setLeader(baseMarcRecord.getLeader());
    validatableRecord.setFields(validatableRecordFields);
    return validatableRecord;
  }
}
