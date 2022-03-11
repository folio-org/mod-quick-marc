package org.folio.qm.converternew;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;

import static org.folio.qm.converter.elements.Constants.BLANK_REPLACEMENT;
import static org.folio.qm.converter.elements.Constants.COMPLEX_CONTROL_FIELD_TAGS;
import static org.folio.qm.converter.elements.Constants.CONTROL_FIELD_PATTERN;
import static org.folio.qm.converter.elements.Constants.SPACE_CHARACTER;
import static org.folio.qm.util.ErrorCodes.ILLEGAL_FIXED_LENGTH_CONTROL_FIELD;
import static org.folio.qm.util.ErrorUtils.buildInternalError;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.marc4j.marc.VariableField;

import org.folio.qm.converter.elements.ControlFieldItem;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.exception.ConverterException;

public interface FieldItemConverter {

  VariableField convert(FieldItem field);

  boolean canProcess(FieldItem field, MarcFormat marcFormat);

  default char getIndicator(FieldItem field, int index) {
    if (field.getIndicators().size() < index + 1) {
      return ' ';
    }
    var ind = field.getIndicators().get(index);
    if (ind.equals(BLANK_REPLACEMENT)) {
      return ' ';
    }
    return ind.isEmpty() ? ' ' : ind.charAt(0);
  }

  default boolean isControlField(FieldItem field) {
    return CONTROL_FIELD_PATTERN.matcher(field.getTag()).matches();
  }

  default boolean isSimpleControlField(FieldItem field) {
    return isControlField(field) && !COMPLEX_CONTROL_FIELD_TAGS.contains(field.getTag());
  }

  default String restoreBlanks(String sourceString) {
    return sourceString.replace(BLANK_REPLACEMENT, SPACE);
  }

  default String restoreFixedLengthField(int length, List<ControlFieldItem> items, Map<String, Object> map, int delta) {
    StringBuilder stringBuilder = new StringBuilder(StringUtils.repeat(SPACE_CHARACTER, length));
    items.forEach(item -> {
      String value;
      if (Objects.isNull(map.get(item.getName()))) {
        value = StringUtils.repeat(SPACE_CHARACTER, item.getLength());
      } else {
        value = item.isArray()
          ? String.join(EMPTY, ((List<String>) map.get(item.getName())))
          : map.get(item.getName()).toString();
        if (value.length() != item.getLength()) {
          throw new ConverterException(buildInternalError(ILLEGAL_FIXED_LENGTH_CONTROL_FIELD,
            String.format("Invalid %s field length, must be %d characters", item.getName(), item.getLength())));
        }
      }
      stringBuilder.replace(item.getPosition() + delta, item.getPosition() + delta + item.getLength(), value);
    });
    return stringBuilder.toString();
  }
}
