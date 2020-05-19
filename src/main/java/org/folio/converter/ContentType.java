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

  public static String resolveTypeOfRecord(char code) {
    switch (code) {
      case 'a': return "Language material";
      case 'c': return "Notated music";
      case 'd': return "Manuscript notated music";
      case 'e': return "Cartographic material";
      case 'f': return "Manuscript cartographic material";
      case 'g': return "Projected medium";
      case 'i': return "Nonmusical sound recording";
      case 'j': return "Musical sound recording";
      case 'k': return "Two-dimensional nonprojectable graphic";
      case 'm': return "Computer file";
      case 'o': return "Kit";
      case 'p': return "Mixed materials";
      case 'r': return "Three-dimensional artifact or naturally occurring object";
      case 's': return "Serial/integrating resource";
      case 't': return "Manuscript language material";
      default: return Character.toString(code);
    }
  }
}
