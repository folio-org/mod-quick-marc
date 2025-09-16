package org.folio.qm.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RecordTypeEnum {

  BIB("MARC_BIB"),
  AUTHORITY("MARC_AUTHORITY"),
  HOLDING("MARC_HOLDING");

  private final String value;

  RecordTypeEnum(String value) {
    this.value = value;
  }

  @JsonCreator
  public static RecordTypeEnum fromValue(String value) {
    for (RecordTypeEnum b : RecordTypeEnum.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }
}
