package org.folio.qm.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import org.folio.qm.domain.entity.RecordCreationStatus;
import org.folio.qm.domain.repository.RecordCreationStatusRepository;
import org.folio.spring.testing.extension.Random;
import org.folio.spring.testing.extension.impl.RandomParametersExtension;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith({
  MockitoExtension.class,
  RandomParametersExtension.class
})
class StatusServiceImplTest {

  @Mock
  private RecordCreationStatusRepository statusRepository;

  @InjectMocks
  private StatusServiceImpl service;

  @Test
  void shouldReturnStatusById(@Random UUID id, @Random RecordCreationStatus statusDto) {
    when(statusRepository.findById(any(UUID.class))).thenReturn(Optional.of(statusDto));

    var actual = service.findById(id);

    assertThat(actual).contains(statusDto);
  }

  @Test
  void shouldEmptyOptionalWhenNoRecordInDb(@Random UUID notExistedId) {
    when(statusRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

    var actual = service.findById(notExistedId);

    assertThat(actual).isEmpty();
  }

  @Test
  void shouldDeleteOutdated() {
    var currentTime = new Timestamp(System.currentTimeMillis());

    service.removeOlderThan(currentTime);

    verify(statusRepository).deleteByUpdatedAtBefore(currentTime);
  }
}
