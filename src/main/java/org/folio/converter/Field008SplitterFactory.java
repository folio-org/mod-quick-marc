package org.folio.converter;

import static org.folio.converter.StringConstants.ACCM;
import static org.folio.converter.StringConstants.ALPH;
import static org.folio.converter.StringConstants.AUDN;
import static org.folio.converter.StringConstants.BIOG;
import static org.folio.converter.StringConstants.COMP;
import static org.folio.converter.StringConstants.CONF;
import static org.folio.converter.StringConstants.CONT;
import static org.folio.converter.StringConstants.CRTP;
import static org.folio.converter.StringConstants.CTRY;
import static org.folio.converter.StringConstants.DATE1;
import static org.folio.converter.StringConstants.DATE2;
import static org.folio.converter.StringConstants.DTST;
import static org.folio.converter.StringConstants.ENTERED;
import static org.folio.converter.StringConstants.ENTW;
import static org.folio.converter.StringConstants.FEST;
import static org.folio.converter.StringConstants.FILE;
import static org.folio.converter.StringConstants.FMUS;
import static org.folio.converter.StringConstants.FORM;
import static org.folio.converter.StringConstants.FREQ;
import static org.folio.converter.StringConstants.GPUB;
import static org.folio.converter.StringConstants.ILLS;
import static org.folio.converter.StringConstants.INDX;
import static org.folio.converter.StringConstants.LANG;
import static org.folio.converter.StringConstants.LITF;
import static org.folio.converter.StringConstants.LTXT;
import static org.folio.converter.StringConstants.MREC;
import static org.folio.converter.StringConstants.ORIG;
import static org.folio.converter.StringConstants.PART;
import static org.folio.converter.StringConstants.PROJ;
import static org.folio.converter.StringConstants.REGL;
import static org.folio.converter.StringConstants.RELF;
import static org.folio.converter.StringConstants.SL;
import static org.folio.converter.StringConstants.SPFM;
import static org.folio.converter.StringConstants.SRCE;
import static org.folio.converter.StringConstants.SRTP;
import static org.folio.converter.StringConstants.TECH;
import static org.folio.converter.StringConstants.TIME;
import static org.folio.converter.StringConstants.TMAT;
import static org.folio.converter.StringConstants.TRAR;
import static org.folio.converter.StringConstants.VALUE;

import io.vertx.core.json.JsonObject;

import java.util.Arrays;
import java.util.EnumMap;

