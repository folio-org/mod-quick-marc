package org.folio.qm.convertion.converter;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.folio.qm.util.MarcUtils.extractSubfields;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.model.BaseSourceMarcRecord;
import org.folio.qm.domain.model.SourceFieldItem;
import org.marc4j.marc.impl.SubfieldImpl;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class QuickMarcViewToBaseSourceMarcRecordConverter implements Converter<QuickMarcView, BaseSourceMarcRecord> {

  @Override
  public BaseSourceMarcRecord convert(QuickMarcView source) {
    return new BaseSourceMarcRecord()
      .setLeader(source.getLeader())
      .setFields(source.getFields().stream().map(this::toSourceField).toList());
  }

  private Map<String, SourceFieldItem> toSourceField(FieldItem fieldItem) {
    var srsContent = new SourceFieldItem();
    var indicators = fieldItem.getIndicators();
    if (isNotEmpty(indicators)) {
      srsContent.setInd1(indicators.get(0));
      srsContent.setInd2(indicators.get(1));
    }
    if (fieldItem.getContent() instanceof String) {
      srsContent.setSubfields(mapStringToSubfields(fieldItem));
    }
    srsContent.setLinkDetails(fieldItem.getLinkDetails());
    return Map.of(fieldItem.getTag(), srsContent);
  }

  private List<Map<String, String>> mapStringToSubfields(FieldItem fieldItem) {
    var listOfSubfields = new ArrayList<Map<String, String>>();
    var subfields = extractSubfields(
      fieldItem, s -> new SubfieldImpl(s.charAt(1), s.substring(2).trim()));
    for (var subfield : subfields) {
      var code = String.valueOf(subfield.getCode());
      var data = subfield.getData();
      if (StringUtils.isNoneBlank(code, data)) {
        listOfSubfields.add(Map.of(code, data));
      }
    }
    return listOfSubfields;
  }
}
