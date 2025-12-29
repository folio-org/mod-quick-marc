package org.folio.qm.service.change;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ChangeRecordServiceRegistryTest {

  private @Mock ChangeRecordService bibliographicService;
  private @Mock ChangeRecordService authorityService;
  private @Mock ChangeRecordService holdingsService;

  private ChangeRecordServiceRegistry registry;

  @BeforeEach
  void setUp() {
    when(bibliographicService.supportedType()).thenReturn(MarcFormat.BIBLIOGRAPHIC);
    when(authorityService.supportedType()).thenReturn(MarcFormat.AUTHORITY);
    when(holdingsService.supportedType()).thenReturn(MarcFormat.HOLDINGS);

    registry = new ChangeRecordServiceRegistry(List.of(bibliographicService, authorityService, holdingsService));
  }

  @Test
  void shouldGetBibliographicService() {
    var result = registry.get(MarcFormat.BIBLIOGRAPHIC);

    assertNotNull(result);
    assertEquals(bibliographicService, result);
  }

  @Test
  void shouldGetAuthorityService() {
    var result = registry.get(MarcFormat.AUTHORITY);

    assertNotNull(result);
    assertEquals(authorityService, result);
  }

  @Test
  void shouldGetHoldingsService() {
    var result = registry.get(MarcFormat.HOLDINGS);

    assertNotNull(result);
    assertEquals(holdingsService, result);
  }

  @Test
  void shouldThrowExceptionForUnsupportedFormat() {
    registry = new ChangeRecordServiceRegistry(List.of(bibliographicService));

    var exception = assertThrows(IllegalArgumentException.class,
      () -> registry.get(MarcFormat.AUTHORITY));

    assertNotNull(exception);
    assertEquals("No record service found for record type: AUTHORITY", exception.getMessage());
  }
}
