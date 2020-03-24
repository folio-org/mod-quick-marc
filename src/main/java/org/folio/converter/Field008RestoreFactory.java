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
import static org.folio.converter.StringConstants.ELEVEN_SPACES;
import static org.folio.converter.StringConstants.ENTERED;
import static org.folio.converter.StringConstants.ENTW;
import static org.folio.converter.StringConstants.FEST;
import static org.folio.converter.StringConstants.FILE;
import static org.folio.converter.StringConstants.FIVE_SPACES;
import static org.folio.converter.StringConstants.FMUS;
import static org.folio.converter.StringConstants.FORM;
import static org.folio.converter.StringConstants.FOUR_SPACES;
import static org.folio.converter.StringConstants.FREQ;
import static org.folio.converter.StringConstants.GPUB;
import static org.folio.converter.StringConstants.ILLS;
import static org.folio.converter.StringConstants.INDX;
import static org.folio.converter.StringConstants.LANG;
import static org.folio.converter.StringConstants.LITF;
import static org.folio.converter.StringConstants.LTXT;
import static org.folio.converter.StringConstants.MREC;
import static org.folio.converter.StringConstants.ONE_SPACE;
import static org.folio.converter.StringConstants.ORIG;
import static org.folio.converter.StringConstants.PART;
import static org.folio.converter.StringConstants.PROJ;
import static org.folio.converter.StringConstants.REGL;
import static org.folio.converter.StringConstants.RELF;
import static org.folio.converter.StringConstants.SIX_SPACES;
import static org.folio.converter.StringConstants.SL;
import static org.folio.converter.StringConstants.SPFM;
import static org.folio.converter.StringConstants.SRCE;
import static org.folio.converter.StringConstants.SRTP;
import static org.folio.converter.StringConstants.TECH;
import static org.folio.converter.StringConstants.THREE_SPACES;
import static org.folio.converter.StringConstants.TIME;
import static org.folio.converter.StringConstants.TMAT;
import static org.folio.converter.StringConstants.TRAR;
import static org.folio.converter.StringConstants.TWO_SPACES;
import static org.folio.converter.StringConstants.VALUE;

import io.vertx.core.json.JsonObject;

import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

public class Field008RestoreFactory {

  private static EnumMap<ContentType, Field008RestoreStrategy> map = new EnumMap<>(ContentType.class);

  private Field008RestoreFactory(){}

  static {
    map.put(ContentType.BOOKS, jsonObject ->
      restoreHeader(jsonObject)
        .concat(arrayToString(ILLS, jsonObject))
        .concat(jsonObject.getString(AUDN))
        .concat(jsonObject.getString(FORM))
        .concat(arrayToString(CONT, jsonObject))
        .concat(jsonObject.getString(GPUB))
        .concat(jsonObject.getString(CONF))
        .concat(jsonObject.getString(FEST))
        .concat(jsonObject.getString(INDX))
        .concat(ONE_SPACE)
        .concat(jsonObject.getString(LITF))
        .concat(jsonObject.getString(BIOG))
        .concat(restoreFooter(jsonObject)));

    map.put(ContentType.FILES, jsonObject ->
      restoreHeader(jsonObject)
        .concat(FOUR_SPACES)
        .concat(jsonObject.getString(AUDN))
        .concat(jsonObject.getString(FORM))
        .concat(TWO_SPACES)
        .concat(jsonObject.getString(FILE))
        .concat(ONE_SPACE)
        .concat(jsonObject.getString(GPUB))
        .concat(SIX_SPACES)
        .concat(restoreFooter(jsonObject)));

    map.put(ContentType.CONTINUING, jsonObject ->
      restoreHeader(jsonObject)
        .concat(jsonObject.getString(FREQ))
        .concat(jsonObject.getString(REGL))
        .concat(ONE_SPACE)
        .concat(jsonObject.getString(SRTP))
        .concat(jsonObject.getString(ORIG))
        .concat(jsonObject.getString(FORM))
        .concat(jsonObject.getString(ENTW))
        .concat(arrayToString(CONT, jsonObject))
        .concat(jsonObject.getString(GPUB))
        .concat(jsonObject.getString(CONF))
        .concat(THREE_SPACES)
        .concat(jsonObject.getString(ALPH))
        .concat(jsonObject.getString(SL))
        .concat(restoreFooter(jsonObject)));

    map.put(ContentType.MAPS, jsonObject ->
      restoreHeader(jsonObject)
        .concat(arrayToString(RELF, jsonObject))
        .concat(jsonObject.getString(PROJ))
        .concat(ONE_SPACE)
        .concat(jsonObject.getString(CRTP))
        .concat(TWO_SPACES)
        .concat(jsonObject.getString(GPUB))
        .concat(jsonObject.getString(FORM))
        .concat(ONE_SPACE)
        .concat(jsonObject.getString(INDX))
        .concat(ONE_SPACE)
        .concat(jsonObject.getString(SPFM))
        .concat(restoreFooter(jsonObject)));

    map.put(ContentType.MIXED, jsonObject ->
      restoreHeader(jsonObject)
        .concat(FIVE_SPACES)
        .concat(jsonObject.getString(FORM))
        .concat(ELEVEN_SPACES)
        .concat(restoreFooter(jsonObject)));

    map.put(ContentType.SCORES, Field008RestoreFactory::restoreValueForScoresOrSound);

    map.put(ContentType.SOUND, Field008RestoreFactory::restoreValueForScoresOrSound);

    map.put(ContentType.VISUAL, jsonObject ->
      restoreHeader(jsonObject)
        .concat(arrayToString(TIME, jsonObject))
        .concat(ONE_SPACE)
        .concat(jsonObject.getString(AUDN))
        .concat(FIVE_SPACES)
        .concat(jsonObject.getString(GPUB))
        .concat(jsonObject.getString(FORM))
        .concat(THREE_SPACES)
        .concat(jsonObject.getString(TMAT))
        .concat(jsonObject.getString(TECH))
        .concat(restoreFooter(jsonObject)));

    map.put(ContentType.UNKNOWN, jsonObject ->
      restoreHeader(jsonObject)
        .concat(jsonObject.getString(VALUE))
        .concat(restoreFooter(jsonObject)));
  }

  private static String restoreHeader(JsonObject jsonObject) {
    return jsonObject.getString(ENTERED)
      .concat(jsonObject.getString(DTST))
      .concat(jsonObject.getString(DATE1))
      .concat(jsonObject.getString(DATE2))
      .concat(jsonObject.getString(CTRY));
  }

  private static String restoreFooter(JsonObject jsonObject) {
    return jsonObject.getString(LANG)
      .concat(jsonObject.getString(MREC))
      .concat(jsonObject.getString(SRCE));
  }

  private static String restoreValueForScoresOrSound(JsonObject jsonObject){
    return restoreHeader(jsonObject)
      .concat(jsonObject.getString(COMP))
      .concat(jsonObject.getString(FMUS))
      .concat(jsonObject.getString(PART))
      .concat(jsonObject.getString(AUDN))
      .concat(jsonObject.getString(FORM))
      .concat(arrayToString(ACCM, jsonObject))
      .concat(arrayToString(LTXT, jsonObject))
      .concat(ONE_SPACE)
      .concat(jsonObject.getString(TRAR))
      .concat(ONE_SPACE)
      .concat(restoreFooter(jsonObject));
  }

  private static String arrayToString(String key, JsonObject jsonObject) {
    return ((List<String>)jsonObject.getJsonArray(key).getList()).stream().collect(Collectors.joining());
  }

  public static Field008RestoreStrategy getStrategy(ContentType contentType){
    return map.get(contentType);
  }
}
