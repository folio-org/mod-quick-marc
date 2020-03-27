package org.folio.converter;

import java.util.Map;

@FunctionalInterface
public interface Field008SplitStrategy {
  Map<String, Object> split(String source);
}
