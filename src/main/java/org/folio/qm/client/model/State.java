package org.folio.qm.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum State {
  ACTUAL("ACTUAL");

  private final String value;

  State(String value) {
    this.value = value;
  }

  @JsonCreator
  public static State fromValue(String value) {
    for (State b : State.values()) {
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
