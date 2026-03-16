package org.folio.qm.convertion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.folio.qm.exception.NullConvertionException;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;

@UnitTest
@ExtendWith(MockitoExtension.class)
class RecordConversionServiceTest {

  private @Mock ConversionService conversionService;
  private @InjectMocks RecordConversionService recordConversionService;

  @Test
  void shouldConvertSuccessfully() {
    var source = "test";
    var expected = 123;
    when(conversionService.convert(source, Integer.class)).thenReturn(expected);

    var result = recordConversionService.convert(source, Integer.class);

    assertNotNull(result);
    assertEquals(expected, result);
    verify(conversionService).convert(source, Integer.class);
  }

  @Test
  void shouldThrowNullConvertionExceptionWhenConversionReturnsNull() {
    var source = "test";
    when(conversionService.convert(any(), eq(String.class))).thenReturn(null);

    var exception = assertThrows(NullConvertionException.class,
      () -> recordConversionService.convert(source, String.class));

    assertNotNull(exception);
    assertEquals("Conversion resulted in null for target type: java.lang.String", exception.getMessage());
    verify(conversionService).convert(source, String.class);
  }

  @Test
  void shouldConvertComplexObject() {
    var source = new Object();
    var expected = "converted";
    when(conversionService.convert(source, String.class)).thenReturn(expected);

    var result = recordConversionService.convert(source, String.class);

    assertEquals(expected, result);
  }
}
