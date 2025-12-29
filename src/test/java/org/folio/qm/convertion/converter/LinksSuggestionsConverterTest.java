package org.folio.qm.convertion.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.LinkDetails;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.model.BaseSourceMarcRecord;
import org.folio.qm.domain.model.SourceFieldItem;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class LinksSuggestionsConverterTest {

  private static final String LEADER = "test leader";
  private static final BaseSourceMarcRecordToQuickMarcViewConverter TO_QUICK_MARC_CONVERTER =
      new BaseSourceMarcRecordToQuickMarcViewConverter();
  private static final QuickMarcViewToBaseSourceMarcRecordConverter TO_SRS_CONVERTER =
      new QuickMarcViewToBaseSourceMarcRecordConverter();

  @Test
  void shouldConvertSrsToQuickMarcSuccessfully() {
    var expectedTag = "100";
    var expectedIndicators = List.of("1", "2");
    var linkDetails = new LinkDetails().status("ACTUAL");
    var expectedContent = "$a test {dollar} subfield $0 test0 $9 test9";
    var srsSubfields = List.of(
      Map.of("a", "test $ subfield"),
      Map.of("0", "test0"),
      Map.of("9", "test9"));
    var srsField = Map.of(expectedTag, new SourceFieldItem()
      .setInd1(expectedIndicators.get(0))
      .setInd2(expectedIndicators.get(1))
      .setLinkDetails(linkDetails)
      .setSubfields(srsSubfields));

    var srsRecord = new BaseSourceMarcRecord().setLeader(LEADER).setFields(List.of(srsField));

    var quickMarcRecord = TO_QUICK_MARC_CONVERTER.convert(srsRecord);
    var quickMarcField = quickMarcRecord.getFields().getFirst();
    assertThat(quickMarcRecord).hasFieldOrPropertyWithValue("leader", LEADER);
    assertThat(quickMarcField)
      .hasFieldOrPropertyWithValue("tag", expectedTag)
      .hasFieldOrPropertyWithValue("content", expectedContent)
      .hasFieldOrPropertyWithValue("linkDetails", linkDetails)
      .hasFieldOrPropertyWithValue("indicators", expectedIndicators);
  }

  @Test
  void shouldConvertQuickMarcToSrsSuccessfully() {
    var quickMarcTag = "100";
    var quickMarcIndicators = List.of("1", "2");
    var linkDetails = new LinkDetails().status("ACTUAL");
    var quickMarcContent = "$a test {dollar} subfield $z $x$0test0 $9 test9";
    var quickMarcField = new FieldItem()
      .tag(quickMarcTag)
      .linkDetails(linkDetails)
      .indicators(quickMarcIndicators)
      .content(quickMarcContent);

    var quickMarcRecord = new QuickMarcView().leader(LEADER).addFieldsItem(quickMarcField);
    var expectedSubfields = List.of(
      Map.of("a", "test $ subfield"),
      Map.of("0", "test0"),
      Map.of("9", "test9"));
    var expectedField = Map.of(quickMarcTag, new SourceFieldItem()
      .setInd1(quickMarcIndicators.get(0))
      .setInd2(quickMarcIndicators.get(1))
      .setLinkDetails(linkDetails)
      .setSubfields(expectedSubfields));

    var srsRecord = TO_SRS_CONVERTER.convert(quickMarcRecord);
    assertThat(srsRecord)
      .hasFieldOrPropertyWithValue("leader", LEADER)
      .hasFieldOrPropertyWithValue("fields", List.of(expectedField));
  }

  @Test
  void shouldConvertSrsToQuickMarcSuccessfullyWhenContentIsEmpty() {
    var expectedTag = "100";
    var srsField = Map.of(expectedTag, new SourceFieldItem().setSubfields(null));
    var srsRecord = new BaseSourceMarcRecord().setLeader(LEADER).setFields(List.of(srsField));

    var quickMarcRecord = TO_QUICK_MARC_CONVERTER.convert(srsRecord);
    var quickMarcField = quickMarcRecord.getFields().getFirst();
    assertThat(quickMarcRecord).hasFieldOrPropertyWithValue("leader", LEADER);
    assertThat(quickMarcField)
      .hasFieldOrPropertyWithValue("tag", expectedTag);
  }

  @Test
  void shouldConvertQuickMarcToSrsSuccessfullyWhenContentIsEmpty() {
    var quickMarcTag = "100";
    var quickMarcField = new FieldItem().tag(quickMarcTag);

    var quickMarcRecord = new QuickMarcView().leader(LEADER).addFieldsItem(quickMarcField);
    var expectedField = Map.of(quickMarcTag, new SourceFieldItem());

    var srsRecord = TO_SRS_CONVERTER.convert(quickMarcRecord);
    assertThat(srsRecord)
      .hasFieldOrPropertyWithValue("leader", LEADER)
      .hasFieldOrPropertyWithValue("fields", List.of(expectedField));
  }
}
