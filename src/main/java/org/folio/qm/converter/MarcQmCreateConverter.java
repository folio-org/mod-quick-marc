package org.folio.qm.converter;

import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.mapper.MarcTypeMapper;
import org.jspecify.annotations.NonNull;
import org.marc4j.marc.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

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
