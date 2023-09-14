package org.folio.qm.domain.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RecordType {

  MARC_BIBLIOGRAPHIC("bibliographic"),
  MARC_HOLDINGS("holdings"),
  MARC_AUTHORITY("authority");
  private String value;

  RecordType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static RecordType fromValue(String value) {
    for (RecordType b : RecordType.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}
