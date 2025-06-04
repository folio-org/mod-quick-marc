package org.folio.qm.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.mapper.MarcTypeMapper;
import org.marc4j.marc.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class MarcQmCreateConverter extends MarcQmConverter<QuickMarcCreate> {

  public MarcQmCreateConverter(ObjectMapper objectMapper, MarcTypeMapper typeMapper,
                               Converter<BaseMarcRecord, Record> recordConverter) {
    super(objectMapper, typeMapper, recordConverter);
  }

  @Override
  public ParsedRecordDto convert(@NonNull QuickMarcCreate source) {
    return super.convert(updateRecordTimestamp(source));
  }
}
