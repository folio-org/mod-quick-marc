package org.folio.qm.service.storage.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.folio.Record;
import org.folio.qm.client.SourceStorageClient;
import org.folio.spring.exception.NotFoundException;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SourceRecordServiceTest {

  @Mock
  private SourceStorageClient sourceStorageClient;

  @InjectMocks
  private SourceRecordServiceImpl service;

  @Test
  void getByExternalId_positive() {
    var id = UUID.randomUUID();
    var sourceRecord = new Record();
    when(sourceStorageClient.getSourceRecord(id, SourceStorageClient.IdType.EXTERNAL))
      .thenReturn(Optional.of(sourceRecord));

    var result = service.getByExternalId(id);

    assertEquals(sourceRecord, result);
    verify(sourceStorageClient).getSourceRecord(id, SourceStorageClient.IdType.EXTERNAL);
  }

  @Test
  void getByExternalId_negative_notFound() {
    var id = UUID.randomUUID();
    when(sourceStorageClient.getSourceRecord(id, SourceStorageClient.IdType.EXTERNAL))
      .thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.getByExternalId(id));
  }

  @Test
  void create_positive() {
    var snapshotId = UUID.randomUUID();
    SourceStorageClient.SourceRecordSnapshot snapshot = mock(SourceStorageClient.SourceRecordSnapshot.class);
    when(snapshot.jobExecutionId()).thenReturn(snapshotId);
    when(sourceStorageClient.createSnapshot(any())).thenReturn(snapshot);

    var created = new Record();
    when(sourceStorageClient.createSourceRecord(any())).thenReturn(created);
    var sourceRecord = new Record();

    var result = service.create(sourceRecord);

    assertEquals(created, result);
    assertEquals(snapshotId.toString(), sourceRecord.getSnapshotId());
    verify(sourceStorageClient).createSnapshot(any());
    verify(sourceStorageClient).createSourceRecord(sourceRecord);
  }

  @Test
  void create_negative_snapshotCreationFails() {
    when(sourceStorageClient.createSnapshot(any()))
      .thenThrow(new RuntimeException("Snapshot creation failed"));
    var sourceRecord = new Record();

    assertThrows(RuntimeException.class, () -> service.create(sourceRecord));
  }

  @Test
  void create_negative_sourceRecordCreationFails() {
    var snapshotId = UUID.randomUUID();
    SourceStorageClient.SourceRecordSnapshot snapshot = mock(SourceStorageClient.SourceRecordSnapshot.class);
    when(snapshot.jobExecutionId()).thenReturn(snapshotId);
    when(sourceStorageClient.createSnapshot(any())).thenReturn(snapshot);
    when(sourceStorageClient.createSourceRecord(any()))
      .thenThrow(new RuntimeException("Source record creation failed"));
    var sourceRecord = new Record();

    assertThrows(RuntimeException.class, () -> service.create(sourceRecord));
  }

  @Test
  void update_positive() {
    var snapshotId = UUID.randomUUID();
    SourceStorageClient.SourceRecordSnapshot snapshot = mock(SourceStorageClient.SourceRecordSnapshot.class);
    when(snapshot.jobExecutionId()).thenReturn(snapshotId);
    when(sourceStorageClient.createSnapshot(any())).thenReturn(snapshot);
    var sourceRecord = new Record();
    var id = UUID.randomUUID();

    service.update(id, sourceRecord);

    assertEquals(snapshotId.toString(), sourceRecord.getSnapshotId());
    verify(sourceStorageClient).createSnapshot(any());
    verify(sourceStorageClient).updateSourceRecord(id, sourceRecord);
  }

  @Test
  void update_negative_snapshotCreationFails() {
    when(sourceStorageClient.createSnapshot(any()))
      .thenThrow(new RuntimeException("Snapshot creation failed"));
    var id = UUID.randomUUID();
    var sourceRecord = new Record();

    assertThrows(RuntimeException.class, () -> service.update(id, sourceRecord));
  }

  @Test
  void update_negative_updateSourceRecordFails() {
    SourceStorageClient.SourceRecordSnapshot snapshot = mock(SourceStorageClient.SourceRecordSnapshot.class);
    when(snapshot.jobExecutionId()).thenReturn(UUID.randomUUID());
    when(sourceStorageClient.createSnapshot(any())).thenReturn(snapshot);
    doThrow(new RuntimeException("Update failed"))
      .when(sourceStorageClient).updateSourceRecord(any(), any());

    var id = UUID.randomUUID();
    var sourceRecord = new Record();

    assertThrows(RuntimeException.class, () -> service.update(id, sourceRecord));
  }
}
