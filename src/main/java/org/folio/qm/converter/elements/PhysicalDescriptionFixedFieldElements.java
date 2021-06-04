package org.folio.qm.converter.elements;

import static java.util.Arrays.asList;

import static org.folio.qm.converter.elements.ControlFieldItem.ALTITUDE;
import static org.folio.qm.converter.elements.ControlFieldItem.ANTECEDENT;
import static org.folio.qm.converter.elements.ControlFieldItem.ASPECT_MAP;
import static org.folio.qm.converter.elements.ControlFieldItem.ASPECT_MICROFORM;
import static org.folio.qm.converter.elements.ControlFieldItem.ASPECT_MOTION;
import static org.folio.qm.converter.elements.ControlFieldItem.ATTITUDE;
import static org.folio.qm.converter.elements.ControlFieldItem.BASE_OF_EMULSION;
import static org.folio.qm.converter.elements.ControlFieldItem.BASE_OF_FILM;
import static org.folio.qm.converter.elements.ControlFieldItem.BRAILLE_MUSIC_FORMAT;
import static org.folio.qm.converter.elements.ControlFieldItem.CAPTURE_TECHNIQUE;
import static org.folio.qm.converter.elements.ControlFieldItem.CATEGORY;
import static org.folio.qm.converter.elements.ControlFieldItem.CLASS_OF_BRAILLE;
import static org.folio.qm.converter.elements.ControlFieldItem.CLOUD_COVER;
import static org.folio.qm.converter.elements.ControlFieldItem.COLOR;
import static org.folio.qm.converter.elements.ControlFieldItem.COLOR_MICROFORM;
import static org.folio.qm.converter.elements.ControlFieldItem.COMPLETENESS;
import static org.folio.qm.converter.elements.ControlFieldItem.CONFIGURATION_OF_CHANNELS;
import static org.folio.qm.converter.elements.ControlFieldItem.CONFIGURATION_OF_CHANNELS_SOUND;
import static org.folio.qm.converter.elements.ControlFieldItem.DATA_TYPE;
import static org.folio.qm.converter.elements.ControlFieldItem.DETERIORATION_STAGE;
import static org.folio.qm.converter.elements.ControlFieldItem.DIMENSIONS;
import static org.folio.qm.converter.elements.ControlFieldItem.DIMENSIONS_MOTION;
import static org.folio.qm.converter.elements.ControlFieldItem.DIMENSIONS_SOUND;
import static org.folio.qm.converter.elements.ControlFieldItem.EMULSION_ON_FILM;
import static org.folio.qm.converter.elements.ControlFieldItem.FILE_FORMATS;
import static org.folio.qm.converter.elements.ControlFieldItem.FILM_INSPECTION_DATE;
import static org.folio.qm.converter.elements.ControlFieldItem.GENERATION;
import static org.folio.qm.converter.elements.ControlFieldItem.GROOVE;
import static org.folio.qm.converter.elements.ControlFieldItem.IMAGE_BIT_DEPTH;
import static org.folio.qm.converter.elements.ControlFieldItem.KIND_OF_COLOR;
import static org.folio.qm.converter.elements.ControlFieldItem.KIND_OF_CUTTING;
import static org.folio.qm.converter.elements.ControlFieldItem.KIND_OF_DISC;
import static org.folio.qm.converter.elements.ControlFieldItem.KIND_OF_MATERIAL;
import static org.folio.qm.converter.elements.ControlFieldItem.LEVEL_OF_COMPRESSION;
import static org.folio.qm.converter.elements.ControlFieldItem.LEVEL_OF_CONTRACTION;
import static org.folio.qm.converter.elements.ControlFieldItem.MEDIUM_FOR_SOUND;
import static org.folio.qm.converter.elements.ControlFieldItem.MOTION_PICTURE_PRESENTATION_FORMAT;
import static org.folio.qm.converter.elements.ControlFieldItem.PHYSICAL_MEDIUM;
import static org.folio.qm.converter.elements.ControlFieldItem.PLATFORM_CONSTRUCTION_TYPE;
import static org.folio.qm.converter.elements.ControlFieldItem.PLATFORM_USE_CATEGORY;
import static org.folio.qm.converter.elements.ControlFieldItem.PRIMARY_SUPPORT;
import static org.folio.qm.converter.elements.ControlFieldItem.PRODUCTION_DETAILS;
import static org.folio.qm.converter.elements.ControlFieldItem.PRODUCTION_ELEMENTS;
import static org.folio.qm.converter.elements.ControlFieldItem.QUALITY_ASSURANCE_TARGET;
import static org.folio.qm.converter.elements.ControlFieldItem.REDUCTION_RATIO;
import static org.folio.qm.converter.elements.ControlFieldItem.REFINED_CATEGORIES;
import static org.folio.qm.converter.elements.ControlFieldItem.REFORMATTING_QUALITY;
import static org.folio.qm.converter.elements.ControlFieldItem.SECONDARY_SUPPORT;
import static org.folio.qm.converter.elements.ControlFieldItem.SECONDARY_SUPPORT_PROJECTED;
import static org.folio.qm.converter.elements.ControlFieldItem.SENSOR_TYPE;
import static org.folio.qm.converter.elements.ControlFieldItem.SMD;
import static org.folio.qm.converter.elements.ControlFieldItem.SOUND;
import static org.folio.qm.converter.elements.ControlFieldItem.SOUND_ON_MEDIUM;
import static org.folio.qm.converter.elements.ControlFieldItem.SPECIAL_PHYSICAL_CHARACTERISTICS;
import static org.folio.qm.converter.elements.ControlFieldItem.SPECIAL_PLAYBACK_CHARACTERISTICS;
import static org.folio.qm.converter.elements.ControlFieldItem.SPEED;
import static org.folio.qm.converter.elements.ControlFieldItem.TAPE_CONFIGURATION;
import static org.folio.qm.converter.elements.ControlFieldItem.TAPE_WIDTH;
import static org.folio.qm.converter.elements.ControlFieldItem.TYPE_OF_REPRODUCTION;
import static org.folio.qm.converter.elements.ControlFieldItem.VAL;
import static org.folio.qm.converter.elements.ControlFieldItem.VIDEORECORDING_FORMAT;

