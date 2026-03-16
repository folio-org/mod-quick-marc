package org.folio.qm.convertion.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.LinkDetails;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class QuickMarcViewToBaseSourceMarcRecordConverterTest {

  private final QuickMarcViewToBaseSourceMarcRecordConverter converter =
    new QuickMarcViewToBaseSourceMarcRecordConverter();

  @Test
  void shouldConvertQuickMarcViewToBaseSourceMarcRecord() {
    var leader = "test leader";
    var tag = "100";
    var indicators = List.of("1", "2");
    var linkDetails = new LinkDetails().status("ACTUAL");
    var content = "$a test {dollar} subfield $z $x$0test0 $9 test9";

    var fieldItem = getFieldItem(tag, linkDetails, indicators, content);

    var quickMarcView = new QuickMarcView().leader(leader).addFieldsItem(fieldItem);

    var result = converter.convert(quickMarcView);

    assertNotNull(result);
    assertEquals(leader, result.getLeader());
    assertEquals(1, result.getFields().size());

    var sourceField = result.getFields().getFirst();
    assertTrue(sourceField.containsKey(tag));

    var sourceFieldItem = sourceField.get(tag);
    assertEquals("1", sourceFieldItem.getInd1());
    assertEquals("2", sourceFieldItem.getInd2());
    assertEquals(linkDetails, sourceFieldItem.getLinkDetails());

    var subfields = sourceFieldItem.getSubfields();
    assertEquals(3, subfields.size());
    assertEquals(Map.of("a", "test $ subfield"), subfields.get(0));
    assertEquals(Map.of("0", "test0"), subfields.get(1));
    assertEquals(Map.of("9", "test9"), subfields.get(2));
  }

  @Test
  void shouldConvertDollarPlaceholdersToActualDollars() {
    var content = "$a test {dollar} value $b another {dollar}{dollar} test";
    var fieldItem = new FieldItem().tag("245").content(content);
    var quickMarcView = new QuickMarcView().leader("leader").addFieldsItem(fieldItem);

    var result = converter.convert(quickMarcView);

    var subfields = result.getFields().getFirst().get("245").getSubfields();
    assertEquals(Map.of("a", "test $ value"), subfields.get(0));
    assertEquals(Map.of("b", "another $$ test"), subfields.get(1));
  }

  @Test
  void shouldHandleFieldWithoutIndicators() {
    var fieldItem = new FieldItem().tag("100").content("$a test");
    var quickMarcView = new QuickMarcView().leader("leader").addFieldsItem(fieldItem);

    var result = converter.convert(quickMarcView);

    var sourceFieldItem = result.getFields().getFirst().get("100");
    assertNull(sourceFieldItem.getInd1());
    assertNull(sourceFieldItem.getInd2());
  }

  @Test
  void shouldHandleFieldWithIndicators() {
    var fieldItem = new FieldItem()
      .tag("100")
      .indicators(List.of("1", "0"))
      .content("$a test");
    var quickMarcView = new QuickMarcView().leader("leader").addFieldsItem(fieldItem);

    var result = converter.convert(quickMarcView);

    var sourceFieldItem = result.getFields().getFirst().get("100");
    assertEquals("1", sourceFieldItem.getInd1());
    assertEquals("0", sourceFieldItem.getInd2());
  }

  @Test
  void shouldHandleFieldWithoutContent() {
    var fieldItem = new FieldItem().tag("100");
    var quickMarcView = new QuickMarcView().leader("leader").addFieldsItem(fieldItem);

    var result = converter.convert(quickMarcView);

    var sourceFieldItem = result.getFields().getFirst().get("100");
    var subfields = sourceFieldItem.getSubfields();
    assertTrue(subfields == null || subfields.isEmpty());
  }

  @Test
  void shouldHandleEmptyContent() {
    var fieldItem = new FieldItem().tag("100").content("");
    var quickMarcView = new QuickMarcView().leader("leader").addFieldsItem(fieldItem);

    var result = converter.convert(quickMarcView);

    var sourceFieldItem = result.getFields().getFirst().get("100");
    var subfields = sourceFieldItem.getSubfields();
    assertNotNull(subfields);
    assertEquals(0, subfields.size());
  }

  @Test
  void shouldHandleMultipleFields() {
    var field1 = new FieldItem().tag("100").content("$a Author");
    var field2 = new FieldItem().tag("245").content("$a Title");
    var field3 = new FieldItem().tag("650").content("$a Subject");

    var quickMarcView = new QuickMarcView()
      .leader("leader")
      .addFieldsItem(field1)
      .addFieldsItem(field2)
      .addFieldsItem(field3);

    var result = converter.convert(quickMarcView);

    assertEquals(3, result.getFields().size());
    assertTrue(result.getFields().get(0).containsKey("100"));
    assertTrue(result.getFields().get(1).containsKey("245"));
    assertTrue(result.getFields().get(2).containsKey("650"));
  }

  @Test
  void shouldHandleMultipleSubfieldsWithSameCode() {
    var content = "$a First $a Second $a Third";
    var fieldItem = new FieldItem().tag("650").content(content);
    var quickMarcView = new QuickMarcView().leader("leader").addFieldsItem(fieldItem);

    var result = converter.convert(quickMarcView);

    var subfields = result.getFields().getFirst().get("650").getSubfields();
    assertEquals(3, subfields.size());
    assertEquals(Map.of("a", "First"), subfields.get(0));
    assertEquals(Map.of("a", "Second"), subfields.get(1));
    assertEquals(Map.of("a", "Third"), subfields.get(2));
  }

  @Test
  void shouldHandleContentWithNoSpacesAfterDelimiter() {
    var content = "$aNoSpace$bAlsoNoSpace$cStillNoSpace";
    var fieldItem = new FieldItem().tag("100").content(content);
    var quickMarcView = new QuickMarcView().leader("leader").addFieldsItem(fieldItem);

    var result = converter.convert(quickMarcView);

    var subfields = result.getFields().getFirst().get("100").getSubfields();
    assertEquals(3, subfields.size());
    assertEquals(Map.of("a", "NoSpace"), subfields.get(0));
    assertEquals(Map.of("b", "AlsoNoSpace"), subfields.get(1));
    assertEquals(Map.of("c", "StillNoSpace"), subfields.get(2));
  }

  @Test
  void shouldPreserveLinkDetails() {
    var linkDetails = new LinkDetails().status("ACTUAL");

    var fieldItem = new FieldItem()
      .tag("100")
      .content("$a Test")
      .linkDetails(linkDetails);

    var quickMarcView = new QuickMarcView().leader("leader").addFieldsItem(fieldItem);

    var result = converter.convert(quickMarcView);

    var sourceFieldItem = result.getFields().getFirst().get("100");
    assertEquals(linkDetails, sourceFieldItem.getLinkDetails());
    assertEquals("ACTUAL", sourceFieldItem.getLinkDetails().getStatus());
  }

  @Test
  void shouldHandleFieldWithoutLinkDetails() {
    var fieldItem = new FieldItem().tag("100").content("$a Test");
    var quickMarcView = new QuickMarcView().leader("leader").addFieldsItem(fieldItem);

    var result = converter.convert(quickMarcView);

    var sourceFieldItem = result.getFields().getFirst().get("100");
    assertNull(sourceFieldItem.getLinkDetails());
  }

  @Test
  void shouldSkipEmptySubfields() {
    var content = "$a Valid $b  $c Another Valid";
    var fieldItem = new FieldItem().tag("100").content(content);
    var quickMarcView = new QuickMarcView().leader("leader").addFieldsItem(fieldItem);

    var result = converter.convert(quickMarcView);

    var subfields = result.getFields().getFirst().get("100").getSubfields();
    assertEquals(2, subfields.size());
    assertEquals(Map.of("a", "Valid"), subfields.get(0));
    assertEquals(Map.of("c", "Another Valid"), subfields.get(1));
  }

  @Test
  void shouldHandleComplexContent() {
    var content = "$a Main entry $b Subdivision $c Additional $x Topic $y Period $z Geographic";
    var fieldItem = new FieldItem()
      .tag("650")
      .indicators(List.of(" ", "0"))
      .content(content);
    var quickMarcView = new QuickMarcView().leader("leader").addFieldsItem(fieldItem);

    var result = converter.convert(quickMarcView);

    var sourceFieldItem = result.getFields().getFirst().get("650");
    assertEquals(" ", sourceFieldItem.getInd1());
    assertEquals("0", sourceFieldItem.getInd2());

    var subfields = sourceFieldItem.getSubfields();
    assertEquals(6, subfields.size());
    assertEquals(Map.of("a", "Main entry"), subfields.get(0));
    assertEquals(Map.of("b", "Subdivision"), subfields.get(1));
    assertEquals(Map.of("c", "Additional"), subfields.get(2));
    assertEquals(Map.of("x", "Topic"), subfields.get(3));
    assertEquals(Map.of("y", "Period"), subfields.get(4));
    assertEquals(Map.of("z", "Geographic"), subfields.get(5));
  }

  private FieldItem getFieldItem(String tag, LinkDetails linkDetails, List<String> indicators, String content) {
    return new FieldItem()
      .tag(tag)
      .linkDetails(linkDetails)
      .indicators(indicators)
      .content(content);
  }
}
