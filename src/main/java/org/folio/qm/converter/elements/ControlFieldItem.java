package org.folio.qm.converter.elements;

public enum ControlFieldItem {

  ACCM("AccM", 7, 6, true),
  ALPH("Alph", 16, 1, false),
  ALTITUDE("Altitude of sensor", 3, 1, false),
  ANTECEDENT("Antecedent/ Source", 11, 1, false),
  ASPECT_MAP(Constants.POSITIVE_NEGATIVE_ASPECT, 7, 1, false),
  ASPECT_MICROFORM(Constants.POSITIVE_NEGATIVE_ASPECT, 3, 1, false),
  ASPECT_MOTION(Constants.POSITIVE_NEGATIVE_ASPECT, 10, 1, false),
  ATTITUDE("Attitude of sensor", 4, 1, false),
  AUDN("Audn", 5, 1, false),
  BASE_OF_EMULSION("Base of emulsion", 4, 1, false),
  BASE_OF_FILM("Base of film", 12, 1, false),
  BIOG("Biog", 17, 1, false),
  BRAILLE_MUSIC_FORMAT("Braille music format", 6, 3, false),
  CAPTURE_TECHNIQUE("Capture and storage technique", 13, 1, false),
  CATEGORY("Category", 0, 1, false),
  CLASS_OF_BRAILLE("Class of braille writing", 3, 2, false),
  CLOUD_COVER("Cloud cover", 5, 1, false),
  COLOR("Color", 3, 1, false),
  COLOR_MICROFORM("Color", 9, 1, false),
  COMP("Comp", 1, 2, false),
  COMPLETENESS("Completeness", 16, 1, false),
  CONF("Conf", 12, 1, false),
  CONFIGURATION_OF_CHANNELS("Configuration of playback channels", 8, 1, false),
  CONFIGURATION_OF_CHANNELS_SOUND("Configuration of playback channels", 4, 1, false),
  CONT_B("Cont", 7, 4, true),
  CONT_C("Cont", 8, 3, true),
  CRTP("CrTp", 8, 1, false),
  CTRY("Ctry", 16, 3, false),
  DATA_TYPE("Data type", 9, 2, false),
  DATE1("Date1", 8, 4, false),
  DATE2("Date2", 12, 4, false),
  DETERIORATION_STAGE("Deterioration stage", 15, 1, false),
  DIMENSIONS(Constants.DIMENSIONS_NAME, 4, 1, false),
  DIMENSIONS_MOTION(Constants.DIMENSIONS_NAME, 7, 1, false),
  DIMENSIONS_SOUND(Constants.DIMENSIONS_NAME, 6, 1, false),
  DTST("DtSt", 7, 1, false),
  EMULSION_ON_FILM("Emulsion on film", 10, 1, false),
  ENTERED("Entered", 1, 6, false),
  ENTW("EntW", 7, 1, false),
  FEST("Fest", 13, 1, false),
  FILE("File", 9, 1, false),
  FILE_FORMATS("File formats", 9, 1, false),
  FILM_INSPECTION_DATE("Film inspection date", 17, 6, false),
  FMUS("FMus", 3, 1, false),
  FORM("Form", 6, 1, false),
  FORM_MV("Form", 12, 1, false),
  FREQ("Freq", 1, 1, false),
  GENERATION("Generation", 11, 1, false),
  GPUB("GPub", 11, 1, false),
  GROOVE("Groove width/ groove pitch", 5, 1, false),
  ILLS("Ills", 1, 4, true),
  IMAGE_BIT_DEPTH("Image bit depth", 6, 3, false),
  INDX("Indx", 14, 1, false),
  KIND_OF_COLOR("Kind of color stock or print", 14, 1, false),
  KIND_OF_CUTTING("Kind of cutting", 11, 1, false),
  KIND_OF_DISC("Kind of disc, cylinder, or tape", 9, 1, false),
  KIND_OF_MATERIAL("Kind of material", 10, 1, false),
  LANG("Lang", 36, 3, false),
  LEVEL_OF_COMPRESSION("Level of compression", 12, 1, false),
  LEVEL_OF_CONTRACTION("Level of contraction", 5, 1, false),
  LITF("LitF", 16, 1, false),
  LTXT("LTxt", 13, 2, true),
  MEDIUM_FOR_SOUND("Medium for sound", 6, 1, false),
  MOTION_PICTURE_PRESENTATION_FORMAT("Motion picture presentation format", 4, 1, false),
  MREC("MRec", 39, 1, false),
  ORIG("Orig", 5, 1, false),
  PART("Part", 4, 1, false),
  PHYSICAL_MEDIUM("Physical medium", 4, 1, false),
  PLATFORM_CONSTRUCTION_TYPE("Platform construction type", 6, 1, false),
  PLATFORM_USE_CATEGORY("Platform use category", 7, 1, false),
  PRIMARY_SUPPORT("Primary support material", 4, 1, false),
  PRODUCTION_DETAILS("Production/reproduction details", 6, 1, false),
  PRODUCTION_ELEMENTS("Production elements", 9, 1, false),
  PROJ("Proj", 5, 2, true),
  QUALITY_ASSURANCE_TARGET("Quality assurance target(s)", 10, 1, false),
  REDUCTION_RATIO("Reduction ratio range/Reduction ratio", 5, 4, false),
  REFINED_CATEGORIES("Refined categories of color", 13, 1, false),
  REFORMATTING_QUALITY("Reformatting quality", 13, 1, false),
  REGL("Regl", 2, 1, false),
  RELF("Relf", 1, 4, true),
  SECONDARY_SUPPORT("Secondary support material", 5, 1, false),
  SECONDARY_SUPPORT_PROJECTED("Secondary support material", 8, 1, false),
  SENSOR_TYPE("Sensor type", 8, 1, false),
  SL("S/L", 17, 1, false),
  SMD("SMD", 1, 1, false),
  SOUND("Sound", 5, 1, false),
  SOUND_ON_MEDIUM("Sound on medium or separate", 5, 1, false),
  SPECIAL_PHYSICAL_CHARACTERISTICS("Special physical characteristics", 9, 1, false),
  SPECIAL_PLAYBACK_CHARACTERISTICS("Special playback characteristics", 12, 1, false),
  SPEED("Speed", 3, 1, false),
  SPFM("SpFm", 16, 2, true),
  SRCE("Srce", 40, 1, false),
  SRTP("SrTp", 4, 1, false),
  TAPE_CONFIGURATION("Tape configuration", 8, 1, false),
  TAPE_WIDTH("Tape width", 7, 1, false),
  TECH("Tech", 17, 1, false),
  TIME("Time", 1, 3, true),
  TMAT("TMat", 16, 1, false),
  TRAR("TrAr", 16, 1, false),
  TYPE("Type", 0, 1, false),
  TYPE_OF_REPRODUCTION("Type of reproduction", 5, 1, false),
  VAL("Value", 0, 0, false),
  VALUE("Value", 1, 17, false),
  VIDEORECORDING_FORMAT("Videorecording format", 4, 1, false),

