package org.folio.qm.convertion.converter;

import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ValidatableRecordToBaseMarcRecordConverter implements Converter<ValidatableRecord, BaseMarcRecord> {

  @Override
  public BaseMarcRecord convert(ValidatableRecord validatableRecord) {
    var fields = validatableRecord.getFields().stream()
      .map(field -> new FieldItem()
        .tag(field.getTag())
        .content(field.getContent())
        .indicators(field.getIndicators()))
      .toList();

    var baseMarcRecord = new BaseMarcRecord();
    baseMarcRecord.setLeader(validatableRecord.getLeader());
    baseMarcRecord.setMarcFormat(validatableRecord.getMarcFormat());
    baseMarcRecord.setFields(fields);
    return baseMarcRecord;
  }
}
