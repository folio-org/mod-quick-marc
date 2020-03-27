package org.folio.converter;

import static org.folio.converter.StringConstants.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Field008RestoreFactory {

  private static EnumMap<ContentType, Function<Map<String, Object>, String>> map = new EnumMap<>(ContentType.class);

  private Field008RestoreFactory(){}

  static {
    map.put(ContentType.BOOKS, map ->
      new StringBuilder(restoreHeader(map))
        .append(arrayToString(ILLS, map))
        .append(map.get(AUDN))
        .append(map.get(FORM))
        .append(arrayToString(CONT, map))
        .append(map.get(GPUB))
        .append(map.get(CONF))
        .append(map.get(FEST))
        .append(map.get(INDX))
        .append(ONE_SPACE)
        .append(map.get(LITF))
        .append(map.get(BIOG))
        .append(restoreFooter(map))
        .toString());

    map.put(ContentType.FILES, map ->
      new StringBuilder(restoreHeader(map))
        .append(FOUR_SPACES)
        .append(map.get(AUDN))
        .append(map.get(FORM))
        .append(TWO_SPACES)
        .append(map.get(FILE))
        .append(ONE_SPACE)
        .append(map.get(GPUB))
        .append(SIX_SPACES)
        .append(restoreFooter(map))
        .toString());

    map.put(ContentType.CONTINUING, map ->
      new StringBuilder(restoreHeader(map))
        .append(map.get(FREQ))
        .append(map.get(REGL))
        .append(ONE_SPACE)
        .append(map.get(SRTP))
        .append(map.get(ORIG))
        .append(map.get(FORM))
        .append(map.get(ENTW))
        .append(arrayToString(CONT, map))
        .append(map.get(GPUB))
        .append(map.get(CONF))
        .append(THREE_SPACES)
        .append(map.get(ALPH))
        .append(map.get(SL))
        .append(restoreFooter(map))
        .toString());

    map.put(ContentType.MAPS, map ->
      new StringBuilder(restoreHeader(map))
        .append(arrayToString(RELF, map))
        .append(map.get(PROJ))
        .append(ONE_SPACE)
        .append(map.get(CRTP))
        .append(TWO_SPACES)
        .append(map.get(GPUB))
        .append(map.get(FORM))
        .append(ONE_SPACE)
        .append(map.get(INDX))
        .append(ONE_SPACE)
        .append(map.get(SPFM))
        .append(restoreFooter(map))
        .toString());

    map.put(ContentType.MIXED, map ->
      new StringBuilder(restoreHeader(map))
        .append(FIVE_SPACES)
        .append(map.get(FORM))
        .append(ELEVEN_SPACES)
        .append(restoreFooter(map))
        .toString());

    map.put(ContentType.SCORES, Field008RestoreFactory::restoreValueForScoresOrSound);

    map.put(ContentType.SOUND, Field008RestoreFactory::restoreValueForScoresOrSound);

    map.put(ContentType.VISUAL, map ->
      new StringBuilder(restoreHeader(map))
        .append(arrayToString(TIME, map))
        .append(ONE_SPACE)
        .append(map.get(AUDN))
        .append(FIVE_SPACES)
        .append(map.get(GPUB))
        .append(map.get(FORM))
        .append(THREE_SPACES)
        .append(map.get(TMAT))
        .append(map.get(TECH))
        .append(restoreFooter(map))
        .toString());

    map.put(ContentType.UNKNOWN, map ->
      new StringBuilder(restoreHeader(map))
        .append(map.get(VALUE))
        .append(restoreFooter(map))
        .toString());
  }

  private static String restoreHeader(Map<String, Object> map) {
    return new StringBuilder(map.get(ENTERED).toString())
      .append(map.get(DTST))
      .append(map.get(DATE1))
      .append(map.get(DATE2))
      .append(map.get(CTRY))
      .toString();
  }

  private static String restoreFooter(Map<String, Object> map) {
    return new StringBuilder(map.get(LANG).toString())
      .append(map.get(MREC))
      .append(map.get(SRCE))
      .toString();
  }

  private static String restoreValueForScoresOrSound(Map<String, Object> map){
    return new StringBuilder(restoreHeader(map))
      .append(map.get(COMP))
      .append(map.get(FMUS))
      .append(map.get(PART))
      .append(map.get(AUDN))
      .append(map.get(FORM))
      .append(arrayToString(ACCM, map))
      .append(arrayToString(LTXT, map))
      .append(ONE_SPACE)
      .append(map.get(TRAR))
      .append(ONE_SPACE)
      .append(restoreFooter(map))
      .toString();
  }

  private static String arrayToString(String key, Map<String, Object> map) {
    return ((List<String>) map.get(key)).stream().collect(Collectors.joining());
  }

  public static Function<Map<String, Object>, String> getStrategy(ContentType contentType){
    return map.get(contentType);
  }
}
