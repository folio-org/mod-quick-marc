package org.folio.qm.convertion.converter;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.List;
import java.util.Map;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.model.BaseSourceMarcRecord;
import org.folio.qm.domain.model.SourceFieldItem;
import org.folio.qm.util.MarcUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class BaseSourceMarcRecordToQuickMarcViewConverter implements Converter<BaseSourceMarcRecord, QuickMarcView> {

  @Override
  public QuickMarcView convert(BaseSourceMarcRecord source) {
    return new QuickMarcView()
      .leader(source.getLeader())
      .fields(source.getFields().stream().map(this::toFieldItem).toList());
  }

  private FieldItem toFieldItem(Map<String, SourceFieldItem> fields) {
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
      fieldItem.content(mapSubfieldsToString(content.getSubfields()));
    }
    return fieldItem.linkDetails(content.getLinkDetails());
  }

  private String mapSubfieldsToString(List<Map<String, String>> subfields) {
    StringBuilder content = new StringBuilder();
    for (Map<String, String> subfieldMap : subfields) {
      var subfield = subfieldMap.entrySet().iterator().next();
      if (!content.isEmpty()) {
        content.append(" ");
      }
      content
        .append("$").append(subfield.getKey())
        .append(" ").append(MarcUtils.convertDollar(subfield.getValue()));
    }
    return content.toString();
  }
}
