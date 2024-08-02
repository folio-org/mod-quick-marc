package org.folio.qm.util;

import static org.folio.qm.converter.elements.Constants.SPLIT_PATTERN;
import static org.folio.qm.converter.elements.Constants.TOKEN_MIN_LENGTH;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.marc4j.marc.Subfield;

public final class MarcUtils {
  public static final BiMap<ParsedRecordDto.RecordTypeEnum, MarcFormat> TYPE_MAP = ImmutableBiMap.of(
    ParsedRecordDto.RecordTypeEnum.BIB, MarcFormat.BIBLIOGRAPHIC,
    ParsedRecordDto.RecordTypeEnum.AUTHORITY, MarcFormat.AUTHORITY,
    ParsedRecordDto.RecordTypeEnum.HOLDING, MarcFormat.HOLDINGS
  );
  private static final DateTimeFormatter DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD_FORMATTER =
    DateTimeFormatter.ofPattern("yyyyMMddHHmmss.S");
  private static final Pattern UUID_REGEX =
    Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

  private static final String DOLLAR_EXPRESSION = "{dollar}";

  private MarcUtils() {
  }

  /**
   * This method decode MARC date-time representation for.
   * Date and Time of Latest Transaction Field (005)
   * in the Java {@link LocalDateTime} value
   *
   * @param representation MARC date-time representation
   * @return Java {@link LocalDateTime} for MARC date-time value
   */
  public static LocalDateTime decodeFromMarcDateTime(String representation) {
    return LocalDateTime.parse(representation, DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD_FORMATTER);
  }

  /**
   * This method encode Java {@link java.time.LocalDateTime} value in the MARC date-time format for
   * <a href="https://www.loc.gov/marc/bibliographic/bd005.html">Date and Time of Latest Transaction Field (005)</a>.
   *
   * @param localDateTime {@link java.time.LocalDateTime} value
   * @return string with MARC date-time representation
   */
  public static String encodeToMarcDateTime(LocalDateTime localDateTime) {
    return localDateTime.format(DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD_FORMATTER);
  }

  public static Optional<FieldItem> getFieldByTag(BaseMarcRecord quickMarc, String tag) {
    return quickMarc.getFields().stream()
      .filter(field -> tag.equals(field.getTag()))
      .findFirst();
  }

  public static String restoreBlanks(String sourceString) {
    return sourceString.replace('\\', ' ');
  }

  public static String masqueradeBlanks(String sourceString) {
    return sourceString.replace(' ', '\\');
  }

  /**
   * Returns string with provided length and replaced spaces by '\\'. Trims sourceString to length or append '\\'.
   */
  public static String normalizeFixedLengthString(String sourceString, int length) {
    if (length <= 0 || length == Integer.MAX_VALUE) {
      throw new IllegalArgumentException("Length must be > 0 and < 2^31-1");
    }
    var source = masqueradeBlanks(StringUtils.trimToEmpty(sourceString));
    var sourceLength = source.length();
    if (sourceLength == length) {
      return source;
    } else if (sourceLength > length) {
      return source.substring(0, length);
    } else {
      return source + StringUtils.repeat('\\', length - sourceLength);
    }
  }

  public static boolean isValidUuid(String id) {
    return UUID_REGEX.matcher(id).matches();
  }

  /**
   * Extract subfields from FieldItem.
   *
   * @param field Quick Marc field.
   * @param subfieldFunction Function to map string to subfield object.
   * @param soft Indicates whether to throw exception on invalid data. soft=true doesn't throw.
   * */
  public static List<Subfield> extractSubfields(FieldItem field, Function<String, Subfield> subfieldFunction,
                                                boolean soft) {
    return Arrays.stream(SPLIT_PATTERN.split(field.getContent().toString()))
      .map(token -> {
        if (!soft && token.length() < TOKEN_MIN_LENGTH) {
          throw new IllegalArgumentException("Subfield length");
        }
        return token.replace(DOLLAR_EXPRESSION, "$");
      })
      .filter(StringUtils::isNotBlank)
      .map(subfieldFunction)
      .toList();
  }

  public static String convertDollar(String input) {
    return input.replace("$", DOLLAR_EXPRESSION);
  }

}
