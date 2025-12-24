package org.folio.qm.converter;

import static org.folio.qm.util.MarcUtils.masqueradeBlanks;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.marc4j.marc.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuickMarcRecordConverter implements Converter<QuickMarcRecord, QuickMarcView> {

  private final MarcFieldsConverter fieldsConverter;

  @Override
  public QuickMarcView convert(@NonNull QuickMarcRecord source) {
    var marcRecord = source.getMarcRecord();

    var format = source.getMarcFormat();
    var leader = convertLeader(marcRecord);
    var fields = fieldsConverter.convertDtoFields(marcRecord.getVariableFields(), marcRecord.getLeader(), format);

    return new QuickMarcView()
      .leader(leader)
      .fields(fields)
      .marcFormat(format)
      .parsedRecordId(source.getParsedRecordId())
      .parsedRecordDtoId(source.getParsedRecordDtoId())
      .sourceVersion(source.getSourceVersion())
      .externalId(UUID.fromString(source.getFolioRecordId()))
      .externalHrid(source.getFolioRecordHrid())
      .suppressDiscovery(source.isSuppressDiscovery());
  }

  private String convertLeader(Record marcRecord) {
    return masqueradeBlanks(marcRecord.getLeader().marshal());
  }
}
