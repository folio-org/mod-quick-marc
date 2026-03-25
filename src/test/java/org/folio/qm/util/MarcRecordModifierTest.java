package org.folio.qm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

  @ParameterizedTest
  @MethodSource("oclcNormalizationArguments")
  void shouldNormalize035Field(String given, String expected) {
    var basicRecord = createBasicRecord();
    var field = FACTORY.newDataField("035", ' ', ' ');
    field.addSubfield(FACTORY.newSubfield('a', given));
    basicRecord.addVariableField(field);

    MarcRecordModifier.normalize035Field(basicRecord);

    var updatedField = (DataField) basicRecord.getVariableField("035");
    assertEquals(expected, updatedField.getSubfield('a').getData());
  }

  @Test
  void shouldDeduplicate035Field() {
    var basicRecord = createBasicRecord();
    var field1 = FACTORY.newDataField("035", ' ', ' ');
    field1.addSubfield(FACTORY.newSubfield('a', "(OCoLC)12345"));
    basicRecord.addVariableField(field1);

    var field2 = FACTORY.newDataField("035", ' ', ' ');
    field2.addSubfield(FACTORY.newSubfield('a', "(OCoLC)12345"));
    basicRecord.addVariableField(field2);

    MarcRecordModifier.normalize035Field(basicRecord);

    assertEquals(1, basicRecord.getVariableFields("035").size());
  }

  @Test
  void shouldHandleMultipleSubfieldsIn035() {
    var basicRecord = createBasicRecord();

    var field1 = FACTORY.newDataField("035", ' ', ' ');
    field1.addSubfield(FACTORY.newSubfield('a', "(OCoLC)64758"));
    basicRecord.addVariableField(field1);

    var field2 = FACTORY.newDataField("035", ' ', ' ');
    field2.addSubfield(FACTORY.newSubfield('a', "(OCoLC)ocm000064758"));
    field2.addSubfield(FACTORY.newSubfield('k', "(OCoLC)976939443"));
    field2.addSubfield(FACTORY.newSubfield('k', "(OCoLC)1001261435"));
    field2.addSubfield(FACTORY.newSubfield('k', "(OCoLC)120194933"));
    basicRecord.addVariableField(field2);

    MarcRecordModifier.normalize035Field(basicRecord);

    var updatedField = (DataField) basicRecord.getVariableField("035");
    assertNotNull(updatedField);
    assertEquals(4, updatedField.getSubfields().size());
    assertEquals("(OCoLC)64758", updatedField.getSubfield('a').getData());
    var subfieldsK = updatedField.getSubfields('k');
    assertEquals(3, subfieldsK.size());
    assertEquals("(OCoLC)976939443", subfieldsK.get(0).getData());
    assertEquals("(OCoLC)1001261435", subfieldsK.get(1).getData());
    assertEquals("(OCoLC)120194933", subfieldsK.get(2).getData());
  }

  @Test
  void shouldNotNormalize035FieldWhenNoOclc() {
    var basicRecord = createBasicRecord();
    var field = FACTORY.newDataField("035", ' ', ' ');
    var originalValue = "other-prefix-12345";
    field.addSubfield(FACTORY.newSubfield('a', originalValue));
    basicRecord.addVariableField(field);

    MarcRecordModifier.normalize035Field(basicRecord);

    var updatedField = (DataField) basicRecord.getVariableField("035");
    assertEquals(originalValue, updatedField.getSubfield('a').getData());
  }

  @Test
  void shouldNotRemoveNonDuplicate035Fields() {
    var basicRecord = createBasicRecord();
    var field1 = FACTORY.newDataField("035", ' ', ' ');
    field1.addSubfield(FACTORY.newSubfield('a', "(OCoLC)12345"));
    basicRecord.addVariableField(field1);

    var field2 = FACTORY.newDataField("035", ' ', ' ');
    field2.addSubfield(FACTORY.newSubfield('a', "(OCoLC)67890"));
    basicRecord.addVariableField(field2);

    MarcRecordModifier.normalize035Field(basicRecord);

    assertEquals(2, basicRecord.getVariableFields("035").size());
  }

  @Test
  void shouldRemove003Field() {
    var basicRecord = createBasicRecord();
    basicRecord.addVariableField(FACTORY.newControlField("003", "some-source"));

    // Ensure added
    assertEquals(1, basicRecord.getVariableFields("003").size());

    MarcRecordModifier.remove003Field(basicRecord);

    assertEquals(0, basicRecord.getVariableFields("003").size());
  }

  @Test
  void shouldGet004ControlFieldData() {
    var basicRecord = createBasicRecord();

    // When no 004 present, expect null
    assertNull(MarcRecordModifier.get004ControlFieldData(basicRecord));

    // Add 004
    basicRecord.addVariableField(FACTORY.newControlField("004", "instanceHrid"));
    var result = MarcRecordModifier.get004ControlFieldData(basicRecord);
    assertEquals("instanceHrid", result);
  }

  private static Stream<Arguments> oclcNormalizationArguments() {
    return Stream.of(
      Arguments.of("(OCoLC)tfe0000501056183", "(OCoLC)tfe501056183"),
      Arguments.of("(OCoLC)0000501056183", "(OCoLC)501056183"),
      Arguments.of("(OCoLC)501056183", "(OCoLC)501056183"),
      Arguments.of("(OCoLC)ocm0000123456", "(OCoLC)123456"),
      Arguments.of("(OCoLC)ocm000064758", "(OCoLC)64758")
    );
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
