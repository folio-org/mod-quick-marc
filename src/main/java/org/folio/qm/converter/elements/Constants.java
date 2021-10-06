package org.folio.qm.converter.elements;

import java.util.regex.Pattern;

public class Constants {

  public static final char SPACE_CHARACTER = ' ';

  public static final int ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD_LENGTH = 18;
  public static final int BIBLIOGRAPHIC_GENERAL_INFORMATION_CONTROL_FIELD_LENGTH = 40;
  public static final int HOLDINGS_GENERAL_INFORMATION_CONTROL_FIELD_LENGTH = 32;
  public static final int AUTHORITY_GENERAL_INFORMATION_CONTROL_FIELD_LENGTH = 40;
  public static final int TAG_LENGTH = 4;
  public static final int TERMINATOR_LENGTH = 1;
  public static final int TOKEN_MIN_LENGTH = 3;
  public static final int ADDRESS_LENGTH = 12;
  public static final int LEADER_LENGTH = 24;
  public static final int TYPE_OF_RECORD_LEADER_POS = 6;
  public static final int BLVL_LEADER_POS = 7;
  public static final int DESC_LEADER_POS = 18;
  public static final int ELVL_LEADER_POS = 17;
  public static final int LCCN_NEW_PREFIX_LENGTH = 2;
  public static final int LCCN_OLD_PREFIX_LENGTH = 3;
  public static final int SPECIFIC_ELEMENTS_BEGIN_INDEX = 18;
  public static final int SPECIFIC_ELEMENTS_END_INDEX = 35;

  public static final Pattern CONTROL_FIELD_PATTERN = Pattern.compile("^(00)[1-9]$");
  public static final Pattern SPLIT_PATTERN = Pattern.compile("(?=[$][a-z0-9])");
  public static final String CONCAT_CONDITION_PATTERN = "(?:[$][1]\\s*|[$]\\d+(?:[.,])[^\\\\]*)$";

  public static final String DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD = "005";
  public static final String ADDITIONAL_CHARACTERISTICS_CONTROL_FIELD = "006";
  public static final String PHYSICAL_DESCRIPTIONS_CONTROL_FIELD = "007";
  public static final String GENERAL_INFORMATION_CONTROL_FIELD = "008";
  public static final String LCCN_CONTROL_FIELD = "010";

  public static final String BLANK_REPLACEMENT = "\\";
  public static final String BLVL = "BLvl";
  public static final String CATEGORY_NAME = "$categoryName";
  public static final String DESC = "Desc";
  public static final String DIMENSIONS_CONST = "Dimensions";
  public static final String ELVL = "ELvl";
  public static final String FIELDS = "fields";
  public static final String INDICATOR1 = "ind1";
  public static final String INDICATOR2 = "ind2";
  public static final String LEADER = "leader";
  public static final String POSITIVE_NEGATIVE_ASPECT = "Positive/negative aspect";
  public static final String SUBFIELDS = "subfields";
  public static final String TYPE = "Type";

  private Constants() {
  }
}
