package org.folio.qm.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.QuickMarcCreate;
import org.folio.qm.mapper.MarcTypeMapper;
import org.marc4j.marc.MarcFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class MarcQmCreateConverter extends MarcQmConverter<QuickMarcCreate> {

  public MarcQmCreateConverter(MarcFactory factory, ObjectMapper objectMapper, MarcTypeMapper typeMapper,
                               MarcFieldsConverter fieldsConverter) {
    super(factory, objectMapper, typeMapper, fieldsConverter);
  }

  @Override
  public ParsedRecordDto convert(@NonNull QuickMarcCreate source) {
    return super.convert(updateRecordTimestamp(source));
  }

}
