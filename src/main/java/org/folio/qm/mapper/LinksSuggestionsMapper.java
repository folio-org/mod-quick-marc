package org.folio.qm.mapper;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.folio.qm.domain.dto.BaseSrsMarcRecord;
import org.folio.qm.domain.dto.EntitiesLinksSuggestions;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.dto.SrsFieldItem;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface LinksSuggestionsMapper {

  default List<QuickMarcView> map(EntitiesLinksSuggestions srsRecords) {
    return srsRecords.getRecords().stream().map(this::mapRecord).toList();
  }

  default EntitiesLinksSuggestions map(List<QuickMarcView> quickMarcRecords) {
    return new EntitiesLinksSuggestions()
      .records(quickMarcRecords.stream().map(this::mapRecord).toList());
  }

  default QuickMarcView mapRecord(BaseSrsMarcRecord quickMarcRecord) {
    return new QuickMarcView()
      .leader(quickMarcRecord.getLeader())
      .fields(quickMarcRecord.getFields().parallelStream().map(this::mapField).toList());
  }

  default BaseSrsMarcRecord mapRecord(QuickMarcView quickMarcRecord) {
    return new BaseSrsMarcRecord()
      .leader(quickMarcRecord.getLeader())
      .fields(quickMarcRecord.getFields().parallelStream().map(this::mapField).toList());
  }

  default FieldItem mapField(Map<String, SrsFieldItem> fields) {
    var field = fields.entrySet().iterator().next();
    var content = field.getValue();

    var fieldItem = new FieldItem().tag(field.getKey());
    if (nonNull(content.getInd1())) {
      fieldItem.addIndicatorsItem(content.getInd1());
    }
    if (nonNull(content.getInd2())) {
      fieldItem.addIndicatorsItem(content.getInd2());
    }
    if (isEmpty(content.getSubfields())) {
      fieldItem.content(mapSubfields(content.getSubfields()));
    }
    return fieldItem.linkDetails(content.getLinkDetails());
  }

  default Map<String, SrsFieldItem> mapField(FieldItem fieldItem) {
    var srsContent = new SrsFieldItem();
    var indicators = fieldItem.getIndicators();
    if (isNotEmpty(indicators)) {
      srsContent.setInd1(indicators.get(0));
      srsContent.setInd2(indicators.get(1));
    }
    if (fieldItem.getContent() instanceof String subfields) {
      srsContent.setSubfields(mapSubfields(subfields));
    }
    srsContent.setLinkDetails(fieldItem.getLinkDetails());
    return Map.of(fieldItem.getTag(), srsContent);
  }

  default String mapSubfields(List<Map<String, String>> subfields) {
    StringBuilder content = new StringBuilder();
    for (Map<String, String> subfieldMap : subfields) {
      var subfield = subfieldMap.entrySet().iterator().next();
      if (!content.isEmpty()) {
        content.append(" ");
      }
      content
        .append("$").append(subfield.getKey())
        .append(" ").append(subfield.getValue());
    }
    return content.toString();
  }

  default List<Map<String, String>> mapSubfields(String stringSubfields) {
    var listOfSubfields = new ArrayList<Map<String, String>>();

    if (stringSubfields.contains("$")) {
      var subfieldsPairs = stringSubfields.split("[$]");
      for (String subfieldPair : subfieldsPairs) {
        var subfield = subfieldPair.split("\\s", 2);
        if (subfield.length == 2) {
          var tag = subfield[0];
          var content = subfield[1].trim();
          listOfSubfields.add(Map.of(tag, content));
        }
      }
    }
    return listOfSubfields;
  }
}

