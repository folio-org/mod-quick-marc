package org.folio.converter;

import io.vertx.core.json.JsonObject;

@FunctionalInterface
public interface Field008SplitStrategy {
  JsonObject split(String source);
}
