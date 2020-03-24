package org.folio.converter;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.ParsedRecord;
import org.folio.srs.model.Record;
import org.marc4j.MarcJsonReader;
import org.marc4j.MarcReader;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import io.vertx.core.json.JsonObject;

@Component
public class RecordToQuickMarcConverter implements Converter<Record, QuickMarcJson> {

  private static Map<>

  @Override
  public QuickMarcJson convert(Record record) {

    QuickMarcJson quickMarcJson = new QuickMarcJson();
    quickMarcJson.setId(record.getId());
    ParsedRecord parsedRecord = record.getParsedRecord();
    InputStream input = IOUtils.toInputStream(JsonObject.mapFrom(parsedRecord).encode(), StandardCharsets.UTF_8);
    MarcReader reader = new MarcJsonReader(input);

    try {
      quickMarcJson.setId(record.getId());
      org.marc4j.marc.Record rec = reader.next();
      quickMarcJson.setLeader(rec.getLeader().marshal());
      rec.getControlFields().forEach(field -> {
        org.folio.rest.jaxrs.model.Record r = new org.folio.rest.jaxrs.model.Record();
        String tag = field.getTag();
        r.setTag(field.getTag());
        if ("008".equals(tag)) {
          r.getIndicators().add(field.getData());
        } else {
          r.getIndicators().add(field.getData());
        }
        quickMarcJson.getRecords().add(r);

      });

      rec.getDataFields().forEach(data -> {
        org.folio.rest.jaxrs.model.Record r = new org.folio.rest.jaxrs.model.Record();
        r.setTag(data.getTag());
        r.getIndicators().add(data.getIndicator1());
        r.getIndicators().add(data.getIndicator2());
        r.setContent(data.getSubfieldsAsString(""));
        quickMarcJson.getRecords().add(r);
      });

    } catch(Exception e) {

    }

    return quickMarcJson;
  }


}
