package org.folio.qm.support.utils.testentities;

public enum PhysicalDescriptionsTestEntities {

  ELECTRONIC_RESOURCE,
  ELECTRONIC_RESOURCE_MISSING_ITEMS,
  GLOBE,
  KIT,
  MAP,
  MICROFORM,
  MOTION_PICTURE,
  NONPROJECTED_GRAPHIC,
  NOTATED_MUSIC,
  PROJECTED_GRAPHIC,
  REMOTE_SENSING_IMAGE,
  SOUND_RECORDING,
  TACTILE_MATERIAL,
  TEXT,
  UNKNOWN,
  UNSPECIFIED,
  VIDEORECORDING;

  private static final String PARSED_RECORDS_DIR = "mockdata/parsed-records/descriptions/";
  private static final String QUICK_MARC_JSON_DIR = "mockdata/quick-marc-json/descriptions/";

  public String getParsedRecordPath() {
    return PARSED_RECORDS_DIR + this.name().toLowerCase() + ".json";
  }

  public String getQuickMarcJsonPath() {
    return QUICK_MARC_JSON_DIR + this.name().toLowerCase() + ".json";
  }
}
