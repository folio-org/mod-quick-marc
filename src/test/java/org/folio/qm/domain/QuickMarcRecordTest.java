package org.folio.qm.domain;

import static org.folio.qm.util.MarcRecordModifier.TAG_001;
import static org.folio.qm.util.MarcRecordModifier.TAG_999;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.UUID;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.model.BaseQuickMarcRecord;
import org.folio.qm.domain.model.FolioRecord;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.folio.qm.exception.ConverterException;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.impl.MarcFactoryImpl;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class QuickMarcRecordTest {

  private static final String TAG_005 = "005";
  private static final String TAG_100 = "100";
  private static final String TAG_245 = "245";
  private static final MarcFactory MARC_FACTORY = new MarcFactoryImpl();
  @Mock
  private BaseQuickMarcRecord source;
  @Mock
  private FolioRecord folioRecord;

  @Test
  void getFolioRecordId_shouldReturnId_whenFolioRecordExists() {
    var id = UUID.randomUUID().toString();
    when(folioRecord.getId()).thenReturn(id);
    var quickMarcRecord = QuickMarcRecord.builder()
      .folioRecord(folioRecord)
      .build();

    var result = quickMarcRecord.getFolioRecordId();

    assertEquals(id, result);
  }

  @Test
  void getFolioRecordId_shouldReturnNull_whenFolioRecordIsNull() {
    var result = new QuickMarcRecord().getFolioRecordId();

    assertNull(result);
  }

  @Test
  void getFolioRecordHrid_shouldReturnHrid_whenFolioRecordExists() {
    var hrid = "hr123456";
    when(folioRecord.getHrid()).thenReturn(hrid);
    var quickMarcRecord = QuickMarcRecord.builder()
      .folioRecord(folioRecord)
      .build();

    var result = quickMarcRecord.getFolioRecordHrid();

    assertEquals(hrid, result);
  }

  @Test
  void getFolioRecordHrid_shouldReturnNull_whenFolioRecordIsNull() {
    var result = new QuickMarcRecord().getFolioRecordHrid();

    assertNull(result);
  }

  @Test
  void getParsedContent_shouldReturnExistingParsedContent_whenAlreadySet() {
    var parsedContent = new JsonObject().put("key", "value");
    var quickMarcRecord = QuickMarcRecord.builder()
      .parsedContent(parsedContent)
      .build();

    var result = quickMarcRecord.getParsedContent();

    assertSame(parsedContent, result);
  }

  @Test
  void getParsedContent_shouldBuildParsedContent_whenParsedContentIsNull() {
    var field = new FieldItem();
    field.setTag(TAG_245);
    when(source.getFields()).thenReturn(List.of(field));

    var marcRecord = createRecordWithTags(TAG_001, TAG_245);
    var quickMarcRecord = QuickMarcRecord.builder()
      .marcRecord(marcRecord)
      .source(source)
      .build();

    var result = quickMarcRecord.getParsedContent();

    assertNotNull(result);
    assertNotNull(result.getJsonArray("fields"));
  }

  @Test
  void buildParsedContent_shouldPlace001First_when001NotPresentInSource() {
    var field245 = new FieldItem().tag(TAG_245);
    when(source.getFields()).thenReturn(List.of(field245));

    var marcRecord = createRecordWithTags(TAG_245, TAG_001);
    var quickMarcRecord = QuickMarcRecord.builder()
      .marcRecord(marcRecord)
      .source(source)
      .build();

    quickMarcRecord.buildParsedContent();

    var fields = quickMarcRecord.getParsedContent().getJsonArray("fields");
    var firstTag = fields.getJsonObject(0).fieldNames().iterator().next();
    assertEquals(TAG_001, firstTag);
  }

  @Test
  void buildParsedContent_shouldRespectSourceFieldOrder() {
    var field100 = new FieldItem().tag(TAG_100);
    var field245 = new FieldItem().tag(TAG_245);
    when(source.getFields()).thenReturn(List.of(field100, field245));

    var marcRecord = createRecordWithTags(TAG_245, TAG_100);
    var quickMarcRecord = QuickMarcRecord.builder()
      .marcRecord(marcRecord)
      .source(source)
      .build();

    quickMarcRecord.buildParsedContent();

    var fields = quickMarcRecord.getParsedContent().getJsonArray("fields");
    var firstTag = fields.getJsonObject(0).fieldNames().iterator().next();
    var secondTag = fields.getJsonObject(1).fieldNames().iterator().next();

    assertEquals(TAG_100, firstTag);
    assertEquals(TAG_245, secondTag);
  }

  @Test
  void buildParsedContent_shouldAppend999AtEnd() {
    var field245 = new FieldItem().tag(TAG_245);
    when(source.getFields()).thenReturn(List.of(field245));

    var marcRecord = createRecordWithTags(TAG_245, TAG_999);
    var quickMarcRecord = QuickMarcRecord.builder()
      .marcRecord(marcRecord)
      .source(source)
      .build();

    quickMarcRecord.buildParsedContent();

    var fields = quickMarcRecord.getParsedContent().getJsonArray("fields");
    var lastTag = fields.getJsonObject(fields.size() - 1)
      .fieldNames().iterator().next();
    assertEquals(TAG_999, lastTag);
  }

  @Test
  void buildParsedContent_shouldAppend999AtTheTop() {
    var field999 = new FieldItem().tag(TAG_999);
    var field245 = new FieldItem().tag(TAG_245);
    var field001 = new FieldItem().tag(TAG_001);
    var field005 = new FieldItem().tag(TAG_005);
    when(source.getFields()).thenReturn(List.of(field999, field245, field001, field005));

    var marcRecord = createRecordWithTags(TAG_245, TAG_999, TAG_005, TAG_001);
    var quickMarcRecord = QuickMarcRecord.builder()
      .marcRecord(marcRecord)
      .source(source)
      .build();

    quickMarcRecord.buildParsedContent();

    var fields = quickMarcRecord.getParsedContent().getJsonArray("fields");
    assertEquals(TAG_999, getTag(fields.getJsonObject(0)));
    assertEquals(TAG_245, getTag(fields.getJsonObject(1)));
    assertEquals(TAG_001, getTag(fields.getJsonObject(2)));
    assertEquals(TAG_005, getTag(fields.getJsonObject(3)));
  }

  @Test
  void buildParsedContent_shouldThrowConverterException_whenMarcWriterFails() {
    var quickMarcRecord = QuickMarcRecord.builder()
      .marcRecord(null)
      .source(source)
      .build();

    assertThrows(ConverterException.class, quickMarcRecord::buildParsedContent);
  }

  private Record createRecordWithTags(String... tags) {
    var newRecord = MARC_FACTORY.newRecord();

    for (String tag : tags) {
      if (tag.compareTo("010") < 0) { // 001–009
        var field = MARC_FACTORY.newControlField(tag, "value");
        newRecord.addVariableField(field);
      } else {
        var field = MARC_FACTORY.newDataField(tag, ' ', ' ');
        field.addSubfield(MARC_FACTORY.newSubfield('a', "value"));
        newRecord.addVariableField(field);
      }
    }
    return newRecord;
  }

  private String getTag(JsonObject field) {
    return field.fieldNames().stream().findFirst().orElse(null);
  }
}
