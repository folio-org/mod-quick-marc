package org.folio.qm.converter.field;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;

import static org.folio.qm.converter.elements.Constants.BLANK_REPLACEMENT;
import static org.folio.qm.converter.elements.Constants.COMPLEX_CONTROL_FIELD_TAGS;
import static org.folio.qm.converter.elements.Constants.CONTROL_FIELD_PATTERN;
import static org.folio.qm.util.ErrorCodes.ILLEGAL_FIXED_LENGTH_CONTROL_FIELD;
import static org.folio.qm.util.ErrorUtils.buildInternalError;

import java.util.List;
import java.util.Map;

import org.marc4j.marc.VariableField;

import org.folio.qm.converter.elements.ControlFieldItem;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.exception.ConverterException;

public interface FieldItemConverter {

  VariableField convert(FieldItem field);

  boolean canProcess(FieldItem field, MarcFormat marcFormat);

  default char getIndicator(FieldItem field, int index) {
    var indicators = field.getIndicators();
    if (indicators == null || indicators.size() < index + 1) {
      return ' ';
    }
    var ind = indicators.get(index);
    if (ind.equals(BLANK_REPLACEMENT)) {
      return ' ';
    }
    return ind.isEmpty() ? ' ' : ind.charAt(0);
  }

  default boolean isSimpleControlField(FieldItem field) {
    return isControlField(field) && !COMPLEX_CONTROL_FIELD_TAGS.contains(field.getTag());
  }

  default boolean isControlField(FieldItem field) {
    return CONTROL_FIELD_PATTERN.matcher(field.getTag()).matches();
  }

  default String restoreFixedLengthField(Map<String, Object> map, int length, int delta, List<ControlFieldItem> items) {
    var sb = new StringBuilder(SPACE.repeat(length));
    items.forEach(item -> {
      if (map.get(item.getName()) != null) {
        String result = getItemValue(map, item);
        if (result.length() != item.getLength()) {
          throw new ConverterException(buildInternalError(ILLEGAL_FIXED_LENGTH_CONTROL_FIELD,
            String.format("Invalid %s field length, must be %d characters", item.getName(), item.getLength())));
        }
        sb.replace(item.getPosition() + delta, item.getPosition() + delta + item.getLength(), result);
      }
    });
    return sb.toString();
  }

  @SuppressWarnings("unchecked")
  private String getItemValue(Map<String, Object> map, ControlFieldItem item) {
    var itemValue = map.get(item.getName());
    if (itemValue instanceof List && item.isArray()) {
      return String.join(EMPTY, ((List<String>) itemValue));
    } else {
      return itemValue.toString();
    }
  }
}
