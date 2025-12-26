package org.folio.qm.convertion.elements;

import static org.folio.qm.convertion.elements.ControlFieldItem.ACCM;
import static org.folio.qm.convertion.elements.ControlFieldItem.ALPH;
import static org.folio.qm.convertion.elements.ControlFieldItem.AUDN;
import static org.folio.qm.convertion.elements.ControlFieldItem.BIOG;
import static org.folio.qm.convertion.elements.ControlFieldItem.COMP;
import static org.folio.qm.convertion.elements.ControlFieldItem.CONF;
import static org.folio.qm.convertion.elements.ControlFieldItem.CONT_B;
import static org.folio.qm.convertion.elements.ControlFieldItem.CONT_C;
import static org.folio.qm.convertion.elements.ControlFieldItem.CRTP;
import static org.folio.qm.convertion.elements.ControlFieldItem.CTRY;
import static org.folio.qm.convertion.elements.ControlFieldItem.DATE1;
import static org.folio.qm.convertion.elements.ControlFieldItem.DATE2;
import static org.folio.qm.convertion.elements.ControlFieldItem.DTST;
import static org.folio.qm.convertion.elements.ControlFieldItem.ENTERED;
import static org.folio.qm.convertion.elements.ControlFieldItem.ENTW;
import static org.folio.qm.convertion.elements.ControlFieldItem.FEST;
import static org.folio.qm.convertion.elements.ControlFieldItem.FILE;
import static org.folio.qm.convertion.elements.ControlFieldItem.FMUS;
import static org.folio.qm.convertion.elements.ControlFieldItem.FORM;
import static org.folio.qm.convertion.elements.ControlFieldItem.FORM_MV;
import static org.folio.qm.convertion.elements.ControlFieldItem.FREQ;
import static org.folio.qm.convertion.elements.ControlFieldItem.GPUB;
import static org.folio.qm.convertion.elements.ControlFieldItem.ILLS;
import static org.folio.qm.convertion.elements.ControlFieldItem.INDX;
import static org.folio.qm.convertion.elements.ControlFieldItem.LANG;
import static org.folio.qm.convertion.elements.ControlFieldItem.LITF;
import static org.folio.qm.convertion.elements.ControlFieldItem.LTXT;
import static org.folio.qm.convertion.elements.ControlFieldItem.MREC;
import static org.folio.qm.convertion.elements.ControlFieldItem.ORIG;
import static org.folio.qm.convertion.elements.ControlFieldItem.PART;
import static org.folio.qm.convertion.elements.ControlFieldItem.PROJ;
import static org.folio.qm.convertion.elements.ControlFieldItem.REGL;
import static org.folio.qm.convertion.elements.ControlFieldItem.RELF;
import static org.folio.qm.convertion.elements.ControlFieldItem.SL;
import static org.folio.qm.convertion.elements.ControlFieldItem.SPFM;
import static org.folio.qm.convertion.elements.ControlFieldItem.SRCE;
import static org.folio.qm.convertion.elements.ControlFieldItem.SRTP;
import static org.folio.qm.convertion.elements.ControlFieldItem.TECH;
import static org.folio.qm.convertion.elements.ControlFieldItem.TIME;
import static org.folio.qm.convertion.elements.ControlFieldItem.TMAT;
import static org.folio.qm.convertion.elements.ControlFieldItem.TRAR;
import static org.folio.qm.convertion.elements.ControlFieldItem.VALUE;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

public enum Tag008Configuration {

  BOOKS("Books", Arrays.asList(ILLS, AUDN, FORM, CONT_B, GPUB, CONF, FEST, INDX, LITF, BIOG)),
  FILES("Computer Files", Arrays.asList(AUDN, FORM, FILE, GPUB)),
  CONTINUING("Continuing Resources", Arrays.asList(FREQ, REGL, SRTP, ORIG, FORM, ENTW, CONT_C, GPUB, CONF, ALPH, SL)),
  MAPS("Maps", Arrays.asList(RELF, PROJ, CRTP, GPUB, FORM_MV, INDX, SPFM)),
  MIXED("Mixed Materials", Collections.singletonList(FORM)),
  SCORES("Scores", Arrays.asList(COMP, FMUS, PART, AUDN, FORM, ACCM, LTXT, TRAR)),
  SOUND("Sound Recordings", Arrays.asList(COMP, FMUS, PART, AUDN, FORM, ACCM, LTXT, TRAR)),
  VISUAL("Visual Materials", Arrays.asList(TIME, AUDN, GPUB, FORM_MV, TMAT, TECH)),
  UNKNOWN("Unknown Type", Collections.singletonList(VALUE));

  @Getter
  private final String name;
  private final List<ControlFieldItem> controlFieldItems;

  Tag008Configuration(String name, List<ControlFieldItem> controlFieldItems) {
    this.name = name;
    this.controlFieldItems = controlFieldItems;
  }

  public static List<ControlFieldItem> getCommonItems() {
    return Arrays.asList(ENTERED, DTST, DATE1, DATE2, CTRY, LANG, MREC, SRCE);
  }

  public static Tag008Configuration resolveContentType(char typeByte, char blvlByte) {
    return switch (typeByte) {
      case 'a' -> Arrays.asList('b', 'i', 's').contains(blvlByte) ? CONTINUING : BOOKS;
      case 't' -> BOOKS;
      case 's' -> CONTINUING;
      case 'm' -> FILES;
      case 'e', 'f' -> MAPS;
      case 'p' -> MIXED;
      case 'i', 'j' -> SOUND;
      case 'c', 'd' -> SCORES;
      case 'g', 'k', 'o', 'r' -> VISUAL;
      default -> UNKNOWN;
    };
  }

  public List<ControlFieldItem> getSpecificItems() {
    return controlFieldItems;
  }
}
