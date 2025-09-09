package org.folio.qm.client.model;

import java.time.OffsetDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@NoArgsConstructor
public class Metadata {

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime createdDate;
  private String createdByUserId;
  private String createdByUsername;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime updatedDate;
  private String updatedByUserId;
  private String updatedByUsername;
}

