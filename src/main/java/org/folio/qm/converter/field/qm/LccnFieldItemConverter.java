package org.folio.qm.converter.field.qm;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.repeat;

import static org.folio.qm.converter.elements.Constants.LCCN_CONTROL_FIELD;
import static org.folio.qm.converter.elements.Constants.LCCN_NEW_PREFIX_LENGTH;
import static org.folio.qm.converter.elements.Constants.LCCN_OLD_PREFIX_LENGTH;
import static org.folio.qm.converter.elements.Constants.SPACE_CHARACTER;

import java.util.regex.Pattern;

import org.marc4j.marc.Subfield;
import org.marc4j.marc.impl.SubfieldImpl;
import org.springframework.stereotype.Component;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;

@Component
public class LccnFieldItemConverter extends AbstractFieldItemConverter {

  private static final Pattern LCCN_FIELD_PATTERN = Pattern.compile("[$][abz].*$");
  private static final Pattern LCCN_10_L_FIELD_PATTERN = Pattern.compile("\\d{10}");
  private static final Pattern D_8 = Pattern.compile("\\d{8}");
  private static final Pattern D_8_S_$ = Pattern.compile("\\d{8}\\s/.*$");
  private static final Pattern A_Z_S_3_D_8 = Pattern.compile("[a-z\\s]{3}\\d{8}");

  @Override
  protected Subfield subfieldFromString(String string) {
    String lccnString = string.substring(2).trim();
    if (LCCN_FIELD_PATTERN.matcher(string).matches()) {
      if (LCCN_10_L_FIELD_PATTERN.matcher(lccnString).matches()) {
        lccnString = repeat(SPACE_CHARACTER, LCCN_NEW_PREFIX_LENGTH).concat(lccnString);
      } else if (D_8.matcher(lccnString).matches()) {
        lccnString = repeat(SPACE_CHARACTER, LCCN_OLD_PREFIX_LENGTH).concat(lccnString).concat(SPACE);
      } else if (D_8_S_$.matcher(lccnString).matches()) {
        lccnString = repeat(SPACE_CHARACTER, LCCN_OLD_PREFIX_LENGTH).concat(lccnString);
      } else if (A_Z_S_3_D_8.matcher(lccnString).matches()) {
        lccnString = lccnString.concat(SPACE);
      }
    } else {
      System.out.println(1);
    }
    return new SubfieldImpl(string.charAt(1), lccnString);
  }

  @Override
  public boolean canProcess(FieldItem field, MarcFormat marcFormat) {
    return field.getTag().equals(LCCN_CONTROL_FIELD) && !isControlField(field);
  }
}
