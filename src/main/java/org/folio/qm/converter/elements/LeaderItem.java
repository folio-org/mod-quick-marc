package org.folio.qm.converter.elements;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum LeaderItem {
  //COMMON FIELDS FOR BIB, HOLDINGS AND AUTHORITY
  RECORD_LENGTH("Record length", 0, 5),
  CODING_SCHEME("Character coding scheme", 9, 1),
  INDICATOR_COUNT("Indicator count", 10, 1, 2),
  SUBFIELD_CODE_LENGTH("Subfield code length", 11, 1, 2),
  BASE_ADDRESS("Base address of data", 12, 5),
  ENTRY_MAP_20("Entry map", 20, 1, 4),
  ENTRY_MAP_21("Entry map", 21, 1, 5),
  ENTRY_MAP_22("Entry map", 22, 1, 0),
  ENTRY_MAP_23("Entry map", 23, 1, 0),

  //COMMON FIELDS FOR HOLDINGS AND AUTHORITY
  UNDEFINED_CHARACTER_POSITION_7("Undefined character position", 7, 1, '\\', ' '),
  UNDEFINED_CHARACTER_POSITION_8("Undefined character position", 8, 1, '\\', ' '),
  UNDEFINED_CHARACTER_POSITION_19("Encoding level", 19, 1, '\\', ' '),

  //BIB FIELDS
  BIB_RECORD_STATUS("Record status", 5, 1, 'a', 'c', 'd', 'n', 'p'),
  BIB_RECORD_TYPE("Type of record", 6, 1, 'a', 'c', 'd', 'e', 'f', 'g', 'i', 'j', 'k', 'm', 'o', 'p', 'r', 't'),
  BIBLIOGRAPHIC_LEVEL("Bibliographic level", 7, 1, 'a', 'b', 'c', 'd', 'i', 'm', 's'),
  CONTROL_TYPE("Type of control", 8, 1, '\\', ' ', 'a'),
  BIB_ENCODING_LEVEL("Encoding level", 17, 1, '\\', ' ', 1, 2, 3, 4, 5, 7, 8, 'u', 'z'),
  CATALOGING_FORM("Descriptive cataloging form", 18, 1, '\\', ' ', 'a', 'c', 'i', 'n', 'u'),
  RESOURCE_RECORD_LEVEL("Multipart resource record level", 18, 1, '\\', ' ', 'a', 'c', 'i', 'n', 'u'),

  //HOLDINGS FIELDS
  HOLDINGS_RECORD_STATUS("Record status", 5, 1, 'c', 'd', 'n'),
  HOLDINGS_RECORD_TYPE("Type of record", 6, 1, 'u', 'v', 'x', 'y'),
  HOLDINGS_ENCODING_LEVEL("Encoding level", 17, 1, 1, 2, 3, 4, 5, 'm', 'u', 'z'),
  ITEM_INFORMATION("Encoding level", 18, 1),

  //AUTHORITY FIELDS
  AUTHORITY_RECORD_STATUS("Record status", 5, 1, 'a', 'c', 'd', 'n', 'o', 's', 'x'),
  AUTHORITY_RECORD_TYPE("Type of record", 6, 1, 'z'),
  AUTHORITY_ENCODING_LEVEL("Encoding level", 17, 1,  'n', 'o'),
  PUNCTUATION_POLICY("Punctuation policy", 18, 1, '\\', ' ', ' ', 'c', 'i', 'u');

  private final String name;
  private final int position;
  private final int length;
  private final List<Character> possibleValues;


  LeaderItem(String name, int position, int length, Object ... possibleValues) {
    this.name = name;
    this.position = position;
    this.length = length;
    this.possibleValues = Arrays.stream(possibleValues).map(value -> (Character) value).collect(Collectors.toList());
  }

  public String getName() {
    return name;
  }

  public int getPosition() {
    return position;
  }

  public int getLength() {
    return length;
  }

  public List<Character> getPossibleValues() {
    return possibleValues;
  }
}
