package org.folio.converter;

import static org.folio.converter.FixedLengthControlFieldItems.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum ContentType {
  BOOKS("Books", Arrays.asList(ILLS, AUDN, FORM, CONT_B, GPUB, CONF, FEST, INDX, LITF, BIOG)),
  FILES("Computer Files", Arrays.asList(AUDN, FORM, FILE, GPUB)),
  CONTINUING("Continuing Resources", Arrays.asList(FREQ, REGL, SRTP, ORIG, FORM, ENTW, CONT_C, GPUB, CONF, ALPH, SL)),
  MAPS("Maps", Arrays.asList(RELF, PROJ, CRTP, GPUB, FORM_MV, INDX, SPFM)),
  MIXED("Mixed Materials", Collections.singletonList(FORM)),
  SCORES("Scores", Arrays.asList(COMP, FMUS, PART, AUDN, FORM, ACCM, LTXT, TRAR)),
  SOUND("Sound Recordings", Arrays.asList(COMP, FMUS, PART, AUDN, FORM, ACCM, LTXT, TRAR)),
  VISUAL("Visual Materials", Arrays.asList(TIME, AUDN, GPUB, FORM_MV, TMAT, TECH)),
  UNKNOWN("Unknown Type", Collections.singletonList(VALUE));

  private String name;
  private List<FixedLengthControlFieldItems> fixedLengthControlFieldItems;

  ContentType(String name, List<FixedLengthControlFieldItems> fixedLengthControlFieldItems){
    this.name = name;
    this.fixedLengthControlFieldItems = fixedLengthControlFieldItems;
  }

  public static List<FixedLengthControlFieldItems> getCommonItems() {
    return Arrays.asList(ENTERED, DTST, DATE1, DATE2, CTRY, LANG, MREC, SRCE);
  }

  public String getName(){
    return name;
  }

  public List<FixedLengthControlFieldItems> getFixedLengthControlFieldItems() {
    return fixedLengthControlFieldItems;
  }

  public static ContentType getByName(String typeName) {
    for(ContentType type: values()) {
      if (type.name.equals(typeName)) {
        return type;
      }
    }
    return UNKNOWN;
  }

  public static ContentType resolveContentType(char code) {
    switch (code) {
      case 'a':
      case 't':
        return BOOKS;
      case 's':
        return CONTINUING;
      case 'm':
        return FILES;
      case 'e':
      case 'f':
        return MAPS;
      case 'p':
        return MIXED;
      case 'i':
      case 'j':
        return SOUND;
      case 'c':
      case 'd':
        return SCORES;
      case 'g':
      case 'k':
      case 'o':
      case 'r':
        return VISUAL;
      default:
        return UNKNOWN;
    }
  }
}
