package org.folio.qm.converter;

import static org.folio.qm.util.MarcUtils.restoreBlanks;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Primary
@Component
@RequiredArgsConstructor
public class MarcQmToMarc4jConverter implements Converter<BaseMarcRecord, Record> {

  private final MarcFactory factory;
  private final MarcFieldsConverter fieldsConverter;

  @Override
  public Record convert(BaseMarcRecord source) {
    var marcRecord = factory.newRecord();

    fieldsConverter.convertQmFields(source.getFields(), source.getMarcFormat())
      .forEach(marcRecord::addVariableField);

    var leaderString = source.getLeader();
    if (StringUtils.isNotBlank(leaderString)) {
      marcRecord.setLeader(factory.newLeader(restoreBlanks(leaderString)));
    }
    return marcRecord;
  }
}
