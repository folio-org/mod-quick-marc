package org.folio.qm.convertion.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import java.util.List;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.spring.testing.type.UnitTest;
import org.folio.support.StubQuickMarcRecord;
import org.junit.jupiter.api.Test;

@UnitTest
class BaseQuickMarcRecordToValidatableRecordConverterTest {

  private final BaseQuickMarcRecordToValidatableRecordConverter converter =
    new BaseQuickMarcRecordToValidatableRecordConverter();

  @Test
  void shouldConvertBaseMarcRecordWithAllFields() {
    var leader = "00000nam\\\\2200000\\u\\4500";
    var fields = List.of(
      new FieldItem().tag("001").content("test001"),
      new FieldItem().tag("245").content("$a Test title").indicators(List.of("0", "0"))
    );
    var baseMarcRecord = new StubQuickMarcRecord(MarcFormat.BIBLIOGRAPHIC, leader, fields);

    var result = converter.convert(baseMarcRecord);

    assertNotNull(result);
    assertEquals(MarcFormat.BIBLIOGRAPHIC, result.getMarcFormat());
    assertEquals(leader, result.getLeader());
    assertThat(result.getFields()).hasSize(2);

    var firstField = result.getFields().getFirst();
    assertEquals("001", firstField.getTag());
    assertEquals("test001", firstField.getContent());

    var secondField = result.getFields().get(1);
    assertEquals("245", secondField.getTag());
    assertEquals("$a Test title", secondField.getContent());
    assertThat(secondField.getIndicators()).containsExactly("0", "0");
  }

  @Test
  void shouldConvertBaseMarcRecordWithEmptyFields() {
    var leader = "00000nam\\\\2200000\\u\\4500";
    var baseMarcRecord = new StubQuickMarcRecord(MarcFormat.BIBLIOGRAPHIC, leader, Collections.emptyList());

    var result = converter.convert(baseMarcRecord);

    assertNotNull(result);
    assertEquals(MarcFormat.BIBLIOGRAPHIC, result.getMarcFormat());
    assertEquals(leader, result.getLeader());
    assertThat(result.getFields()).isEmpty();
  }

  @Test
  void shouldConvertAuthorityMarcFormat() {
    var leader = "00000nz\\\\\\2200000\\u\\4500";
    var fields = List.of(
      new FieldItem().tag("100").content("$a Author name").indicators(List.of("1", " "))
    );
    var baseMarcRecord = new StubQuickMarcRecord(MarcFormat.AUTHORITY, leader, fields);

    var result = converter.convert(baseMarcRecord);

    assertNotNull(result);
    assertEquals(MarcFormat.AUTHORITY, result.getMarcFormat());
    assertEquals(leader, result.getLeader());
    assertThat(result.getFields()).hasSize(1);
    assertEquals("100", result.getFields().getFirst().getTag());
  }

  @Test
  void shouldConvertHoldingsMarcFormat() {
    var leader = "00000nx\\\\\\2200000\\u\\4500";
    var fields = List.of(
      new FieldItem().tag("852").content("$b Library").indicators(List.of(" ", " "))
    );
    var baseMarcRecord = new StubQuickMarcRecord(MarcFormat.HOLDINGS, leader, fields);

    var result = converter.convert(baseMarcRecord);

    assertNotNull(result);
    assertEquals(MarcFormat.HOLDINGS, result.getMarcFormat());
    assertEquals(leader, result.getLeader());
    assertThat(result.getFields()).hasSize(1);
    assertEquals("852", result.getFields().getFirst().getTag());
  }

  @Test
  void shouldConvertMultipleFields() {
    var fields = List.of(
      new FieldItem().tag("001").content("test001"),
      new FieldItem().tag("008").content("test008"),
      new FieldItem().tag("245").content("$a Title").indicators(List.of("0", "0")),
      new FieldItem().tag("650").content("$a Subject").indicators(List.of(" ", "0"))
    );
    var baseMarcRecord = new StubQuickMarcRecord(MarcFormat.BIBLIOGRAPHIC, "00000nam\\\\2200000\\u\\4500", fields);
    var result = converter.convert(baseMarcRecord);

    assertNotNull(result);
    assertThat(result.getFields()).hasSize(4);
    assertEquals("001", result.getFields().get(0).getTag());
    assertEquals("008", result.getFields().get(1).getTag());
    assertEquals("245", result.getFields().get(2).getTag());
    assertEquals("650", result.getFields().get(3).getTag());
  }

  @Test
  void shouldPreserveFieldContent() {
    var content = "$a Complex $b content $c with $d multiple subfields";
    var fields = List.of(
      new FieldItem().tag("245").content(content).indicators(List.of("1", "0"))
    );
    var baseMarcRecord = new StubQuickMarcRecord(MarcFormat.BIBLIOGRAPHIC, "00000nam\\\\2200000\\u\\4500", fields);

    var result = converter.convert(baseMarcRecord);

    assertNotNull(result);
    assertThat(result.getFields()).hasSize(1);
    assertEquals(content, result.getFields().getFirst().getContent());
  }

  @Test
  void shouldPreserveIndicators() {
    var fields = List.of(
      new FieldItem().tag("245").content("$a Title").indicators(List.of("1", "4"))
    );
    var baseMarcRecord = new StubQuickMarcRecord(MarcFormat.BIBLIOGRAPHIC, "00000nam\\\\2200000\\u\\4500", fields);

    var result = converter.convert(baseMarcRecord);

    assertNotNull(result);
    assertThat(result.getFields()).hasSize(1);
    assertThat(result.getFields().getFirst().getIndicators()).containsExactly("1", "4");
  }

  @Test
  void shouldHandleFieldsWithoutIndicators() {
    var fields = List.of(
      new FieldItem().tag("001").content("test001")
    );
    var baseMarcRecord = new StubQuickMarcRecord(MarcFormat.BIBLIOGRAPHIC, "00000nam\\\\2200000\\u\\4500", fields);

    var result = converter.convert(baseMarcRecord);

    assertNotNull(result);
    assertThat(result.getFields()).hasSize(1);
    assertEquals("001", result.getFields().getFirst().getTag());
    assertEquals("test001", result.getFields().getFirst().getContent());
  }

  @Test
  void shouldPreserveLeader() {
    var leader = "00000nam a2200000 a 4500";
    var baseMarcRecord = new StubQuickMarcRecord(MarcFormat.BIBLIOGRAPHIC, leader, Collections.emptyList());

    var result = converter.convert(baseMarcRecord);

    assertNotNull(result);
    assertEquals(leader, result.getLeader());
  }

  @Test
  void shouldHandleNullFieldContent() {
    var fields = List.of(
      new FieldItem().tag("001")
    );
    var baseMarcRecord = new StubQuickMarcRecord(MarcFormat.BIBLIOGRAPHIC, "00000nam\\\\2200000\\u\\4500", fields);

    var result = converter.convert(baseMarcRecord);

    assertNotNull(result);
    assertThat(result.getFields()).hasSize(1);
    assertEquals("001", result.getFields().getFirst().getTag());
  }
}
