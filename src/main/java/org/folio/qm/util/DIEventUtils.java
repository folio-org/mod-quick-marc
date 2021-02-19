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
          var idNode = objectMapper.readTree(instancePayload).get("id");
          return idNode != null ? UUID.fromString(idNode.asText()) : null;
        } catch (JsonProcessingException e) {
          log.info("Failed to process json", e);
          throw new IllegalStateException("Failed to process json with message: " + e.getMessage());
        }
      });
  }

  public static Optional<String> extractErrorMessage(DataImportEventPayload data) {
    return Optional.ofNullable(data.getContext().get("ERROR"));
  }
}
