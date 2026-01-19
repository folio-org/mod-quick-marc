package org.folio.qm.convertion.converter;

import static org.folio.qm.convertion.elements.Constants.DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD;
import static org.folio.qm.util.MarcUtils.encodeToMarcDateTime;
import static org.folio.qm.util.MarcUtils.getFieldByTag;
import static org.folio.qm.util.MarcUtils.restoreBlanks;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.folio.qm.convertion.field.MarcFieldsConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.model.BaseQuickMarcRecord;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BaseQuickMarcRecordToMarc4jRecordConverter implements Converter<BaseQuickMarcRecord, Record> {

  private final MarcFactory factory;
  private final MarcFieldsConverter fieldsConverter;

  @Override
  public Record convert(BaseQuickMarcRecord source) {
    var marcRecord = factory.newRecord();
    updateRecordTimestamp(source);
    fieldsConverter.convertQmFields(source.getFields(), source.getMarcFormat())
      .forEach(marcRecord::addVariableField);

    var leaderString = source.getLeader();
    if (StringUtils.isNotBlank(leaderString)) {
      marcRecord.setLeader(factory.newLeader(restoreBlanks(leaderString)));
    }
    return marcRecord;
  }

  private void updateRecordTimestamp(BaseQuickMarcRecord quickMarc) {
    final var currentTime = encodeToMarcDateTime(LocalDateTime.now());
    getFieldByTag(quickMarc, DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD)
      .ifPresentOrElse(field -> field.setContent(currentTime),
        () -> quickMarc.addFieldsItem(
          new FieldItem().tag(DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD).content(currentTime))
      );
  }
}
