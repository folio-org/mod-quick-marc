package org.folio.qm.service.population.impl;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.folio.qm.converter.elements.Constants.LCCN_CONTROL_FIELD;
import static org.folio.qm.converter.elements.Constants.LCCN_NEW_PREFIX_LENGTH;
import static org.folio.qm.converter.elements.Constants.LCCN_OLD_PREFIX_LENGTH;
import static org.folio.qm.converter.elements.Constants.SPACE_CHARACTER;
import static org.folio.qm.util.MarcUtils.extractSubfields;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.service.population.FieldItemMarcPopulationService;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.impl.SubfieldImpl;
import org.springframework.stereotype.Service;


@Service
public class Tag010FieldItemPopulationService extends FieldItemMarcPopulationService {

  private static final Pattern LCCN_FIELD_PATTERN = Pattern.compile("[$][abz].*$");
  private static final Pattern LCCN_10_DIGITS_ONLY_PATTERN = Pattern.compile("\\d{10}");
  private static final Pattern LCCN_10_ONLY_LETTER_PREFIX_PATTERN = Pattern.compile("[a-z]\\d{10}");
  private static final Pattern LCCN_8_DIGITS_ONLY_PATTERN = Pattern.compile("\\d{8}");
  private static final Pattern LCCN_8_ONE_OR_TWO_LETTER_PREFIX_PATTERN = Pattern.compile("([a-z][ |a-z]? ?)(\\d{8})");
  private static final Pattern LCCN_8_NO_TRAILING_SPACE_PATTERN = Pattern.compile("[a-z]{3}\\d{8}");
  private static final Pattern D_8_S_DOLLAR_SIGN = Pattern.compile("\\d{8}\\s/.*$");

  @Override
  protected boolean canProcess(FieldItem field) {
    return field.getTag().equals(LCCN_CONTROL_FIELD);
  }

  @Override
  protected void populateValues(FieldItem fieldItem, MarcFormat marcFormat) {
    var content = extractSubfields(fieldItem, this::subfieldFromString, true).stream()
      .map(Subfield::toString)
      .collect(Collectors.joining());
    fieldItem.setContent(content);
  }

  @Override
  public boolean supportFormat(MarcFormat marcFormat) {
    return marcFormat == MarcFormat.BIBLIOGRAPHIC || marcFormat == MarcFormat.AUTHORITY;
  }

  private Subfield subfieldFromString(String string) {
    String lccnString = string.substring(2).trim();
    if (LCCN_FIELD_PATTERN.matcher(string).matches()) {
      if (LCCN_10_DIGITS_ONLY_PATTERN.matcher(lccnString).matches()) {
        lccnString = repeat(SPACE_CHARACTER, LCCN_NEW_PREFIX_LENGTH).concat(lccnString);
      } else if (LCCN_10_ONLY_LETTER_PREFIX_PATTERN.matcher(lccnString).matches()) {
        var prefix = String.valueOf(lccnString.charAt(0));
        var digits = lccnString.substring(1);
        lccnString = prefix.concat(SPACE).concat(digits);
      } else if (LCCN_8_DIGITS_ONLY_PATTERN.matcher(lccnString).matches()) {
        lccnString = repeat(SPACE_CHARACTER, LCCN_OLD_PREFIX_LENGTH).concat(lccnString).concat(SPACE);
      } else if (LCCN_8_NO_TRAILING_SPACE_PATTERN.matcher(lccnString).matches()) {
        lccnString = lccnString.concat(SPACE);
      } else if (D_8_S_DOLLAR_SIGN.matcher(lccnString).matches()) {
        lccnString = repeat(SPACE_CHARACTER, LCCN_OLD_PREFIX_LENGTH).concat(lccnString);
      } else {
        var matcher = LCCN_8_ONE_OR_TWO_LETTER_PREFIX_PATTERN.matcher(lccnString);
        if (matcher.matches()) {
          var prefix = matcher.group(1).trim();
          var digits = matcher.group(2);
          lccnString = prefix.concat(repeat(SPACE_CHARACTER, LCCN_OLD_PREFIX_LENGTH - prefix.length()))
            .concat(digits).concat(SPACE);
        }
      }
    }
    return new SubfieldImpl(string.charAt(1), lccnString);
  }
}
