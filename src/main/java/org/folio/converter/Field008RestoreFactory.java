package org.folio.converter;

import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Field008RestoreFactory {
  private static String ONE_SPACE =     " ";
  private static String TWO_SPACES =    "  ";
  private static String THREE_SPACES =  "   ";
  private static String FOUR_SPACES =   "    ";
  private static String FIVE_SPACES =   "     ";
  private static String SIX_SPACES =    "      ";
  private static String ELEVEN_SPACES = "           ";

  private static Map<ContentType, Field008RestoreStrategy> map = new HashMap<>();
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
        .concat(ONE_SPACE)
        .concat(jsonObject.getString("LitF"))
        .concat(jsonObject.getString("Biog"))
        .concat(restoreFooter(jsonObject)));

    map.put(ContentType.FILES, jsonObject ->
      restoreHeader(jsonObject)
        .concat(FOUR_SPACES)
        .concat(jsonObject.getString("Audn"))
        .concat(jsonObject.getString("Form"))
        .concat(TWO_SPACES)
        .concat(jsonObject.getString("File"))
        .concat(ONE_SPACE)
        .concat(jsonObject.getString("GPub"))
        .concat(SIX_SPACES)
        .concat(restoreFooter(jsonObject)));

    map.put(ContentType.CONTINUING, jsonObject ->
      restoreHeader(jsonObject)
        .concat(jsonObject.getString("Freq"))
        .concat(jsonObject.getString("Regl"))
        .concat(ONE_SPACE)
        .concat(jsonObject.getString("SrTp"))
        .concat(jsonObject.getString("Orig"))
        .concat(jsonObject.getString("Form"))
        .concat(jsonObject.getString("EntW"))
        .concat(arrayToString("Cont", jsonObject))
        .concat(jsonObject.getString("GPub"))
        .concat(jsonObject.getString("Conf"))
        .concat(THREE_SPACES)
        .concat(jsonObject.getString("Alph"))
        .concat(jsonObject.getString("S/L"))
        .concat(restoreFooter(jsonObject)));

    map.put(ContentType.MAPS, jsonObject ->
      restoreHeader(jsonObject)
        .concat(arrayToString("Relf", jsonObject))
        .concat(jsonObject.getString("Proj"))
        .concat(ONE_SPACE)
        .concat(jsonObject.getString("CrTp"))
        .concat(TWO_SPACES)
        .concat(jsonObject.getString("GPub"))
        .concat(jsonObject.getString("Form"))
        .concat(ONE_SPACE)
        .concat(jsonObject.getString("Indx"))
        .concat(ONE_SPACE)
        .concat(jsonObject.getString("SpFm"))
        .concat(restoreFooter(jsonObject)));

    map.put(ContentType.MIXED, jsonObject ->
      restoreHeader(jsonObject)
        .concat(FIVE_SPACES)
        .concat(jsonObject.getString("Form"))
        .concat(ELEVEN_SPACES)
        .concat(restoreFooter(jsonObject)));

    map.put(ContentType.SCORES, jsonObject ->
      restoreHeader(jsonObject)
        .concat(jsonObject.getString("Comp"))
        .concat(jsonObject.getString("FMus"))
        .concat(jsonObject.getString("Part"))
        .concat(jsonObject.getString("Audn"))
        .concat(jsonObject.getString("Form"))
        .concat(arrayToString("AccM", jsonObject))
        .concat(arrayToString("LTxt", jsonObject))
        .concat(ONE_SPACE)
        .concat(jsonObject.getString("TrAr"))
        .concat(ONE_SPACE)
        .concat(restoreFooter(jsonObject)));

    map.put(ContentType.SOUND, jsonObject ->
      restoreHeader(jsonObject)
        .concat(jsonObject.getString("Comp"))
        .concat(jsonObject.getString("FMus"))
        .concat(jsonObject.getString("Part"))
        .concat(jsonObject.getString("Audn"))
        .concat(jsonObject.getString("Form"))
        .concat(arrayToString("AccM", jsonObject))
        .concat(arrayToString("LTxt", jsonObject))
        .concat(ONE_SPACE)
        .concat(jsonObject.getString("TrAr"))
        .concat(ONE_SPACE)
        .concat(restoreFooter(jsonObject)));

    map.put(ContentType.VISUAL, jsonObject ->
      restoreHeader(jsonObject)
        .concat(arrayToString("Time", jsonObject))
        .concat(ONE_SPACE)
        .concat(jsonObject.getString("Audn"))
        .concat(FIVE_SPACES)
        .concat(jsonObject.getString("GPub"))
        .concat(jsonObject.getString("Form"))
        .concat(THREE_SPACES)
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

  private static String arrayToString(String key, JsonObject jsonObject) {
    return ((List<String>)jsonObject.getJsonArray(key).getList()).stream().collect(Collectors.joining());
  }

  public static Field008RestoreStrategy getStrategy(ContentType contentType){
    return map.get(contentType);
  }
}
