package org.folio.qm.converter.field;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.substring;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.marc4j.marc.Leader;
import org.marc4j.marc.VariableField;

import org.folio.qm.converter.elements.ControlFieldItem;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;

public interface VariableFieldConverter<T extends VariableField> {

  FieldItem convert(T field, Leader leader);

  boolean canProcess(VariableField field, MarcFormat marcFormat);

  default Map<String, Object> fillContentMap(List<ControlFieldItem> items, String content, int delta) {
    return items.stream()
      .collect(toMap(ControlFieldItem::getName, element -> getControlFieldElementContent(content, element, delta),
        (o, o2) -> o, LinkedHashMap::new)
      );
  }

  private Object getControlFieldElementContent(String content, ControlFieldItem element, int delta) {
    var elementFromContent = extractElementFromContent(content, element, delta);
    return element.isArray()
           ? Arrays.asList(elementFromContent.split(EMPTY))
           : elementFromContent;
  }

  default String extractElementFromContent(String content, ControlFieldItem element, int delta) {
    return substring(content, element.getPosition() + delta, element.getPosition() + delta + element.getLength());
  }

}
