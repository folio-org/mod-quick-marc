package org.folio.converter;

import io.vertx.core.json.JsonObject;

import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

public class Field008RestoreFactory {
  private static final String oneSpace =     " ";
  private static final String twoSpaces =    "  ";
  private static final String threeSpaces =  "   ";
  private static final String fourSpaces =   "    ";
  private static final String fiveSpaces =   "     ";
  private static final String sixSpaces =    "      ";
  private static final String elevenSpaces = "           ";

  private static EnumMap<ContentType, Field008RestoreStrategy> map = new EnumMap(ContentType.class);

  private Field008RestoreFactory(){}

  static {
    map.put(ContentType.BOOKS, jsonObject ->
      restoreHeader(jsonObject)
        .concat(arrayToString("Ills", jsonObject))
        .concat(jsonObject.getString("Audn"))
        .concat(jsonObject.getString("Form"))
        .concat(arrayToString("Cont", jsonObject))
        .concat(jsonObject.getString("GPub"))
        .concat(jsonObject.getString("Conf"))
        .concat(jsonObject.getString("Fest"))
        .concat(jsonObject.getString("Indx"))
        .concat(oneSpace)
        .concat(jsonObject.getString("LitF"))
        .concat(jsonObject.getString("Biog"))
        .concat(restoreFooter(jsonObject)));

    map.put(ContentType.FILES, jsonObject ->
      restoreHeader(jsonObject)
        .concat(fourSpaces)
        .concat(jsonObject.getString("Audn"))
        .concat(jsonObject.getString("Form"))
        .concat(twoSpaces)
        .concat(jsonObject.getString("File"))
        .concat(oneSpace)
        .concat(jsonObject.getString("GPub"))
        .concat(sixSpaces)
        .concat(restoreFooter(jsonObject)));

    map.put(ContentType.CONTINUING, jsonObject ->
      restoreHeader(jsonObject)
        .concat(jsonObject.getString("Freq"))
        .concat(jsonObject.getString("Regl"))
        .concat(oneSpace)
        .concat(jsonObject.getString("SrTp"))
        .concat(jsonObject.getString("Orig"))
        .concat(jsonObject.getString("Form"))
        .concat(jsonObject.getString("EntW"))
        .concat(arrayToString("Cont", jsonObject))
        .concat(jsonObject.getString("GPub"))
        .concat(jsonObject.getString("Conf"))
        .concat(threeSpaces)
        .concat(jsonObject.getString("Alph"))
        .concat(jsonObject.getString("S/L"))
        .concat(restoreFooter(jsonObject)));

    map.put(ContentType.MAPS, jsonObject ->
      restoreHeader(jsonObject)
        .concat(arrayToString("Relf", jsonObject))
        .concat(jsonObject.getString("Proj"))
        .concat(oneSpace)
        .concat(jsonObject.getString("CrTp"))
        .concat(twoSpaces)
        .concat(jsonObject.getString("GPub"))
        .concat(jsonObject.getString("Form"))
        .concat(oneSpace)
        .concat(jsonObject.getString("Indx"))
        .concat(oneSpace)
        .concat(jsonObject.getString("SpFm"))
        .concat(restoreFooter(jsonObject)));

    map.put(ContentType.MIXED, jsonObject ->
      restoreHeader(jsonObject)
        .concat(fiveSpaces)
        .concat(jsonObject.getString("Form"))
        .concat(elevenSpaces)
        .concat(restoreFooter(jsonObject)));

    map.put(ContentType.SCORES, Field008RestoreFactory::restoreValueForScoresOrSound);

    map.put(ContentType.SOUND, Field008RestoreFactory::restoreValueForScoresOrSound);

    map.put(ContentType.VISUAL, jsonObject ->
      restoreHeader(jsonObject)
        .concat(arrayToString("Time", jsonObject))
        .concat(oneSpace)
        .concat(jsonObject.getString("Audn"))
        .concat(fiveSpaces)
        .concat(jsonObject.getString("GPub"))
        .concat(jsonObject.getString("Form"))
        .concat(threeSpaces)
        .concat(jsonObject.getString("TMat"))
        .concat(jsonObject.getString("Tech"))
        .concat(restoreFooter(jsonObject)));

    map.put(ContentType.UNKNOWN, jsonObject ->
      restoreHeader(jsonObject)
        .concat(jsonObject.getString("Value"))
        .concat(restoreFooter(jsonObject)));
  }

  private static String restoreHeader(JsonObject jsonObject) {
    return jsonObject.getString("Entered")
      .concat(jsonObject.getString("DtSt"))
      .concat(jsonObject.getString("Date1"))
      .concat(jsonObject.getString("Date2"))
      .concat(jsonObject.getString("Ctry"));
  }

  private static String restoreFooter(JsonObject jsonObject) {
    return jsonObject.getString("Lang")
      .concat(jsonObject.getString("MRec"))
      .concat(jsonObject.getString("Srce"));
  }

  private static String restoreValueForScoresOrSound(JsonObject jsonObject){
    return restoreHeader(jsonObject)
      .concat(jsonObject.getString("Comp"))
      .concat(jsonObject.getString("FMus"))
      .concat(jsonObject.getString("Part"))
      .concat(jsonObject.getString("Audn"))
      .concat(jsonObject.getString("Form"))
      .concat(arrayToString("AccM", jsonObject))
      .concat(arrayToString("LTxt", jsonObject))
      .concat(oneSpace)
      .concat(jsonObject.getString("TrAr"))
      .concat(oneSpace)
      .concat(restoreFooter(jsonObject));
  }

  private static String arrayToString(String key, JsonObject jsonObject) {
    return ((List<String>)jsonObject.getJsonArray(key).getList()).stream().collect(Collectors.joining());
  }

  public static Field008RestoreStrategy getStrategy(ContentType contentType){
    return map.get(contentType);
  }
}
