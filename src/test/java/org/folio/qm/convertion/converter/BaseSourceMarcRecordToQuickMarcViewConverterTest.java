package org.folio.qm.convertion.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;
import org.folio.qm.domain.dto.LinkDetails;
import org.folio.qm.domain.model.BaseSourceMarcRecord;
import org.folio.qm.domain.model.SourceFieldItem;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class BaseSourceMarcRecordToQuickMarcViewConverterTest {

  private final BaseSourceMarcRecordToQuickMarcViewConverter converter
    = new BaseSourceMarcRecordToQuickMarcViewConverter();

  @Test
  void shouldConvertBaseSourceMarcRecordToQuickMarcView() {
    var leader = "test leader";
    var tag = "100";
    var expectedIndicators = List.of("1", "2");
    var linkDetails = new LinkDetails().status("ACTUAL");
    var subfields = List.of(
      Map.of("a", "test $ subfield"),
      Map.of("0", "test0"),
      Map.of("9", "test9"));

    var sourceField = Map.of(tag, new SourceFieldItem()
      .setInd1(expectedIndicators.get(0))
      .setInd2(expectedIndicators.get(1))
      .setLinkDetails(linkDetails)
      .setSubfields(subfields));

    var srsRecord = new BaseSourceMarcRecord().setLeader(leader).setFields(List.of(sourceField));

    var result = converter.convert(srsRecord);

    assertNotNull(result);
    assertEquals(leader, result.getLeader());
    assertEquals(1, result.getFields().size());

    var fieldItem = result.getFields().getFirst();
    assertEquals(tag, fieldItem.getTag());
    assertEquals(expectedIndicators, fieldItem.getIndicators());
    assertEquals("$a test {dollar} subfield $0 test0 $9 test9", fieldItem.getContent());
    assertEquals(linkDetails, fieldItem.getLinkDetails());
  }

  @Test
  void shouldConvertDollarSignsInSubfieldContent() {
    var subfields = List.of(
      Map.of("a", "test $ value"),
      Map.of("b", "another $$ test"));

    var sourceField = Map.of("245", new SourceFieldItem().setSubfields(subfields));
    var srsRecord = new BaseSourceMarcRecord()
      .setLeader("leader")
      .setFields(List.of(sourceField));

    var result = converter.convert(srsRecord);

    var fieldItem = result.getFields().getFirst();
    assertEquals("$a test {dollar} value $b another {dollar}{dollar} test", fieldItem.getContent());
  }

  @Test
  void shouldHandleFieldWithoutIndicators() {
    var subfields = List.of(Map.of("a", "test value"));
    var sourceField = Map.of("100", new SourceFieldItem().setSubfields(subfields));
    var srsRecord = new BaseSourceMarcRecord()
      .setLeader("leader")
      .setFields(List.of(sourceField));

    var result = converter.convert(srsRecord);

    var fieldItem = result.getFields().getFirst();
    assertNotNull(fieldItem.getIndicators());
    assertEquals(0, fieldItem.getIndicators().size());
  }

  @Test
  void shouldHandleFieldWithOnlyFirstIndicator() {
    var subfields = List.of(Map.of("a", "test value"));
    var sourceField = Map.of("100", new SourceFieldItem()
      .setInd1("1")
      .setSubfields(subfields));
    var srsRecord = new BaseSourceMarcRecord()
      .setLeader("leader")
      .setFields(List.of(sourceField));

    var result = converter.convert(srsRecord);

    var fieldItem = result.getFields().getFirst();
    assertEquals(List.of("1"), fieldItem.getIndicators());
  }

  @Test
  void shouldHandleFieldWithBothIndicators() {
    var subfields = List.of(Map.of("a", "test value"));
    var sourceField = Map.of("100", new SourceFieldItem()
      .setInd1("1")
      .setInd2("0")
      .setSubfields(subfields));
    var srsRecord = new BaseSourceMarcRecord()
      .setLeader("leader")
      .setFields(List.of(sourceField));

    var result = converter.convert(srsRecord);

    var fieldItem = result.getFields().getFirst();
    assertEquals(List.of("1", "0"), fieldItem.getIndicators());
  }

  @Test
  void shouldHandleFieldWithoutSubfields() {
    var sourceField = Map.of("100", new SourceFieldItem().setSubfields(null));
    var srsRecord = new BaseSourceMarcRecord()
      .setLeader("leader")
      .setFields(List.of(sourceField));

    var result = converter.convert(srsRecord);

    var fieldItem = result.getFields().getFirst();
    assertEquals("100", fieldItem.getTag());
    assertNull(fieldItem.getContent());
  }

  @Test
  void shouldHandleFieldWithEmptySubfields() {
    var sourceField = Map.of("100", new SourceFieldItem().setSubfields(List.of()));
    var srsRecord = new BaseSourceMarcRecord()
      .setLeader("leader")
      .setFields(List.of(sourceField));

    var result = converter.convert(srsRecord);

    var fieldItem = result.getFields().getFirst();
    assertEquals("100", fieldItem.getTag());
    assertNull(fieldItem.getContent());
  }

  @Test
  void shouldHandleMultipleFields() {
    var field1 = Map.of("100", new SourceFieldItem()
      .setSubfields(List.of(Map.of("a", "Author"))));
    var field2 = Map.of("245", new SourceFieldItem()
      .setSubfields(List.of(Map.of("a", "Title"))));
    var field3 = Map.of("650", new SourceFieldItem()
      .setSubfields(List.of(Map.of("a", "Subject"))));

    var srsRecord = new BaseSourceMarcRecord()
      .setLeader("leader")
      .setFields(List.of(field1, field2, field3));

    var result = converter.convert(srsRecord);

    assertEquals(3, result.getFields().size());
    assertEquals("100", result.getFields().get(0).getTag());
    assertEquals("245", result.getFields().get(1).getTag());
    assertEquals("650", result.getFields().get(2).getTag());
  }

  @Test
  void shouldHandleMultipleSubfieldsInCorrectOrder() {
    var subfields = List.of(
      Map.of("a", "First"),
      Map.of("b", "Second"),
      Map.of("c", "Third"));
    var sourceField = Map.of("100", new SourceFieldItem().setSubfields(subfields));
    var srsRecord = new BaseSourceMarcRecord()
      .setLeader("leader")
      .setFields(List.of(sourceField));

    var result = converter.convert(srsRecord);

    var fieldItem = result.getFields().getFirst();
    assertEquals("$a First $b Second $c Third", fieldItem.getContent());
  }

  @Test
  void shouldPreserveLinkDetails() {
    var linkDetails = new LinkDetails().status("ACTUAL");

    var sourceField = Map.of("100", new SourceFieldItem()
      .setLinkDetails(linkDetails)
      .setSubfields(List.of(Map.of("a", "Test"))));

    var srsRecord = new BaseSourceMarcRecord()
      .setLeader("leader")
      .setFields(List.of(sourceField));

    var result = converter.convert(srsRecord);

    var fieldItem = result.getFields().getFirst();
    assertEquals(linkDetails, fieldItem.getLinkDetails());
    assertNotNull(fieldItem.getLinkDetails());
    assertEquals("ACTUAL", fieldItem.getLinkDetails().getStatus());
  }

  @Test
  void shouldHandleFieldWithoutLinkDetails() {
    var sourceField = Map.of("100", new SourceFieldItem()
      .setSubfields(List.of(Map.of("a", "Test"))));
    var srsRecord = new BaseSourceMarcRecord()
      .setLeader("leader")
      .setFields(List.of(sourceField));

    var result = converter.convert(srsRecord);

    var fieldItem = result.getFields().getFirst();
    assertNull(fieldItem.getLinkDetails());
  }
}
