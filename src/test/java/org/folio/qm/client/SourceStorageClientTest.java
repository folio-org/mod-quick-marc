package org.folio.qm.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.folio.qm.client.SourceStorageClient.SourceRecordSnapshot;
import org.folio.qm.client.SourceStorageClient.SourceRecordSnapshot.Status;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class SourceStorageClientTest {

  @Test
  void shouldCreateSnapshotWithDefaultStatus() {
    var snapshot = SourceRecordSnapshot.snapshot();

    assertNotNull(snapshot);
    assertNotNull(snapshot.jobExecutionId());
    assertEquals(Status.PARSING_IN_PROGRESS, snapshot.status());
  }

  @Test
  void shouldCreateSnapshotWithCustomValues() {
    var jobExecutionId = UUID.randomUUID();
    var snapshot = new SourceRecordSnapshot(jobExecutionId, Status.COMMITTED);

    assertNotNull(snapshot);
    assertEquals(jobExecutionId, snapshot.jobExecutionId());
    assertEquals(Status.COMMITTED, snapshot.status());
  }

  @Test
  void shouldConvertStatusFromValue() {
    assertEquals(Status.NEW, Status.fromValue("NEW"));
    assertEquals(Status.PARSING_IN_PROGRESS, Status.fromValue("PARSING_IN_PROGRESS"));
    assertEquals(Status.COMMITTED, Status.fromValue("COMMITTED"));
    assertEquals(Status.ERROR, Status.fromValue("ERROR"));
  }

  @Test
  void shouldThrowExceptionForInvalidStatusValue() {
    assertThrows(IllegalArgumentException.class, () -> Status.fromValue("INVALID_STATUS"));
  }

  @Test
  void shouldReturnCorrectStatusValue() {
    assertEquals("NEW", Status.NEW.value());
    assertEquals("PARSING_IN_PROGRESS", Status.PARSING_IN_PROGRESS.value());
    assertEquals("COMMITTED", Status.COMMITTED.value());
  }

  @Test
  void shouldHandleAllStatusValues() {
    assertEquals(Status.PARENT, Status.fromValue("PARENT"));
    assertEquals(Status.FILE_UPLOADED, Status.fromValue("FILE_UPLOADED"));
    assertEquals(Status.PARSING_FINISHED, Status.fromValue("PARSING_FINISHED"));
    assertEquals(Status.PROCESSING_IN_PROGRESS, Status.fromValue("PROCESSING_IN_PROGRESS"));
    assertEquals(Status.PROCESSING_FINISHED, Status.fromValue("PROCESSING_FINISHED"));
    assertEquals(Status.COMMIT_IN_PROGRESS, Status.fromValue("COMMIT_IN_PROGRESS"));
    assertEquals(Status.DISCARDED, Status.fromValue("DISCARDED"));
    assertEquals(Status.CANCELLED, Status.fromValue("CANCELLED"));
  }
}
