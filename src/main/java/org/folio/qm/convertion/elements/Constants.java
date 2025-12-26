package org.folio.qm.convertion.elements;

import static org.folio.qm.convertion.elements.ControlFieldItem.ACQ_ENDDATE;
import static org.folio.qm.convertion.elements.ControlFieldItem.ACQ_METHOD;
import static org.folio.qm.convertion.elements.ControlFieldItem.ACQ_STATUS;
import static org.folio.qm.convertion.elements.ControlFieldItem.CAT_RULES;
import static org.folio.qm.convertion.elements.ControlFieldItem.COMPL;
import static org.folio.qm.convertion.elements.ControlFieldItem.COPIES;
import static org.folio.qm.convertion.elements.ControlFieldItem.DATE_ENTERED;
import static org.folio.qm.convertion.elements.ControlFieldItem.GEN_RET;
import static org.folio.qm.convertion.elements.ControlFieldItem.GEO_SUBD;
import static org.folio.qm.convertion.elements.ControlFieldItem.GOVT_AG;
import static org.folio.qm.convertion.elements.ControlFieldItem.KIND_REC;
import static org.folio.qm.convertion.elements.ControlFieldItem.LANG_AUTHORITY;
import static org.folio.qm.convertion.elements.ControlFieldItem.LANG_HOLDINGS;
import static org.folio.qm.convertion.elements.ControlFieldItem.LEND;
import static org.folio.qm.convertion.elements.ControlFieldItem.LEVEL_EST;
import static org.folio.qm.convertion.elements.ControlFieldItem.MAIN_USE;
import static org.folio.qm.convertion.elements.ControlFieldItem.MOD_REC;
import static org.folio.qm.convertion.elements.ControlFieldItem.NUMB_SERIES;
import static org.folio.qm.convertion.elements.ControlFieldItem.PERS_NAME;
import static org.folio.qm.convertion.elements.ControlFieldItem.REC_UPD;
import static org.folio.qm.convertion.elements.ControlFieldItem.REF_EVAL;
import static org.folio.qm.convertion.elements.ControlFieldItem.REPRO;
import static org.folio.qm.convertion.elements.ControlFieldItem.REPT_DATE;
import static org.folio.qm.convertion.elements.ControlFieldItem.ROMAN;
import static org.folio.qm.convertion.elements.ControlFieldItem.SEP_COMP;
import static org.folio.qm.convertion.elements.ControlFieldItem.SERIES;
import static org.folio.qm.convertion.elements.ControlFieldItem.SERIES_USE;
import static org.folio.qm.convertion.elements.ControlFieldItem.SH_SYS;
import static org.folio.qm.convertion.elements.ControlFieldItem.SOURCE;
import static org.folio.qm.convertion.elements.ControlFieldItem.SPEC_RET;
import static org.folio.qm.convertion.elements.ControlFieldItem.SUBJ_USE;
import static org.folio.qm.convertion.elements.ControlFieldItem.TYPE_SUBD;
import static org.folio.qm.convertion.elements.ControlFieldItem.UNDEF_18;
import static org.folio.qm.convertion.elements.ControlFieldItem.UNDEF_30;
import static org.folio.qm.convertion.elements.ControlFieldItem.UNDEF_34;
import static org.folio.qm.convertion.elements.LeaderItem.AUTHORITY_RECORD_TYPE;
import static org.folio.qm.convertion.elements.LeaderItem.CODING_SCHEME;
import static org.folio.qm.convertion.elements.LeaderItem.ENTRY_MAP_20;
import static org.folio.qm.convertion.elements.LeaderItem.ENTRY_MAP_21;
import static org.folio.qm.convertion.elements.LeaderItem.ENTRY_MAP_22;
import static org.folio.qm.convertion.elements.LeaderItem.ENTRY_MAP_23;
import static org.folio.qm.convertion.elements.LeaderItem.INDICATOR_COUNT;
import static org.folio.qm.convertion.elements.LeaderItem.PUNCTUATION_POLICY;
import static org.folio.qm.convertion.elements.LeaderItem.SUBFIELD_CODE_LENGTH;
import static org.folio.qm.convertion.elements.LeaderItem.UNDEFINED_CHARACTER_POSITION_19;
import static org.folio.qm.convertion.elements.LeaderItem.UNDEFINED_CHARACTER_POSITION_7;
import static org.folio.qm.convertion.elements.LeaderItem.UNDEFINED_CHARACTER_POSITION_8;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class Constants {

  public static final char SPACE_CHARACTER = ' ';

  public static final int TAG_006_CONTROL_FIELD_LENGTH = 18;
  public static final int TAG_008_HOLDINGS_CONTROL_FIELD_LENGTH = 32;
  public static final int TAG_008_AUTHORITY_CONTROL_FIELD_LENGTH = 40;
  public static final int TAG_008_BIBLIOGRAPHIC_CONTROL_FIELD_LENGTH = 40;
  public static final int LEADER_LENGTH = 24;
  public static final int LCCN_NEW_PREFIX_LENGTH = 2;
  public static final int LCCN_OLD_PREFIX_LENGTH = 3;
  public static final int SPECIFIC_ELEMENTS_BEGIN_INDEX = 18;
  public static final int SPECIFIC_ELEMENTS_END_INDEX = 35;

  public static final Pattern CONTROL_FIELD_PATTERN = Pattern.compile("^(00)[1-9]$");

  public static final String DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD = "005";
  public static final String TAG_001_CONTROL_FIELD = "001";
  public static final String TAG_006_CONTROL_FIELD = "006";
  public static final String TAG_007_CONTROL_FIELD = "007";
  public static final String TAG_008_CONTROL_FIELD = "008";
  public static final String TAG_999_FIELD = "999";
  public static final String LCCN_CONTROL_FIELD = "010";

  public static final String BLANK_REPLACEMENT = "\\";
  public static final String CATEGORY_NAME = "$categoryName";
  public static final String BLVL = "BLvl";
  public static final String TYPE = "Type";

  public static final Set<String> COMPLEX_CONTROL_FIELD_TAGS = Set.of(
    TAG_006_CONTROL_FIELD,
    TAG_007_CONTROL_FIELD,
    TAG_008_CONTROL_FIELD
  );

  public static final List<ControlFieldItem> AUTHORITY_CONTROL_FIELD_ITEMS = List.of(
    DATE_ENTERED,
    GEO_SUBD, ROMAN,
    LANG_AUTHORITY, KIND_REC, CAT_RULES, SH_SYS,
    SERIES, NUMB_SERIES, MAIN_USE, SUBJ_USE,
    SERIES_USE, TYPE_SUBD, UNDEF_18, GOVT_AG,
    REF_EVAL, UNDEF_30, REC_UPD, PERS_NAME,
    LEVEL_EST, UNDEF_34, MOD_REC, SOURCE);

  public static final List<ControlFieldItem> HOLDINGS_CONTROL_FIELD_ITEMS = List.of(ACQ_STATUS, ACQ_METHOD,
    ACQ_ENDDATE, COMPL, COPIES,
    DATE_ENTERED, GEN_RET, LANG_HOLDINGS, LEND, REPRO,
    REPT_DATE, SEP_COMP, SPEC_RET);

  public static final List<LeaderItem> COMMON_LEADER_ITEMS =
    List.of(CODING_SCHEME, INDICATOR_COUNT, SUBFIELD_CODE_LENGTH,
      ENTRY_MAP_20, ENTRY_MAP_21, ENTRY_MAP_22, ENTRY_MAP_23);

  public static final List<LeaderItem> COMMON_CONSTANT_LEADER_ITEMS =
    List.of(INDICATOR_COUNT, SUBFIELD_CODE_LENGTH,
      ENTRY_MAP_20, ENTRY_MAP_21, ENTRY_MAP_22, ENTRY_MAP_23);

  public static final List<LeaderItem> AUTHORITY_CONSTANT_LEADER_ITEMS = List.of(
    UNDEFINED_CHARACTER_POSITION_7, UNDEFINED_CHARACTER_POSITION_8, UNDEFINED_CHARACTER_POSITION_19,
    AUTHORITY_RECORD_TYPE, CODING_SCHEME, PUNCTUATION_POLICY,
    INDICATOR_COUNT, SUBFIELD_CODE_LENGTH,
    ENTRY_MAP_20, ENTRY_MAP_21, ENTRY_MAP_22, ENTRY_MAP_23);

  public static final List<LeaderItem> HOLDINGS_CONSTANT_LEADER_ITEMS = List.of(
    UNDEFINED_CHARACTER_POSITION_7, UNDEFINED_CHARACTER_POSITION_8, UNDEFINED_CHARACTER_POSITION_19,
    INDICATOR_COUNT, SUBFIELD_CODE_LENGTH,
    ENTRY_MAP_20, ENTRY_MAP_21, ENTRY_MAP_22, ENTRY_MAP_23);

  private Constants() {
  }
}
