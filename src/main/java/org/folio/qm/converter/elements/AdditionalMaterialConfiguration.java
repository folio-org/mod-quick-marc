package org.folio.qm.converter.elements;

import static org.folio.qm.converter.elements.MaterialTypeConfiguration.BOOKS;
import static org.folio.qm.converter.elements.MaterialTypeConfiguration.CONTINUING;
import static org.folio.qm.converter.elements.MaterialTypeConfiguration.FILES;
import static org.folio.qm.converter.elements.MaterialTypeConfiguration.MAPS;
import static org.folio.qm.converter.elements.MaterialTypeConfiguration.MIXED;
import static org.folio.qm.converter.elements.MaterialTypeConfiguration.SOUND;
import static org.folio.qm.converter.elements.MaterialTypeConfiguration.UNKNOWN;
import static org.folio.qm.converter.elements.MaterialTypeConfiguration.VISUAL;

import java.util.ArrayList;
import java.util.List;

public enum AdditionalMaterialConfiguration {

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
  UNKNOWN_MATERIAL("Unknown Type", null, UNKNOWN);

  private final String name;
  private final Character code;
  private final MaterialTypeConfiguration materialTypeConfiguration;

  AdditionalMaterialConfiguration(String name, Character code, MaterialTypeConfiguration materialTypeConfiguration) {
    this.name = name;
    this.code = code;
    this.materialTypeConfiguration = materialTypeConfiguration;
  }

  public static AdditionalMaterialConfiguration resolveByCode(Character code) {
    for (AdditionalMaterialConfiguration value : values()) {
      if (code.equals(value.getCode())) {
        return value;
      }
    }
    return UNKNOWN_MATERIAL;
  }

  public String getName() {
    return name;
  }

  public Character getCode() {
    return code;
  }

  public List<ControlFieldItem> getControlFieldItems() {
    var controlFieldItems = new ArrayList<>(materialTypeConfiguration.getControlFieldItems());
    controlFieldItems.add(0, ControlFieldItem.TYPE);
    return controlFieldItems;
  }

  public MaterialTypeConfiguration getMaterialTypeConfiguration() {
    return materialTypeConfiguration;
  }
}
