package org.folio.qm.converter.elements;

import static org.folio.qm.converter.elements.Tag008Configuration.BOOKS;
import static org.folio.qm.converter.elements.Tag008Configuration.CONTINUING;
import static org.folio.qm.converter.elements.Tag008Configuration.FILES;
import static org.folio.qm.converter.elements.Tag008Configuration.MAPS;
import static org.folio.qm.converter.elements.Tag008Configuration.MIXED;
import static org.folio.qm.converter.elements.Tag008Configuration.SOUND;
import static org.folio.qm.converter.elements.Tag008Configuration.UNKNOWN;
import static org.folio.qm.converter.elements.Tag008Configuration.VISUAL;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public enum Tag006Configuration {

  CARTOGRAPHIC_MATERIAL("Cartographic material", 'e', MAPS),
  FILE_ELECTRONIC_RESOURCE("Computer file/Electronic resource", 'm', FILES),
  KIT("Kit", 'o', VISUAL),
  LANGUAGE_MATERIAL("Language material", 'a', BOOKS),
  MANUSCRIPT_CARTOGRAPHIC_MATERIAL("Manuscript cartographic material", 'f', MAPS),
  MANUSCRIPT_LANGUAGE_MATERIAL("Manuscript language material", 't', BOOKS),
  MANUSCRIPT_NOTATED_MUSIC("Manuscript notated music", 'd', SOUND),
  MIXED_MATERIAL("Mixed material", 'p', MIXED),
  MUSICAL_SOUND_RECORDING("Musical sound recording", 'j', SOUND),
  NONMUSICAL_SOUND_RECORDING("Nonmusical sound recording", 'i', SOUND),
  NOTATED_MUSIC("Notated music", 'c', SOUND),
  PROJECTED_MEDIUM("Projected medium", 'g', VISUAL),
  SERIAL_INTEGRATING_RESOURCE("Serial/Integrating resource", 's', CONTINUING),
  THREE_DIMENSIONAL("Three-dimensional artifact or naturally occurring object", 'r', VISUAL),
  TWO_DIMENSIONAL_GRAPHIC("Two-dimensional nonprojectable graphic", 'k', VISUAL),
  UNKNOWN_MATERIAL("Unknown Type", '-', UNKNOWN);

  @Getter
  private final String name;
  @Getter
  private final char code;
  private final Tag008Configuration tag008Configuration;

  Tag006Configuration(String name, char code, Tag008Configuration tag008Configuration) {
    this.name = name;
    this.code = code;
    this.tag008Configuration = tag008Configuration;
  }

  public static Tag006Configuration resolveByCode(char code) {
    for (Tag006Configuration value : values()) {
      if (code == value.getCode()) {
        return value;
      }
    }
    return UNKNOWN_MATERIAL;
  }

  public List<ControlFieldItem> getControlFieldItems() {
    var controlFieldItems = new ArrayList<>(tag008Configuration.getSpecificItems());
    controlFieldItems.addFirst(ControlFieldItem.TYPE);
    return controlFieldItems;
  }
}
