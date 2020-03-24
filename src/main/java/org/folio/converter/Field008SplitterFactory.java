package org.folio.converter;

import io.vertx.core.json.JsonObject;

import java.util.Arrays;
import java.util.EnumMap;


public class Field008SplitterFactory {
  private static EnumMap<ContentType, Field008SplitStrategy> map = new EnumMap(ContentType.class);

  private Field008SplitterFactory(){}

  static {
    map.put(ContentType.BOOKS, source -> {
      JsonObject jsonObject = splitHeader(source);
      String[] ills = {
        source.substring(18, 19),
        source.substring(19, 20),
        source.substring(20, 21),
        source.substring(21, 22)
      };
      jsonObject.put("Ills", Arrays.asList(ills));
      jsonObject.put("Audn", source.substring(22, 23));
      jsonObject.put("Form", source.substring(23, 24));
      String[] cont = {
        source.substring(24, 25),
        source.substring(25, 26),
        source.substring(26, 27),
        source.substring(27, 28)
      };
      jsonObject.put("Cont", Arrays.asList(cont));
      jsonObject.put("GPub", source.substring(28, 29));
      jsonObject.put("Conf", source.substring(29, 30));
      jsonObject.put("Fest", source.substring(30, 31));
      jsonObject.put("Indx", source.substring(31, 32));
      jsonObject.put("LitF", source.substring(33, 34));
      jsonObject.put("Biog", source.substring(34, 35));
      jsonObject.mergeIn(splitFooter(source));
      return jsonObject;
    });

    map.put(ContentType.FILES, source -> {
      JsonObject jsonObject = splitHeader(source);
      jsonObject.put("Audn", source.substring(22, 23));
      jsonObject.put("Form", source.substring(23, 24));
      jsonObject.put("File", source.substring(26, 27));
      jsonObject.put("GPub", source.substring(28, 29));
      jsonObject.mergeIn(splitFooter(source));
      return jsonObject;
    });

    map.put(ContentType.CONTINUING, source -> {
      JsonObject jsonObject = splitHeader(source);
      jsonObject.put("Freq", source.substring(18, 19));
      jsonObject.put("Regl", source.substring(19, 20));
      jsonObject.put("SrTp", source.substring(21, 22));
      jsonObject.put("Orig", source.substring(22, 23));
      jsonObject.put("Form", source.substring(23, 24));
      jsonObject.put("EntW", source.substring(24, 25));
      String[] cont = {
        source.substring(25, 26),
        source.substring(26, 27),
        source.substring(27, 28),
      };
      jsonObject.put("Cont", Arrays.asList(cont));
      jsonObject.put("GPub", source.substring(28, 29));
      jsonObject.put("Conf", source.substring(29, 30));
      jsonObject.put("Alph", source.substring(33, 34));
      jsonObject.put("S/L", source.substring(34, 35));
      jsonObject.mergeIn(splitFooter(source));
      return jsonObject;
    });

    map.put(ContentType.MAPS, source -> {
      JsonObject jsonObject = splitHeader(source);
      String[] relf = {
        source.substring(18, 19),
        source.substring(19, 20),
        source.substring(20, 21),
        source.substring(21, 22)
      };
      jsonObject.put("Relf", Arrays.asList(relf));
      jsonObject.put("Proj", source.substring(22, 24));
      jsonObject.put("CrTp", source.substring(25, 26));
      jsonObject.put("GPub", source.substring(28, 29));
      jsonObject.put("Form", source.substring(29, 30));
      jsonObject.put("Indx", source.substring(31, 32));
      jsonObject.put("SpFm", source.substring(33, 35));
      jsonObject.mergeIn(splitFooter(source));
      return jsonObject;
    });

    map.put(ContentType.MIXED, source -> {
      JsonObject jsonObject = splitHeader(source);
      jsonObject.put("Form", source.substring(23, 24));
      jsonObject.mergeIn(splitFooter(source));
      return jsonObject;
    });

    map.put(ContentType.SCORES, Field008SplitterFactory::splitField008ForScoresOrSound);

    map.put(ContentType.SOUND, Field008SplitterFactory::splitField008ForScoresOrSound);

    map.put(ContentType.VISUAL, source -> {
      JsonObject jsonObject = splitHeader(source);
      String[] time = {
        source.substring(18, 19),
        source.substring(19, 20),
        source.substring(20, 21)
      };
      jsonObject.put("Time", Arrays.asList(time));
      jsonObject.put("Audn", source.substring(22, 23));
      jsonObject.put("GPub", source.substring(28, 29));
      jsonObject.put("Form", source.substring(29, 30));
      jsonObject.put("TMat", source.substring(33, 34));
      jsonObject.put("Tech", source.substring(34, 35));
      jsonObject.mergeIn(splitFooter(source));
      return jsonObject;
    });

    map.put(ContentType.UNKNOWN, source -> {
      JsonObject jsonObject = splitHeader(source);
      jsonObject.put("Value", source.substring(18, 35));
      jsonObject.mergeIn(splitFooter(source));
      return jsonObject;
    });
  }

  private static JsonObject splitHeader(String source) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("Entered", source.substring(0, 6));
    jsonObject.put("DtSt", source.substring(6, 7));
    jsonObject.put("Date1", source.substring(7, 11));
    jsonObject.put("Date2", source.substring(11, 15));
    jsonObject.put("Ctry", source.substring(15, 18));
    return jsonObject;
  }

  private static JsonObject splitFooter(String source) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("Lang", source.substring(35, 38));
    jsonObject.put("MRec", source.substring(38, 39));
    jsonObject.put("Srce", source.substring(39, 40));
    return jsonObject;
  }

  private static JsonObject splitField008ForScoresOrSound(String source) {
    JsonObject jsonObject = splitHeader(source);
    jsonObject.put("Comp", source.substring(18, 20));
    jsonObject.put("FMus", source.substring(20, 21));
    jsonObject.put("Part", source.substring(21, 22));
    jsonObject.put("Audn", source.substring(22, 23));
    jsonObject.put("Form", source.substring(23, 24));
    String[] accm = {
      source.substring(24, 25),
      source.substring(25, 26),
      source.substring(26, 27),
      source.substring(27, 28),
      source.substring(28, 29),
      source.substring(29, 30)
    };
    jsonObject.put("AccM", Arrays.asList(accm));
    String[] ltxt = {
      source.substring(30, 31),
      source.substring(31, 32)
    };
    jsonObject.put("LTxt", Arrays.asList(ltxt));
    jsonObject.put("TrAr", source.substring(33, 34));
    jsonObject.mergeIn(splitFooter(source));
    return jsonObject;
  }

  public static Field008SplitStrategy getStrategy(ContentType contentType){
    return map.get(contentType);
  }
}