import java.util.List;

public enum PhysicalDescriptionFixedFieldElements {

  ELECTRONIC_RESOURCE("Electronic resource", 'c', 14, asList(CATEGORY, SMD, COLOR, DIMENSIONS, SOUND, IMAGE_BIT_DEPTH, FILE_FORMATS, QUALITY_ASSURANCE_TARGET, ANTECEDENT, LEVEL_OF_COMPRESSION, REFORMATTING_QUALITY)),
  GLOBE("Globe", 'd', 6, asList(CATEGORY, SMD, COLOR, PHYSICAL_MEDIUM, TYPE_OF_REPRODUCTION)),
  KIT("Kit", 'o', 2, asList(CATEGORY, SMD)),
  MAP("Map", 'a', 8, asList(CATEGORY, SMD, COLOR, PHYSICAL_MEDIUM, TYPE_OF_REPRODUCTION, PRODUCTION_DETAILS, ASPECT_MAP)),
  MICROFORM("Microform", 'h', 13, asList(CATEGORY, SMD, ASPECT_MICROFORM, DIMENSIONS, REDUCTION_RATIO, COLOR_MICROFORM, EMULSION_ON_FILM, GENERATION, BASE_OF_FILM)),
  MOTION_PICTURE("Motion picture", 'm', 23, asList(CATEGORY, SMD, COLOR, MOTION_PICTURE_PRESENTATION_FORMAT, SOUND_ON_MEDIUM, MEDIUM_FOR_SOUND, DIMENSIONS_MOTION, CONFIGURATION_OF_CHANNELS, PRODUCTION_ELEMENTS, ASPECT_MOTION, GENERATION, BASE_OF_FILM, REFINED_CATEGORIES, KIND_OF_COLOR, DETERIORATION_STAGE, COMPLETENESS, FILM_INSPECTION_DATE)),
  NONPROJECTED_GRAPHIC("Nonprojected graphic", 'k', 6, asList(CATEGORY, SMD, COLOR, PRIMARY_SUPPORT, SECONDARY_SUPPORT)),
  NOTATED_MUSIC("Notated music", 'q', 2, asList(CATEGORY, SMD)),
  PROJECTED_GRAPHIC("Projected graphic", 'g', 9, asList(CATEGORY, SMD, COLOR, BASE_OF_EMULSION, SOUND_ON_MEDIUM, MEDIUM_FOR_SOUND, DIMENSIONS_MOTION, SECONDARY_SUPPORT_PROJECTED)),
  REMOTE_SENSING_IMAGE("Remote-sensing image", 'r', 11, asList(CATEGORY, SMD, ALTITUDE, ATTITUDE, CLOUD_COVER, PLATFORM_CONSTRUCTION_TYPE, PLATFORM_USE_CATEGORY, SENSOR_TYPE, DATA_TYPE)),
  SOUND_RECORDING("Sound recording", 's', 14, asList(CATEGORY, SMD, SPEED, CONFIGURATION_OF_CHANNELS_SOUND, GROOVE, DIMENSIONS_SOUND, TAPE_WIDTH, TAPE_CONFIGURATION, KIND_OF_DISC, KIND_OF_MATERIAL, KIND_OF_CUTTING, SPECIAL_PLAYBACK_CHARACTERISTICS, CAPTURE_TECHNIQUE)),
  TACTILE_MATERIAL("Tactile material", 'f', 10, asList(CATEGORY, SMD, CLASS_OF_BRAILLE, LEVEL_OF_CONTRACTION, BRAILLE_MUSIC_FORMAT, SPECIAL_PHYSICAL_CHARACTERISTICS)),
  TEXT("Text", 't', 2, asList(CATEGORY, SMD)),
  UNSPECIFIED("Unspecified", 'z', 2, asList(CATEGORY, SMD)),
  VIDEORECORDING("Videorecording", 'v', 9, asList(CATEGORY, SMD, COLOR, VIDEORECORDING_FORMAT, SOUND_ON_MEDIUM, MEDIUM_FOR_SOUND, DIMENSIONS_MOTION, CONFIGURATION_OF_CHANNELS)),
  UNKNOWN("Unknown", 'b', 1, asList(CATEGORY, VAL));

  private final String name;
  private final char code;
  private final int length;
  private final List<ControlFieldItem> controlFieldItems;

  PhysicalDescriptionFixedFieldElements(String name, char code, int length, List<ControlFieldItem> controlFieldItems) {
    this.name = name;
    this.code = code;
    this.length = length;
    this.controlFieldItems = controlFieldItems;
  }

  public static PhysicalDescriptionFixedFieldElements resolveByCode(char code) {
    for (PhysicalDescriptionFixedFieldElements physicalDescriptionFixedFieldElements : values()) {
      if (physicalDescriptionFixedFieldElements.code == code) {
        return physicalDescriptionFixedFieldElements;
      }
    }
    return UNKNOWN;
  }

  public String getName() {
    return name;
  }

  public int getLength() {
    return length;
  }

  public List<ControlFieldItem> getControlFieldItems() {
    return controlFieldItems;
  }
}