public class Field008SplitterFactory {
  private static EnumMap<ContentType, Field008SplitStrategy> map = new EnumMap<>(ContentType.class);

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
      jsonObject.put(ILLS, Arrays.asList(ills));
      jsonObject.put(AUDN, source.substring(22, 23));
      jsonObject.put(FORM, source.substring(23, 24));
      String[] cont = {
        source.substring(24, 25),
        source.substring(25, 26),
        source.substring(26, 27),
        source.substring(27, 28)
      };
      jsonObject.put(CONT, Arrays.asList(cont));
      jsonObject.put(GPUB, source.substring(28, 29));
      jsonObject.put(CONF, source.substring(29, 30));
      jsonObject.put(FEST, source.substring(30, 31));
      jsonObject.put(INDX, source.substring(31, 32));
      jsonObject.put(LITF, source.substring(33, 34));
      jsonObject.put(BIOG, source.substring(34, 35));
      jsonObject.mergeIn(splitFooter(source));
      return jsonObject;
    });

    map.put(ContentType.FILES, source -> {
      JsonObject jsonObject = splitHeader(source);
      jsonObject.put(AUDN, source.substring(22, 23));
      jsonObject.put(FORM, source.substring(23, 24));
      jsonObject.put(FILE, source.substring(26, 27));
      jsonObject.put(GPUB, source.substring(28, 29));
      jsonObject.mergeIn(splitFooter(source));
      return jsonObject;
    });

    map.put(ContentType.CONTINUING, source -> {
      JsonObject jsonObject = splitHeader(source);
      jsonObject.put(FREQ, source.substring(18, 19));
      jsonObject.put(REGL, source.substring(19, 20));
      jsonObject.put(SRTP, source.substring(21, 22));
      jsonObject.put(ORIG, source.substring(22, 23));
      jsonObject.put(FORM, source.substring(23, 24));
      jsonObject.put(ENTW, source.substring(24, 25));
      String[] cont = {
        source.substring(25, 26),
        source.substring(26, 27),
        source.substring(27, 28),
      };
      jsonObject.put(CONT, Arrays.asList(cont));
      jsonObject.put(GPUB, source.substring(28, 29));
      jsonObject.put(CONF, source.substring(29, 30));
      jsonObject.put(ALPH, source.substring(33, 34));
      jsonObject.put(SL, source.substring(34, 35));
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
      jsonObject.put(RELF, Arrays.asList(relf));
      jsonObject.put(PROJ, source.substring(22, 24));
      jsonObject.put(CRTP, source.substring(25, 26));
      jsonObject.put(GPUB, source.substring(28, 29));
      jsonObject.put(FORM, source.substring(29, 30));
      jsonObject.put(INDX, source.substring(31, 32));
      jsonObject.put(SPFM, source.substring(33, 35));
      jsonObject.mergeIn(splitFooter(source));
      return jsonObject;
    });

    map.put(ContentType.MIXED, source -> {
      JsonObject jsonObject = splitHeader(source);
      jsonObject.put(FORM, source.substring(23, 24));
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
      jsonObject.put(TIME, Arrays.asList(time));
      jsonObject.put(AUDN, source.substring(22, 23));
      jsonObject.put(GPUB, source.substring(28, 29));
      jsonObject.put(FORM, source.substring(29, 30));
      jsonObject.put(TMAT, source.substring(33, 34));
      jsonObject.put(TECH, source.substring(34, 35));
      jsonObject.mergeIn(splitFooter(source));
      return jsonObject;
    });

    map.put(ContentType.UNKNOWN, source -> {
      JsonObject jsonObject = splitHeader(source);
      jsonObject.put(VALUE, source.substring(18, 35));
      jsonObject.mergeIn(splitFooter(source));
      return jsonObject;
    });
  }

  private static JsonObject splitHeader(String source) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put(ENTERED, source.substring(0, 6));
    jsonObject.put(DTST, source.substring(6, 7));
    jsonObject.put(DATE1, source.substring(7, 11));
    jsonObject.put(DATE2, source.substring(11, 15));
    jsonObject.put(CTRY, source.substring(15, 18));
    return jsonObject;
  }

  private static JsonObject splitFooter(String source) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.put(LANG, source.substring(35, 38));
    jsonObject.put(MREC, source.substring(38, 39));
    jsonObject.put(SRCE, source.substring(39, 40));
    return jsonObject;
  }

  private static JsonObject splitField008ForScoresOrSound(String source) {
    JsonObject jsonObject = splitHeader(source);
    jsonObject.put(COMP, source.substring(18, 20));
    jsonObject.put(FMUS, source.substring(20, 21));
    jsonObject.put(PART, source.substring(21, 22));
    jsonObject.put(AUDN, source.substring(22, 23));
    jsonObject.put(FORM, source.substring(23, 24));
    String[] accm = {
      source.substring(24, 25),
      source.substring(25, 26),
      source.substring(26, 27),
      source.substring(27, 28),
      source.substring(28, 29),
      source.substring(29, 30)
    };
    jsonObject.put(ACCM, Arrays.asList(accm));
    String[] ltxt = {
      source.substring(30, 31),
      source.substring(31, 32)
    };
    jsonObject.put(LTXT, Arrays.asList(ltxt));
    jsonObject.put(TRAR, source.substring(33, 34));
    jsonObject.mergeIn(splitFooter(source));
    return jsonObject;
  }

  public static Field008SplitStrategy getStrategy(ContentType contentType){
    return map.get(contentType);
  }
}
