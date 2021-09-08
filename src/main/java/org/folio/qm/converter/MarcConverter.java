package org.folio.qm.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

public interface MarcConverter<S, T, F> extends Converter<S, T> {

  T convert(@NonNull S source);

  F supportedType();

}
