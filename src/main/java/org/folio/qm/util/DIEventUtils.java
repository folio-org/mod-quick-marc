package org.folio.qm.util;

import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import org.folio.rest.jaxrs.model.DataImportEventPayload;

@Log4j2
@UtilityClass
public class DIEventUtils {

  public static Optional<UUID> extractInstanceId(DataImportEventPayload data, ObjectMapper objectMapper) {
    return Optional.ofNullable(data.getContext().get("INSTANCE"))
      .map(instancePayload -> {
        try {
          return UUID.fromString(objectMapper.readTree(instancePayload).get("id").asText());
        } catch (JsonProcessingException e) {
          log.debug("Failed to extract instanceId", e);
          return null;
        }
      });
  }
}
