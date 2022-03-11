package org.folio.qm.converter.elements;

import static org.folio.qm.converter.elements.Constants.BLVL_LEADER_POS;
import static org.folio.qm.converter.elements.Constants.TYPE_OF_RECORD_LEADER_POS;
import static org.folio.qm.converter.elements.ControlFieldItem.ACCM;
import static org.folio.qm.converter.elements.ControlFieldItem.ALPH;
import static org.folio.qm.converter.elements.ControlFieldItem.AUDN;
import static org.folio.qm.converter.elements.ControlFieldItem.BIOG;
import static org.folio.qm.converter.elements.ControlFieldItem.COMP;
import static org.folio.qm.converter.elements.ControlFieldItem.CONF;
import static org.folio.qm.converter.elements.ControlFieldItem.CONT_B;
import static org.folio.qm.converter.elements.ControlFieldItem.CONT_C;
import static org.folio.qm.converter.elements.ControlFieldItem.CRTP;
import static org.folio.qm.converter.elements.ControlFieldItem.CTRY;
import static org.folio.qm.converter.elements.ControlFieldItem.DATE1;
import static org.folio.qm.converter.elements.ControlFieldItem.DATE2;
import static org.folio.qm.converter.elements.ControlFieldItem.DTST;
import static org.folio.qm.converter.elements.ControlFieldItem.ENTERED;
import static org.folio.qm.converter.elements.ControlFieldItem.ENTW;
import static org.folio.qm.converter.elements.ControlFieldItem.FEST;
import static org.folio.qm.converter.elements.ControlFieldItem.FILE;
import static org.folio.qm.converter.elements.ControlFieldItem.FMUS;
import static org.folio.qm.converter.elements.ControlFieldItem.FORM;
import static org.folio.qm.converter.elements.ControlFieldItem.FORM_MV;
import static org.folio.qm.converter.elements.ControlFieldItem.FREQ;
import static org.folio.qm.converter.elements.ControlFieldItem.GPUB;
import static org.folio.qm.converter.elements.ControlFieldItem.ILLS;
import static org.folio.qm.converter.elements.ControlFieldItem.INDX;
import static org.folio.qm.converter.elements.ControlFieldItem.LANG;
import static org.folio.qm.converter.elements.ControlFieldItem.LITF;
import static org.folio.qm.converter.elements.ControlFieldItem.LTXT;
import static org.folio.qm.converter.elements.ControlFieldItem.MREC;
import static org.folio.qm.converter.elements.ControlFieldItem.ORIG;
import static org.folio.qm.converter.elements.ControlFieldItem.PART;
import static org.folio.qm.converter.elements.ControlFieldItem.PROJ;
import static org.folio.qm.converter.elements.ControlFieldItem.REGL;
import static org.folio.qm.converter.elements.ControlFieldItem.RELF;
import static org.folio.qm.converter.elements.ControlFieldItem.SL;
import static org.folio.qm.converter.elements.ControlFieldItem.SPFM;
import static org.folio.qm.converter.elements.ControlFieldItem.SRCE;
import static org.folio.qm.converter.elements.ControlFieldItem.SRTP;
import static org.folio.qm.converter.elements.ControlFieldItem.TECH;
import static org.folio.qm.converter.elements.ControlFieldItem.TIME;
import static org.folio.qm.converter.elements.ControlFieldItem.TMAT;
import static org.folio.qm.converter.elements.ControlFieldItem.TRAR;
import static org.folio.qm.converter.elements.ControlFieldItem.VALUE;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum MaterialTypeConfiguration {
  BOOKS("Books", Arrays.asList(ILLS, AUDN, FORM, CONT_B, GPUB, CONF, FEST, INDX, LITF, BIOG)),
  FILES("Computer Files", Arrays.asList(AUDN, FORM, FILE, GPUB)),
  CONTINUING("Continuing Resources", Arrays.asList(FREQ, REGL, SRTP, ORIG, FORM, ENTW, CONT_C, GPUB, CONF, ALPH, SL)),
  MAPS("Maps", Arrays.asList(RELF, PROJ, CRTP, GPUB, FORM_MV, INDX, SPFM)),
  MIXED("Mixed Materials", Collections.singletonList(FORM)),
  SCORES("Scores", Arrays.asList(COMP, FMUS, PART, AUDN, FORM, ACCM, LTXT, TRAR)),
  SOUND("Sound Recordings", Arrays.asList(COMP, FMUS, PART, AUDN, FORM, ACCM, LTXT, TRAR)),
  VISUAL("Visual Materials", Arrays.asList(TIME, AUDN, GPUB, FORM_MV, TMAT, TECH)),
  UNKNOWN("Unknown Type", Collections.singletonList(VALUE));

  private final String name;
  private final List<ControlFieldItem> controlFieldItems;

  MaterialTypeConfiguration(String name, List<ControlFieldItem> controlFieldItems) {
    this.name = name;
    this.controlFieldItems = controlFieldItems;
  }

  public static List<ControlFieldItem> getCommonItems() {
    return Arrays.asList(ENTERED, DTST, DATE1, DATE2, CTRY, LANG, MREC, SRCE);
  }

  public static MaterialTypeConfiguration resolveContentType(String leader) {
    var typeByte = leader.charAt(TYPE_OF_RECORD_LEADER_POS);
    var blvlByte = leader.charAt(BLVL_LEADER_POS);
    return resolveContentType(typeByte, blvlByte);
  }

  public static MaterialTypeConfiguration resolveContentType(char typeByte, char blvlByte) {
    switch (typeByte) {
      case 'a':
        return Arrays.asList('b', 'i', 's').contains(blvlByte) ? CONTINUING : BOOKS;
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

  public String getName() {
    return name;
  }

  public List<ControlFieldItem> getControlFieldItems() {
    return controlFieldItems;
  }
}
