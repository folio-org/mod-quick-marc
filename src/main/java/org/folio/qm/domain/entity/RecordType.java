package org.folio.qm.domain.entity;

import lombok.Getter;

@Getter
public enum RecordType {

  MARC_BIBLIOGRAPHIC("bibliographic"),
  MARC_HOLDINGS("holdings"),
  MARC_AUTHORITY("authority");

  private final String value;

  RecordType(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public static RecordType fromValue(String value) {
    for (RecordType b : RecordType.values()) {
      if (b.value.equalsIgnoreCase(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}
