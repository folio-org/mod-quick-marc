package org.folio.qm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

@UnitTest
class MarcRecordModifierTest {

  private static final MarcFactory FACTORY = MarcFactory.newInstance();

  @Test
  void shouldAdd999FieldSuccessfully() {
    var basicRecord = createBasicRecord();
    var externalId = "test-instance-id";

    var result = MarcRecordModifier.add999Field(basicRecord, externalId);

    assertTrue(result);
    var field999 = basicRecord.getVariableField("999");
    assertNotNull(field999);
    assertInstanceOf(DataField.class, field999);

    var dataField = (DataField) field999;
    assertEquals('f', dataField.getIndicator1());
    assertEquals('f', dataField.getIndicator2());
    assertEquals(externalId, dataField.getSubfield('i').getData());
  }

  @Test
  void shouldReplaceExisting999Field() {
    var basicRecord = createBasicRecord();
    var oldExternalId = "old-id";
    var newExternalId = "new-id";

    MarcRecordModifier.add999Field(basicRecord, oldExternalId);
    var result = MarcRecordModifier.add999Field(basicRecord, newExternalId);

    assertTrue(result);
    var fields999 = basicRecord.getVariableFields("999");
    assertEquals(1, fields999.size());

    var dataField = (DataField) fields999.getFirst();
    assertEquals(newExternalId, dataField.getSubfield('i').getData());
  }

  @Test
  void shouldAdd001FieldSuccessfully() {
    var basicRecord = createBasicRecord();
    var hrid = "in00000001";

    var result = MarcRecordModifier.add001Field(basicRecord, hrid);

    assertTrue(result);
    var field001 = basicRecord.getVariableField("001");
    assertNotNull(field001);
    var fieldContent = field001.toString().trim();
    assertTrue(fieldContent.contains(hrid));
  }

  @Test
  void shouldReplaceExisting001Field() {
    var basicRecord = createBasicRecord();
    basicRecord.addVariableField(FACTORY.newControlField("001", "old-hrid"));

    var newHrid = "new-hrid";
    var result = MarcRecordModifier.add001Field(basicRecord, newHrid);

    assertTrue(result);
    var fields001 = basicRecord.getVariableFields("001");
    assertEquals(1, fields001.size());
    var fieldContent = fields001.getFirst().toString().trim();
    assertTrue(fieldContent.contains(newHrid));
  }

  @Test
  void shouldConvertToJsonContent() {
    var basicRecord = createBasicRecord();
    MarcRecordModifier.add999Field(basicRecord, "test-id");

    var jsonContent = MarcRecordModifier.toJsonContent(basicRecord);

    assertNotNull(jsonContent);
    assertFalse(jsonContent.isEmpty());
    assertTrue(jsonContent.contains("999"));
  }

  @Test
  void shouldConvertFromJsonContent() {
    var basicRecord = createBasicRecord();
    MarcRecordModifier.add999Field(basicRecord, "test-id");
    var jsonContent = MarcRecordModifier.toJsonContent(basicRecord);

    var parsedRecord = MarcRecordModifier.fromJsonContent(jsonContent);

    assertNotNull(parsedRecord);
    var field999 = parsedRecord.getVariableField("999");
    assertNotNull(field999);
  }

  @Test
  void shouldThrowExceptionForInvalidJsonContent() {
    var invalidJson = "invalid json";

    var exception = assertThrows(RuntimeException.class,
      () -> MarcRecordModifier.fromJsonContent(invalidJson));

    assertNotNull(exception);
  }

  private Record createBasicRecord() {
    var basicRecord = FACTORY.newRecord();
    basicRecord.setLeader(FACTORY.newLeader("00000cam a2200000 a 4500"));

    var field245 = FACTORY.newDataField("245", '1', '0');
    field245.addSubfield(FACTORY.newSubfield('a', "Test Title"));
    basicRecord.addVariableField(field245);

    return basicRecord;
  }
}
