package org.folio.converter;

import java.util.Map;

@FunctionalInterface
public interface Field008RestoreStrategy {
  String restore(Map<String, Object> map);
}
