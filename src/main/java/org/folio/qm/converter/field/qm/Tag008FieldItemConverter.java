package org.folio.qm.converter.field.qm;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import org.folio.qm.converter.field.FieldItemConverter;

public abstract class Tag008FieldItemConverter implements FieldItemConverter {

  private static final String NOT_VALID_DATE_ENTERED = "000000";
  private static final String DATE_ENTERED_PATTERN = "yyMMdd";

  private final SimpleDateFormat dateEnteredFormat = new SimpleDateFormat(DATE_ENTERED_PATTERN);

  protected void setDateEntered(String key, Map<String, Object> contentMap) {
    var contentValue = contentMap.get(key);
    if (contentValue == null
      || contentValue instanceof String enteredDate && isNotValidDate(enteredDate)) {
      contentMap.put(key, dateEnteredFormat.format(Calendar.getInstance().getTime()));
    }
  }

  private boolean isNotValidDate(String enteredDate) {
    return isBlank(enteredDate)
      || NOT_VALID_DATE_ENTERED.equals(enteredDate)
      || !isNumeric(enteredDate)
      || DATE_ENTERED_PATTERN.length() != enteredDate.length();
  }
}
