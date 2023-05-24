package org.folio.qm.converter.field.qm;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import org.folio.qm.converter.field.FieldItemConverter;

public abstract class Tag008FieldItemConverter implements FieldItemConverter {

  private final SimpleDateFormat dateEnteredFormat = new SimpleDateFormat("yyMMdd");

  protected void setDateEntered(String key, Map<String, Object> contentMap) {
    var contentValue = contentMap.get(key);
    if (contentValue == null || contentValue instanceof String enteredDate && isBlank(enteredDate)) {
      contentMap.put(key, dateEnteredFormat.format(Calendar.getInstance().getTime()));
    }
  }
}
