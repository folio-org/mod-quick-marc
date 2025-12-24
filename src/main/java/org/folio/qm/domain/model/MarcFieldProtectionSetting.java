package org.folio.qm.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MarcFieldProtectionSetting {

  private UUID id;
  private String field;
  private String indicator1;
  private String indicator2;
  private String subfield;
  private String data;
  private SourceEnum source;
  private Boolean override = false;

  public enum SourceEnum {
    SYSTEM("SYSTEM"),

    USER("USER");

    private final String value;

    SourceEnum(String value) {
      this.value = value;
    }

    @JsonCreator
    public static SourceEnum fromValue(String value) {
      for (SourceEnum b : SourceEnum.values()) {
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
}

