package org.folio.qm.mapper;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.folio.qm.util.MarcUtils.extractSubfields;

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
import org.marc4j.marc.impl.SubfieldImpl;

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
      .fields(quickMarcRecord.getFields().stream().map(this::mapField).toList());
  }

  default BaseSrsMarcRecord mapRecord(QuickMarcView quickMarcRecord) {
    return new BaseSrsMarcRecord()
      .leader(quickMarcRecord.getLeader())
      .fields(quickMarcRecord.getFields().stream().map(this::mapField).toList());
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
    if (isNotEmpty(content.getSubfields())) {
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
    if (fieldItem.getContent() instanceof String) {
      srsContent.setSubfields(mapSubfields(fieldItem));
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

  default List<Map<String, String>> mapSubfields(FieldItem fieldItem) {
    var listOfSubfields = new ArrayList<Map<String, String>>();
    var subfields = extractSubfields(fieldItem, s -> new SubfieldImpl(s.charAt(1), s.substring(2).trim()));
    for (var subfield : subfields) {
      listOfSubfields.add(Map.of(String.valueOf(subfield.getCode()), subfield.getData()));
    }
    return listOfSubfields;
  }
}

