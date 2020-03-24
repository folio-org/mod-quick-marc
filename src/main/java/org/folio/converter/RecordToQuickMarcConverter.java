package org.folio.converter;

import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RecordToQuickMarcConverter implements Converter<Record, QuickMarcJson> {

  @Override
  public QuickMarcJson convert(Record record) {
    return new QuickMarcJson().withId(record.getId());
  }
}
