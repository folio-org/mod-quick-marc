package org.folio.support.testdata;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public enum Tag007FieldTestData {

  ELECTRONIC_RESOURCE("ca bcdefghijkl", getElectronicResourceContent()),
  ELECTRONIC_RESOURCE_WITH_EMPTY_ITEMS("ca b defgh jkl", getElectronicResourceWithEmptyItemsContent()),
  GLOBE("da bcd", getGlobeContent()),
  KIT("oa", getKitContent()),
  MAP("aa bcdef", getMapContent()),
  MICROFORM("ha bcdefghijk", getMicroformContent()),
  MOTION_PICTURE("ma bcdefghijklmnopqrstu", getMotionPictureContent()),
  NON_PROJECTED_GRAPHIC("ka bcd", getNonProjectedGraphicContent()),
  NOTATED_MUSIC("qa", getNotatedMusicContent()),
  PROJECTED_GRAPHIC("ga bcdefg", getProjectedGraphicContent()),
  REMOTE_SENSING_IMAGE("ra bcdefghi", getRemoteSensingImageContent()),
  SOUND_RECORDING("sa bcdefghijkl", getSoundRecordingContent()),
  TACTILE_MATERIAL("fa bcdefgh", getTactileMaterialContent()),
  TEXT("ta", getTextContent()),
  UNKNOWN("pabcdefghijklmnopqrstuvwxyz", getUnknownContent()),
  UNSPECIFIED("za", getUnspecifiedContent()),
  VIDEORECORDING("va bcdefg", getVideorecordingContent());

  private final String dtoData;
  private final Map<String, Object> qmContent;

  Tag007FieldTestData(String dtoData, Map<String, Object> qmContent) {
    this.dtoData = dtoData;
    this.qmContent = qmContent;
  }

  private static Map<String, Object> getElectronicResourceContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("$categoryName", "Electronic resource");
    content.put("Category", "c");
    content.put("SMD", "a");
    content.put("Color", "b");
    content.put("Dimensions", "c");
    content.put("Sound", "d");
    content.put("Image bit depth", "efg");
    content.put("File formats", "h");
    content.put("Quality assurance target(s)", "i");
    content.put("Antecedent/ Source", "j");
    content.put("Level of compression", "k");
    content.put("Reformatting quality", "l");
    return content;
  }

  private static Map<String, Object> getElectronicResourceWithEmptyItemsContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("$categoryName", "Electronic resource");
    content.put("Category", "c");
    content.put("SMD", "a");
    content.put("Color", "b");
    content.put("Dimensions", "\\");
    content.put("Sound", "d");
    content.put("Image bit depth", "efg");
    content.put("File formats", "h");
    content.put("Quality assurance target(s)", "\\");
    content.put("Antecedent/ Source", "j");
    content.put("Level of compression", "k");
    content.put("Reformatting quality", "l");
    return content;
  }

  private static Map<String, Object> getGlobeContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("$categoryName", "Globe");
    content.put("Category", "d");
    content.put("SMD", "a");
    content.put("Color", "b");
    content.put("Physical medium", "c");
    content.put("Type of reproduction", "d");
    return content;
  }

  private static Map<String, Object> getKitContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("$categoryName", "Kit");
    content.put("Category", "o");
    content.put("SMD", "a");
    return content;
  }

  private static Map<String, Object> getMapContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("$categoryName", "Map");
    content.put("Category", "a");
    content.put("SMD", "a");
    content.put("Color", "b");
    content.put("Physical medium", "c");
    content.put("Type of reproduction", "d");
    content.put("Production/reproduction details", "e");
    content.put("Positive/negative aspect", "f");
    return content;
  }

  private static Map<String, Object> getMicroformContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("$categoryName", "Microform");
    content.put("Category", "h");
    content.put("SMD", "a");
    content.put("Positive/negative aspect", "b");
    content.put("Dimensions", "c");
    content.put("Reduction ratio range/Reduction ratio", "defg");
    content.put("Color", "h");
    content.put("Emulsion on film", "i");
    content.put("Generation", "j");
    content.put("Base of film", "k");
    return content;
  }

  private static Map<String, Object> getMotionPictureContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("$categoryName", "Motion picture");
    content.put("Category", "m");
    content.put("SMD", "a");
    content.put("Color", "b");
    content.put("Motion picture presentation format", "c");
    content.put("Sound on medium or separate", "d");
    content.put("Medium for sound", "e");
    content.put("Dimensions", "f");
    content.put("Configuration of playback channels", "g");
    content.put("Production elements", "h");
    content.put("Positive/negative aspect", "i");
    content.put("Generation", "j");
    content.put("Base of film", "k");
    content.put("Refined categories of color", "l");
    content.put("Kind of color stock or print", "m");
    content.put("Deterioration stage", "n");
    content.put("Completeness", "o");
    content.put("Film inspection date", "pqrstu");
    return content;
  }

  private static Map<String, Object> getNonProjectedGraphicContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("$categoryName", "Nonprojected graphic");
    content.put("Category", "k");
    content.put("SMD", "a");
    content.put("Color", "b");
    content.put("Primary support material", "c");
    content.put("Secondary support material", "d");
    return content;
  }

  private static Map<String, Object> getNotatedMusicContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("$categoryName", "Notated music");
    content.put("Category", "q");
    content.put("SMD", "a");
    return content;
  }

  private static Map<String, Object> getProjectedGraphicContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("$categoryName", "Projected graphic");
    content.put("Category", "g");
    content.put("SMD", "a");
    content.put("Color", "b");
    content.put("Base of emulsion", "c");
    content.put("Sound on medium or separate", "d");
    content.put("Medium for sound", "e");
    content.put("Dimensions", "f");
    content.put("Secondary support material", "g");
    return content;
  }

  private static Map<String, Object> getRemoteSensingImageContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("$categoryName", "Remote-sensing image");
    content.put("Category", "r");
    content.put("SMD", "a");
    content.put("Altitude of sensor", "b");
    content.put("Attitude of sensor", "c");
    content.put("Cloud cover", "d");
    content.put("Platform construction type", "e");
    content.put("Platform use category", "f");
    content.put("Sensor type", "g");
    content.put("Data type", "hi");
    return content;
  }

  private static Map<String, Object> getSoundRecordingContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("$categoryName", "Sound recording");
    content.put("Category", "s");
    content.put("SMD", "a");
    content.put("Speed", "b");
    content.put("Configuration of playback channels", "c");
    content.put("Groove width/ groove pitch", "d");
    content.put("Dimensions", "e");
    content.put("Tape width", "f");
    content.put("Tape configuration", "g");
    content.put("Kind of disc, cylinder, or tape", "h");
    content.put("Kind of material", "i");
    content.put("Kind of cutting", "j");
    content.put("Special playback characteristics", "k");
    content.put("Capture and storage technique", "l");
    return content;
  }

  private static Map<String, Object> getTactileMaterialContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("$categoryName", "Tactile material");
    content.put("Category", "f");
    content.put("SMD", "a");
    content.put("Class of braille writing", "bc");
    content.put("Level of contraction", "d");
    content.put("Braille music format", "efg");
    content.put("Special physical characteristics", "h");
    return content;
  }

  private static Map<String, Object> getTextContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("$categoryName", "Text");
    content.put("Category", "t");
    content.put("SMD", "a");
    return content;
  }

  private static Map<String, Object> getUnknownContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("$categoryName", "Unknown");
    content.put("Category", "p");
    content.put("Value", "pabcdefghijklmnopqrstuvwxyz");
    return content;
  }

  private static Map<String, Object> getUnspecifiedContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("$categoryName", "Unspecified");
    content.put("Category", "z");
    content.put("SMD", "a");
    return content;
  }

  private static Map<String, Object> getVideorecordingContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("$categoryName", "Videorecording");
    content.put("Category", "v");
    content.put("SMD", "a");
    content.put("Color", "b");
    content.put("Videorecording format", "c");
    content.put("Sound on medium or separate", "d");
    content.put("Medium for sound", "e");
    content.put("Dimensions", "f");
    content.put("Configuration of playback channels", "g");
    return content;
  }
}
