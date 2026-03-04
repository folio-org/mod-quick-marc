package org.folio.qm.convertion.converter;

import org.folio.qm.domain.dto.ValidatableRecord;
import org.folio.qm.domain.dto.ValidatableRecordFieldsInner;
import org.folio.qm.domain.model.BaseQuickMarcRecord;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class BaseQuickMarcRecordToValidatableRecordConverter
  implements Converter<BaseQuickMarcRecord, ValidatableRecord> {

  @Override
  public ValidatableRecord convert(BaseQuickMarcRecord baseMarcRecord) {
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
