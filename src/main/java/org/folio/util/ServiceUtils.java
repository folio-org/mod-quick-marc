package org.folio.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.jaxrs.model.Field;

public class ServiceUtils {

  private static final Logger logger = LogManager.getLogger(ServiceUtils.class);

  private static final DateTimeFormatter DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss.S");

  private ServiceUtils() {
  }

  public static Optional<Field> getFieldByTag(List<Field> fields, String tag) {
    for (Field field : fields) {
      if (tag.equals(field.getTag())) {
        return Optional.of(field);
      }
    }
    return Optional.empty();
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

  public static String buildQuery(String parameter, String query) {
    return String.format("?%s=%s", parameter, encodeQuery(query));
  }

  private static String encodeQuery(String query) {
    try {
      return URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      logger.error("Error happened while attempting to encode query: '{}'", query);
      throw new CompletionException(e);
    }
  }
}