  //  MARC HOLDINGS
  //  008 field
  ACQ_STATUS("AcqStatus", 6, 1, false),
  ACQ_METHOD("AcqMethod", 7, 1, false),
  ACQ_ENDDATE("AcqEndDate", 8, 4, false),
  COMPL("Compl", 16, 1, false),
  COPIES("Copies", 17, 3, false),
  DATE_ENTERED("Date Ent", 0, 6, false),
  GEN_RET("Gen ret", 12, 1, false),
  LANG_HOLDINGS("Lang", 22, 3, false),
  LEND("Lend", 20, 1, false),
  REPRO("Repro", 21, 1, false),
  REPT_DATE("Rept date", 26, 6, false),
  SEP_COMP("Sep/comp", 25, 1, false),
  SPEC_RET("Spec ret", 13, 3, true),

  //  MARC AUTHORITY
  //  008 field
  GEO_SUBD("Geo Subd", 6, 1, false),
  ROMAN("Roman", 7, 1, false),
  LANG_AUTHORITY("Lang", 8, 1, false),
  KIND_REC("Kind rec", 9, 1, false),
  CAT_RULES("Cat Rules", 10, 1, false),
  SH_SYS("SH Sys", 11, 1, false),
  SERIES("Series", 12, 1, false),
  NUMB_SERIES("Numb Series", 13, 1, false),
  MAIN_USE("Main use", 14, 1, false),
  SUBJ_USE("Subj use", 15, 1, false),
  SERIES_USE("Series use", 16, 1, false),
  TYPE_SUBD("Type Subd", 17, 1, false),
  UNDEF_18("Undef_18", 18, 10, false),
  GOVT_AG("Govt Ag", 28, 1, false),
  REF_EVAL("RefEval", 29, 1, false),
  UNDEF_30("Undef_30", 30, 1, false),
  REC_UPD("RecUpd", 31, 1, false),
  PERS_NAME("Pers Name", 32, 1, false),
  LEVEL_EST("Level Est", 33, 1, false),
  UNDEF_34("Undef_34", 34, 4, false),
  MOD_REC_EST("Mod Rec Est", 38, 1, false),
  SOURCE("Source", 39, 1, false);

  private final String name;
  private final int position;
  private final int length;
  private final boolean array;

  ControlFieldItem(String name, int position, int length, boolean array) {
    this.name = name;
    this.position = position;
    this.length = length;
    this.array = array;
  }

  public String getName() {
    return name;
  }

  public int getPosition() {
    return position;
  }

  public int getLength() {
    return length;
  }

  public boolean isArray() {
    return array;
  }

  private static class Constants {
    private static final String POSITIVE_NEGATIVE_ASPECT = "Positive/negative aspect";
    private static final String DIMENSIONS_NAME = "Dimensions";
  }
}
