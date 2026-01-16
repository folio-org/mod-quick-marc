package org.folio.qm.service.storage.folio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.qm.client.InstanceStorageClient;
import org.folio.qm.client.PrecedingSucceedingTitlesClient;
import org.folio.qm.domain.model.InstanceRecord;
import org.folio.rest.jaxrs.model.InstancePrecedingSucceedingTitles;
import org.folio.spring.exception.NotFoundException;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class FolioRecordInstanceServiceTest {

  private static final String HRID = "hrid";

  @Mock
  private InstanceStorageClient storageClient;

  @Mock
  private PrecedingSucceedingTitlesClient precedingSucceedingTitlesClient;

  @InjectMocks
  private FolioRecordInstanceService service;

  @Test
  void get_shouldReturnInstanceWhenFound() {
    var id = UUID.randomUUID();
    var instance = new InstanceRecord();
    instance.setId(UUID.randomUUID().toString());
    when(storageClient.getInstanceById(id)).thenReturn(Optional.of(instance));

    var result = service.get(id);

    assertEquals(instance, result);
  }

  @Test
  void get_shouldThrowNotFoundWhenMissing() {
    when(storageClient.getInstanceById(any())).thenReturn(Optional.empty());
    var id = UUID.randomUUID();

    assertThrows(NotFoundException.class, () -> service.get(id));
  }

  @Test
  void create_shouldReturnCreatedInstanceAndSetIdOnInputAndUpdateTitles() {
    var instanceRecord = new InstanceRecord();
    instanceRecord.setTitle("title");
    var createdInstance = new InstanceRecord();
    createdInstance.setId(UUID.randomUUID().toString());
    when(storageClient.createInstance(instanceRecord)).thenReturn(createdInstance);

    var result = service.create(instanceRecord);

    assertEquals(createdInstance, result);
    assertEquals(createdInstance.getId(), instanceRecord.getId());
    verify(precedingSucceedingTitlesClient)
      .updateTitles(createdInstance.getId(), new InstancePrecedingSucceedingTitles(Collections.emptyList(), 0L));
  }

  @Test
  void update_shouldCallStorageAndUpdateTitles() {
    var id = UUID.randomUUID();
    var folioRecord = new InstanceRecord();
    folioRecord.setId(UUID.randomUUID().toString());

    var expectedTitles = new InstancePrecedingSucceedingTitles(Collections.emptyList(), 0L);

    service.update(id, folioRecord);

    verify(storageClient).updateInstance(id, folioRecord);
    verify(precedingSucceedingTitlesClient).updateTitles(id.toString(), expectedTitles);
  }

  @Test
  void getInstanceIdByHrid_shouldReturnIdWhenSingleResult() {
    var instanceRecord = new InstanceRecord();
    var instanceId = UUID.randomUUID().toString();
    instanceRecord.setId(instanceId);
    var result = new InstanceStorageClient.InstanceResult(List.of(instanceRecord), 1);
    when(storageClient.getInstanceByHrid(HRID)).thenReturn(result);

    var id = service.getInstanceIdByHrid(HRID);

    assertThat(id).isEqualTo(instanceId);
  }

  @Test
  void getInstanceIdByHrid_shouldThrowWhenEmptyResult() {
    var emptyResult = new InstanceStorageClient.InstanceResult(Collections.emptyList(), 0);
    when(storageClient.getInstanceByHrid(HRID)).thenReturn(emptyResult);

    assertThrows(IllegalStateException.class, () -> service.getInstanceIdByHrid(HRID));
  }

  @Test
  void getInstanceIdByHrid_shouldThrowWhenTotalCountMoreOne() {
    var instanceRecord = new InstanceRecord();
    var instanceId = UUID.randomUUID().toString();
    instanceRecord.setId(instanceId);
    var result = new InstanceStorageClient.InstanceResult(List.of(instanceRecord), 3);
    when(storageClient.getInstanceByHrid(HRID)).thenReturn(result);

    assertThrows(IllegalStateException.class, () -> service.getInstanceIdByHrid(HRID));
  }
}
