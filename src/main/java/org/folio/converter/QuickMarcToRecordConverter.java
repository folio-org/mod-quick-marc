package org.folio.converter;

import static org.folio.converter.StringConstants.CONTENT;

import org.codehaus.jackson.map.ObjectMapper;
import org.folio.exeptions.ConversionException;
import org.folio.exeptions.WrongField008LengthException;
import org.folio.rest.jaxrs.model.Field;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.RawRecord;
import org.folio.srs.model.Record;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.impl.MarcFactoryImpl;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class QuickMarcToRecordConverter implements Converter<QuickMarcJson, Record> {
  private static final int ITEM008_LENGTH = 40;

  private MarcFactory factory = new MarcFactoryImpl();

  @Override
  public Record convert(QuickMarcJson quickMarcJson) {
    Record record = new Record();

    org.marc4j.marc.Record marcRecord = factory.newRecord();
    Leader leader = factory.newLeader(quickMarcJson.getLeader());
    marcRecord.setLeader(leader);
    quickMarcJson.getFields().forEach(f -> restoreRecord(f, marcRecord));
    RawRecord rawRecord = new RawRecord();
    rawRecord.setId(quickMarcJson.getId());
    rawRecord.setContent(marcRecordToString(marcRecord));
    record.setRawRecord(rawRecord);

    return record;
  }

  private void restoreRecord(Field source, org.marc4j.marc.Record dest){
    String tag = source.getTag();
    String data = source.getContent();
    if (Pattern.compile("[0][0][1-9]$").matcher(tag).matches()){
      ControlField field = factory.newControlField();
      field.setTag(tag);
      if ("008".equals(tag)) {
        field.setData(restoreField008(source.getContent()));
      } else {
        field.setData(data);
      }
      dest.getControlFields().add(field);
    } else {
      DataField dataField = factory.newDataField();
      dataField.setTag(source.getTag());
      dataField.getSubfields().addAll(stringToSubfields(source.getContent()));
      dataField.setIndicator1(source.getIndicators().get(0).toString().charAt(0));
      dataField.setIndicator2(source.getIndicators().get(1).toString().charAt(0));
      dest.getDataFields().add(dataField);
    }
  }

  private String restoreField008(String s) {
    try{
      Map<String, Object> map = new ObjectMapper().readValue(s, LinkedHashMap.class);
      ContentType contentType = ContentType.getByName(map.get(CONTENT).toString());
      String result = Field008RestoreFactory.getStrategy(contentType).restore(map);
      if (result.length() != ITEM008_LENGTH) throw new WrongField008LengthException();
      return result;
    } catch (IOException e) {
      throw new ConversionException(e.getMessage());
    }
  }

  private List<Subfield> stringToSubfields(String s){
    List<Subfield> result = new ArrayList<>();
    stringToTokens(s).forEach(token -> {
      Subfield subfield = factory.newSubfield();
      subfield.setCode(token.charAt(0));
      subfield.setData((token.charAt(1) == ' ')? token.substring(2): token.substring(1));
      result.add(subfield);
    });
    return result;
  }

  private List<String> stringToTokens(String s) {
    List<String> result = new ArrayList<>();
    String[] tokens = s.split("[$]");
    for (int i = 1; i < tokens.length; i++) {
      String token = tokens[i];
      if ((token.charAt(token.length() - 1) == ' ') && (i < tokens.length - 1))
        token = token.substring(0, token.length() - 1);
      result.add(token);
    }
    return result;
  }

  private String marcRecordToString(org.marc4j.marc.Record record){
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    MarcWriter writer = new MarcStreamWriter(outputStream, "UTF-8");
    writer.write(record);
    return new String(outputStream.toByteArray());
  }
}
