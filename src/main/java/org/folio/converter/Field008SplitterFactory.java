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

import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Field008SplitterFactory {
  private static EnumMap<ContentType, Field008SplitStrategy> map = new EnumMap<>(ContentType.class);

  private Field008SplitterFactory(){}

  static {
    map.put(ContentType.BOOKS, source -> {
      Map<String, Object> map = splitHeader(source);
      String[] ills = {
        source.substring(18, 19),
        source.substring(19, 20),
        source.substring(20, 21),
        source.substring(21, 22)
      };
      map.put(ILLS, Arrays.asList(ills));
      map.put(AUDN, source.substring(22, 23));
      map.put(FORM, source.substring(23, 24));
      String[] cont = {
        source.substring(24, 25),
        source.substring(25, 26),
        source.substring(26, 27),
        source.substring(27, 28)
      };
      map.put(CONT, Arrays.asList(cont));
      map.put(GPUB, source.substring(28, 29));
      map.put(CONF, source.substring(29, 30));
      map.put(FEST, source.substring(30, 31));
      map.put(INDX, source.substring(31, 32));
      map.put(LITF, source.substring(33, 34));
      map.put(BIOG, source.substring(34, 35));
      map.putAll(splitFooter(source));
      return map;
    });

    map.put(ContentType.FILES, source -> {
      Map<String, Object> map = splitHeader(source);
      map.put(AUDN, source.substring(22, 23));
      map.put(FORM, source.substring(23, 24));
      map.put(FILE, source.substring(26, 27));
      map.put(GPUB, source.substring(28, 29));
      map.putAll(splitFooter(source));
      return map;
    });

    map.put(ContentType.CONTINUING, source -> {
      Map<String, Object> map = splitHeader(source);
      map.put(FREQ, source.substring(18, 19));
      map.put(REGL, source.substring(19, 20));
      map.put(SRTP, source.substring(21, 22));
      map.put(ORIG, source.substring(22, 23));
      map.put(FORM, source.substring(23, 24));
      map.put(ENTW, source.substring(24, 25));
      String[] cont = {
        source.substring(25, 26),
        source.substring(26, 27),
        source.substring(27, 28),
      };
      map.put(CONT, Arrays.asList(cont));
      map.put(GPUB, source.substring(28, 29));
      map.put(CONF, source.substring(29, 30));
      map.put(ALPH, source.substring(33, 34));
      map.put(SL, source.substring(34, 35));
      map.putAll(splitFooter(source));
      return map;
    });

    map.put(ContentType.MAPS, source -> {
      Map<String, Object> map = splitHeader(source);
      String[] relf = {
        source.substring(18, 19),
        source.substring(19, 20),
        source.substring(20, 21),
        source.substring(21, 22)
      };
      map.put(RELF, Arrays.asList(relf));
      map.put(PROJ, source.substring(22, 24));
      map.put(CRTP, source.substring(25, 26));
      map.put(GPUB, source.substring(28, 29));
      map.put(FORM, source.substring(29, 30));
      map.put(INDX, source.substring(31, 32));
      map.put(SPFM, source.substring(33, 35));
      map.putAll(splitFooter(source));
      return map;
    });

    map.put(ContentType.MIXED, source -> {
      Map<String, Object> map = splitHeader(source);
      map.put(FORM, source.substring(23, 24));
      map.putAll(splitFooter(source));
      return map;
    });

    map.put(ContentType.SCORES, Field008SplitterFactory::splitField008ForScoresOrSound);

    map.put(ContentType.SOUND, Field008SplitterFactory::splitField008ForScoresOrSound);

    map.put(ContentType.VISUAL, source -> {
      Map<String, Object> map = splitHeader(source);
      String[] time = {
        source.substring(18, 19),
        source.substring(19, 20),
        source.substring(20, 21)
      };
      map.put(TIME, Arrays.asList(time));
      map.put(AUDN, source.substring(22, 23));
      map.put(GPUB, source.substring(28, 29));
      map.put(FORM, source.substring(29, 30));
      map.put(TMAT, source.substring(33, 34));
      map.put(TECH, source.substring(34, 35));
      map.putAll(splitFooter(source));
      return map;
    });

    map.put(ContentType.UNKNOWN, source -> {
      Map<String, Object> map = splitHeader(source);
      map.put(VALUE, source.substring(18, 35));
      map.putAll(splitFooter(source));
      return map;
    });
  }

  private static Map<String, Object> splitHeader(String source) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(ENTERED, source.substring(0, 6));
    map.put(DTST, source.substring(6, 7));
    map.put(DATE1, source.substring(7, 11));
    map.put(DATE2, source.substring(11, 15));
    map.put(CTRY, source.substring(15, 18));
    return map;
  }

  private static Map<String, Object> splitFooter(String source) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(LANG, source.substring(35, 38));
    map.put(MREC, source.substring(38, 39));
    map.put(SRCE, source.substring(39, 40));
    return map;
  }

  private static Map<String, Object> splitField008ForScoresOrSound(String source) {
    Map<String, Object> map = splitHeader(source);
    map.put(COMP, source.substring(18, 20));
    map.put(FMUS, source.substring(20, 21));
    map.put(PART, source.substring(21, 22));
    map.put(AUDN, source.substring(22, 23));
    map.put(FORM, source.substring(23, 24));
    String[] accm = {
      source.substring(24, 25),
      source.substring(25, 26),
      source.substring(26, 27),
      source.substring(27, 28),
      source.substring(28, 29),
      source.substring(29, 30)
    };
    map.put(ACCM, Arrays.asList(accm));
    String[] ltxt = {
      source.substring(30, 31),
      source.substring(31, 32)
    };
    map.put(LTXT, Arrays.asList(ltxt));
    map.put(TRAR, source.substring(33, 34));
    map.putAll(splitFooter(source));
    return map;
  }

  public static Field008SplitStrategy getStrategy(ContentType contentType){
    return map.get(contentType);
  }
}
