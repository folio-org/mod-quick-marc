package org.folio.qm.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.glytching.junit.extension.random.RandomBeansExtension;
import java.util.List;
import java.util.Map;
import org.folio.qm.domain.dto.BaseSrsMarcRecord;
import org.folio.qm.domain.dto.EntitiesLinksSuggestions;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.LinkDetails;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.dto.SrsFieldItem;
import org.folio.qm.support.types.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;

@UnitTest
@ExtendWith(RandomBeansExtension.class)
class LinksSuggestionsMapperTest {

  private static final String LEADER = "test leader";
  private static final LinksSuggestionsMapper MAPPER = Mappers.getMapper(LinksSuggestionsMapper.class);

  @Test
  void shouldConvertSrsToQuickMarcSuccessfully() {
    var expectedTag = "100";
    var expectedIndicators = List.of("1", "2");
    var linkDetails = new LinkDetails().status("ACTUAL");
    var expectedContent = "$a test a subfield $0 test0 $9 test9";
    var srsSubfields = List.of(
      Map.of("a", "test a subfield"),
      Map.of("0", "test0"),
      Map.of("9", "test9")
    );
    var srsField = Map.of(expectedTag, new SrsFieldItem()
      .ind1(expectedIndicators.get(0))
      .ind2(expectedIndicators.get(1))
      .linkDetails(linkDetails)
      .subfields(srsSubfields)
    );

    var srsRecord = new BaseSrsMarcRecord().leader(LEADER)
      .addFieldsItem(srsField);
    var srsRecordCollection = new EntitiesLinksSuggestions()
      .addRecordsItem(srsRecord);

    var quickMarcRecord = MAPPER.map(srsRecordCollection).get(0);
    var quickMarcField = quickMarcRecord.getFields().get(0);
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
    var quickMarcContent = "$a test a subfield $0 test0  $0 test0 $9 test9";
    var quickMarcField = new FieldItem()
      .tag(quickMarcTag)
      .linkDetails(linkDetails)
      .indicators(quickMarcIndicators)
      .content(quickMarcContent);

    var quickMarcRecord = new QuickMarcView().leader(LEADER)
      .addFieldsItem(quickMarcField);

    var expectedSubfields = List.of(
      Map.of("a", "test a subfield"),
      Map.of("0", "test0"),
      Map.of("9", "test9")
    );
    var expectedField = Map.of(quickMarcTag, new SrsFieldItem()
      .ind1(quickMarcIndicators.get(0))
      .ind2(quickMarcIndicators.get(1))
      .linkDetails(linkDetails)
      .subfields(expectedSubfields)
    );

    var srsRecord = MAPPER.map(List.of(quickMarcRecord)).getRecords().get(0);
    assertThat(srsRecord)
      .hasFieldOrPropertyWithValue("leader", LEADER)
      .hasFieldOrPropertyWithValue("fields", List.of(expectedField));
  }

  @Test
  void shouldConvertSrsToQuickMarcSuccessfullyWhenContentIsEmpty() {
    var expectedTag = "100";
    var srsField = Map.of(expectedTag, new SrsFieldItem().subfields(null));
    var srsRecord = new BaseSrsMarcRecord().leader(LEADER).addFieldsItem(srsField);
    var srsRecordCollection = new EntitiesLinksSuggestions().addRecordsItem(srsRecord);

    var quickMarcRecord = MAPPER.map(srsRecordCollection).get(0);
    var quickMarcField = quickMarcRecord.getFields().get(0);
    assertThat(quickMarcRecord).hasFieldOrPropertyWithValue("leader", LEADER);
    assertThat(quickMarcField)
      .hasFieldOrPropertyWithValue("tag", expectedTag);
  }

  @Test
  void shouldConvertQuickMarcToSrsSuccessfullyWhenContentIsEmpty() {
    var quickMarcTag = "100";
    var quickMarcField = new FieldItem().tag(quickMarcTag);

    var quickMarcRecord = new QuickMarcView().leader(LEADER).addFieldsItem(quickMarcField);
    var expectedField = Map.of(quickMarcTag, new SrsFieldItem());

    var srsRecord = MAPPER.map(List.of(quickMarcRecord)).getRecords().get(0);
    assertThat(srsRecord)
      .hasFieldOrPropertyWithValue("leader", LEADER)
      .hasFieldOrPropertyWithValue("fields", List.of(expectedField));
  }
}
