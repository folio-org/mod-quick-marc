package org.folio.qm.convertion;

import org.folio.qm.exception.NullConvertionException;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

@Component
public class RecordConversionService {

  private final ConversionService conversionService;

  public RecordConversionService(ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  public <T> @NonNull T convert(@NonNull Object source, @NonNull Class<T> targetType) {
    var converted = conversionService.convert(source, targetType);
    if (converted != null) {
      return converted;
    }
    throw new NullConvertionException(targetType);
  }
}
