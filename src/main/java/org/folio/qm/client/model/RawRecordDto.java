package org.folio.qm.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class RawRecordDto {

  @JsonProperty("record")
  private String recordData;
  private Integer order;
}

