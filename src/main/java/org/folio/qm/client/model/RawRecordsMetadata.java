package org.folio.qm.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class RawRecordsMetadata {

  private Boolean last;
  private Integer counter;
  private Integer total;
  private ContentTypeEnum contentType;

  public enum ContentTypeEnum {

    RAW("MARC_RAW"),
    JSON("MARC_JSON"),
    XML("MARC_XML");

    private final String value;

    ContentTypeEnum(String value) {
      this.value = value;
    }

    @JsonCreator
    public static ContentTypeEnum fromValue(String value) {
      for (ContentTypeEnum b : ContentTypeEnum.values()) {
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

