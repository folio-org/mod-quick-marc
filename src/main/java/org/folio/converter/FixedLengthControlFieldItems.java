package org.folio.converter;

import static org.folio.util.Constants.DIMENSIONS_CONST;
import static org.folio.util.Constants.POSITIVE_NEGATIVE_ASPECT;

public enum FixedLengthControlFieldItems {

  ENTERED("Entered", 0, 6, false),
  DTST("DtSt", 6, 1, false),
  DATE1("Date1", 7, 4, false),
  DATE2("Date2", 11, 4, false),
  CTRY("Ctry", 15, 3, false),
  VALUE("Value", 0, 17, false),
  ILLS("Ills", 0, 4, true),
  FREQ("Freq", 0, 1, false),
  RELF("Relf", 0, 4, true),
  COMP("Comp", 0, 2, false),
  TIME("Time", 0, 3, true),
  REGL("Regl", 1, 1, false),
  FMUS("FMus", 2, 1, false),
  SRTP("SrTp", 3, 1, false),
  PART("Part", 3, 1, false),
  AUDN("Audn", 4, 1, false),
  ORIG("Orig", 4, 1, false),
  PROJ("Proj", 4, 2, false),
  FORM("Form", 5, 1, false),
  CONT_B("Cont", 6, 4, true),
  ENTW("EntW", 6, 1, false),
  ACCM("AccM", 6, 6, true),
  CONT_C("Cont", 7, 3, true),
  CRTP("CrTp", 7, 1, false),
  FILE("File", 8, 1, false),
  GPUB("GPub", 10, 1, false),
  CONF("Conf", 11, 1, false),
  FORM_MV("Form", 11, 1, false),
  FEST("Fest",12, 1, false),
  LTXT("LTxt", 12, 2, true),
  INDX("Indx", 13, 1, false),
  LITF("LitF", 15, 1 ,false),
  ALPH("Alph", 15, 1, false),
  SPFM("SpFm", 15, 2, false),
  TRAR("TrAr", 15, 1, false),
  TMAT("TMat", 15, 1, false),
  BIOG("Biog", 16, 1, false),
  SL("S/L", 16, 1, false),
  TECH("Tech", 16, 1, false),
  LANG("Lang", 35, 3, false),
  MREC("MRec", 38, 1, false),
  SRCE("Srce", 39, 1, false),
  CATEGORY("Category", 0, 1, false),
  SMD("SMD", 1, 1, false),
  COLOR("Color",3,1, false),
  DIMENSIONS(DIMENSIONS_CONST, 4, 1, false),
  SOUND("Sound", 5, 1, false),
  IMAGE_BIT_DEPTH("Image bit depth", 6, 3, false),
  FILE_FORMATS("File formats", 9, 1, false),
  QUALITY_ASSURANCE_TARGET("Quality assurance target(s)", 10, 1, false),
  ANTECEDENT("Antecedent/ Source", 11, 1, false),
  LEVEL_OF_COMPRESSION("Level of compression", 12, 1, false),
  REFORMATTING_QUALITY("Reformatting quality", 13, 1, false),
  PHYSICAL_MEDIUM("Physical medium", 4, 1, false),
  TYPE_OF_REPRODUCTION("Type of reproduction", 5, 1, false),
  PRODUCTION_DETAILS("Production/reproduction details", 6, 1, false),
  ASPECT_MAP(POSITIVE_NEGATIVE_ASPECT, 7, 1, false),
  ASPECT_MICROFORM(POSITIVE_NEGATIVE_ASPECT, 3, 1, false),
  REDUCTION_RATIO("Reduction ratio range/Reduction ratio", 5, 4, false),
  COLOR_MICROFORM("Color", 9, 1, false),
  EMULSION_ON_FILM("Emulsion on film", 10, 1, false),
  GENERATION("Generation", 11, 1, false),
  BASE_OF_FILM("Base of film", 12, 1, false),
  MOTION_PICTURE_PRESENTATION_FORMAT("Motion picture presentation format", 4, 1, false),
  SOUND_ON_MEDIUM("Sound on medium or separate", 5, 1, false),
  MEDIUM_FOR_SOUND("Medium for sound", 6, 1, false),
  DIMENSIONS_MOTION(DIMENSIONS_CONST, 7, 1, false),
  CONFIGURATION_OF_CHANNELS("Configuration of playback channels", 8, 1, false),
  PRODUCTION_ELEMENTS("Production elements", 9, 1, false),
  ASPECT_MOTION(POSITIVE_NEGATIVE_ASPECT, 10, 1, false),
  REFINED_CATEGORIES("Refined categories of color", 13, 1, false),
  KIND_OF_COLOR("Kind of color stock or print", 14, 1, false),
  DETERIORATION_STAGE("Deterioration stage", 15, 1, false),
  COMPLETENESS("Completeness", 16, 1, false),
  FILM_INSPECTION_DATE("Film inspection date", 17, 6, false),
  PRIMARY_SUPPORT("Primary support material", 4, 1, false),
  SECONDARY_SUPPORT("Secondary support material", 5, 1, false),
  BASE_OF_EMULSION("Base of emulsion", 4, 1, false),
  SECONDARY_SUPPORT_PROJECTED("Secondary support material", 8, 1, false),
  ALTITUDE("Altitude of sensor", 3, 1, false),
  ATTITUDE("Attitude of sensor", 4, 1, false),
  CLOUD_COVER("Cloud cover", 5, 1, false),
  PLATFORM_CONSTRUCTION_TYPE("Platform construction type", 6, 1, false),
  PLATFORM_USE_CATEGORY("Platform use category", 7, 1, false),
  SENSOR_TYPE("Sensor type", 8, 1, false),
  DATA_TYPE("Data type", 9, 2, false),
  SPEED("Speed", 3, 1, false),
  CONFIGURATION_OF_CHANNELS_SOUND("Configuration of playback channels", 4, 1, false),
  GROOVE("Groove width/ groove pitch", 5, 1, false),
  DIMENSIONS_SOUND(DIMENSIONS_CONST, 6, 1, false),
  TAPE_WIDTH("Tape width", 7, 1, false),
  TAPE_CONFIGURATION("Tape configuration", 8, 1, false),
  KIND_OF_DISC("Kind of disc, cylinder, or tape", 9, 1, false),
  KIND_OF_MATERIAL("Kind of material", 10, 1, false),
  KIND_OF_CUTTING("Kind of cutting", 11, 1, false),
  SPECIAL_PLAYBACK_CHARACTERISTICS("Special playback characteristics", 12, 1, false),
  CAPTURE_TECHNIQUE("Capture and storage technique", 13, 1, false),
  CLASS_OF_BRAILLE("Class of braille writing", 3, 2, false),
  LEVEL_OF_CONTRACTION("Level of contraction", 5, 1, false),
  BRAILLE_MUSIC_FORMAT("Braille music format", 6, 3, false),
  SPECIAL_PHYSICAL_CHARACTERISTICS("Special physical characteristics", 9, 1, false),
  VIDEORECORDING_FORMAT("Videorecording format", 4, 1, false),
  VAL("Value", 0, 0, false);

  private String name;
  private int position;
  private int length;
  private boolean array;

  FixedLengthControlFieldItems(String name, int position, int length, boolean array) {
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
}
