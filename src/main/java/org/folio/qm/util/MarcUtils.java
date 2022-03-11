package org.folio.qm.util;

import static org.apache.commons.lang3.StringUtils.SPACE;

import static org.folio.qm.converter.elements.Constants.BLANK_REPLACEMENT;
import static org.folio.qm.converter.elements.Constants.DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.QuickMarc;

public final class MarcUtils {

  private static final DateTimeFormatter DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD_FORMATTER =
    DateTimeFormatter.ofPattern("yyyyMMddHHmmss.S");

  private MarcUtils() {
  }

  public static Optional<FieldItem> getFieldByTag(QuickMarc quickMarc, String tag) {
    return quickMarc.getFields().stream()
      .filter(field -> tag.equals(field.getTag()))
      .findFirst();
  }

  /**
   * This method encode Java {@link LocalDateTime} value in the MARC date-time format for
   * Date and Time of Latest Transaction Field (005)
   * https://www.loc.gov/marc/bibliographic/bd005.html
   *
   * @param localDateTime {@link LocalDateTime} value
   * @return string with MARC date-time representation
   */
  public static String encodeToMarcDateTime(LocalDateTime localDateTime) {
    return localDateTime.format(DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD_FORMATTER);
  }

  /**
   * This method decode MARC date-time representation for
   * Date and Time of Latest Transaction Field (005)
   * in the Java {@link LocalDateTime} value
   *
   * @param representation MARC date-time representation
   * @return Java {@link LocalDateTime} for MARC date-time value
   */
  public static LocalDateTime decodeFromMarcDateTime(String representation) {
    return LocalDateTime.parse(representation, DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD_FORMATTER);
  }

  public static QuickMarc updateRecordTimestamp(QuickMarc quickMarc) {
    final var currentTime = encodeToMarcDateTime(LocalDateTime.now());
    getFieldByTag(quickMarc, DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD)
      .ifPresentOrElse(field -> field.setContent(currentTime),
        () -> quickMarc.addFieldsItem(new FieldItem().tag(DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD).content(currentTime))
      );
    return quickMarc;
  }

  public static String restoreBlanks(String sourceString) {
    return sourceString.replace(BLANK_REPLACEMENT, SPACE);
  }

}
