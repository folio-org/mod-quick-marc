package org.folio.qm.convertion.converter;

import java.util.EnumMap;
import org.springframework.core.convert.converter.Converter;

public abstract class AbstractEnumConverter<S extends Enum<S>, T extends Enum<T>> implements Converter<S, T> {

  private final EnumMap<S, T> enumMap;

  protected AbstractEnumConverter(EnumMap<S, T> enumMap) {
    this.enumMap = enumMap;
  }

  @Override
  public T convert(S source) {
    return enumMap.get(source);
  }
}
