package org.folio.converter;

import static org.folio.converter.Field008Items.*;

import org.marc4j.marc.Leader;

import java.util.Arrays;
import java.util.List;

public enum ContentType {
  BOOKS("Books", Arrays.asList(ENTERED, DTST, DATE1, DATE2, CTRY, ILLS, AUDN, FORM, CONT_B, GPUB, CONF, FEST, INDX, LITF, BIOG, LANG, MREC, SRCE)),
  FILES("Computer Files", Arrays.asList(ENTERED, DTST, DATE1, DATE2, CTRY, AUDN, FORM, FILE, GPUB, LANG, MREC, SRCE)),
  CONTINUING("Continuing Resources", Arrays.asList(ENTERED, DTST, DATE1, DATE2, CTRY, FREQ, REGL, SRTP, ORIG, FORM, ENTW, CONT_C, GPUB, CONF, ALPH, SL, LANG, MREC, SRCE)),
  MAPS("Maps", Arrays.asList(ENTERED, DTST, DATE1, DATE2, CTRY, RELF, PROJ, CRTP, GPUB, FORM_MV, INDX, SPFM, LANG, MREC, SRCE)),
  MIXED("Mixed Materials", Arrays.asList(ENTERED, DTST, DATE1, DATE2, CTRY, FORM, LANG, MREC, SRCE)),
  SCORES("Scores", Arrays.asList(ENTERED, DTST, DATE1, DATE2, CTRY, COMP, FMUS, PART, AUDN, FORM, ACCM, LTXT, TRAR, LANG, MREC, SRCE)),
  SOUND("Sound Recordings", Arrays.asList(ENTERED, DTST, DATE1, DATE2, CTRY, COMP, FMUS, PART, AUDN, FORM, ACCM, LTXT, TRAR, LANG, MREC, SRCE)),
  VISUAL("Visual Materials", Arrays.asList(ENTERED, DTST, DATE1, DATE2, CTRY, TIME, AUDN, GPUB, FORM_MV, TMAT, TECH, LANG, MREC, SRCE)),
  UNKNOWN("Unknown Type", Arrays.asList(ENTERED, DTST, DATE1, DATE2, CTRY, VALUE, LANG, MREC, SRCE));

  private String name;
  private List<Field008Items> field008Items;

  ContentType(String name, List<Field008Items> field008Items){
    this.name = name;
    this.field008Items = field008Items;
  }

  public String getName(){
    return name;
  }

  public List<Field008Items> getField008Items() {
    return field008Items;
  }

  public static ContentType getByName(String name) {
    for(ContentType type: values()) {
      if (type.name.equals(name)) {
        return type;
      }
    }
    return UNKNOWN;
  }

  public static ContentType resolveContentType(Leader leader) {
    switch (leader.getTypeOfRecord()) {
      case 'a':
        return (Arrays.asList("b", "i", "s").contains(Character.toString(leader.getImplDefined1()[0])))? CONTINUING: BOOKS;
      case 't':
        return BOOKS;
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
