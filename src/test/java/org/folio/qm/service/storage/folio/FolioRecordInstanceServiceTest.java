package org.folio.qm.service.storage.folio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.support.utils.ApiTestUtils.TENANT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.folio.qm.client.InstanceStorageClient;
import org.folio.qm.client.PrecedingSucceedingTitlesClient;
import org.folio.qm.domain.model.InstanceRecord;
import org.folio.qm.service.mapping.InstanceToInstanceRecordMapper;
import org.folio.qm.service.storage.tenant.UserTenantsService;
import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.jaxrs.model.InstancePrecedingSucceedingTitles;
import org.folio.rest.jaxrs.model.Instances;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.exception.NotFoundException;
import org.folio.spring.scope.FolioExecutionContextService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class FolioRecordInstanceServiceTest {

  private static final String HRID = "hrid";
  private static final String SHARED_INSTANCE_HRID = "test-hrid";

  @Mock
  private InstanceStorageClient storageClient;
  @Mock
  private PrecedingSucceedingTitlesClient precedingSucceedingTitlesClient;
  @Mock
  private InstanceToInstanceRecordMapper instanceMapper;
  @Mock
  private UserTenantsService userTenantsService;
  @Mock
  private FolioExecutionContext folioExecutionContext;
  @Mock
  private FolioExecutionContextService executionService;

  @InjectMocks
  private FolioRecordInstanceService service;

  @BeforeEach
  void setUp() {
    lenient().when(folioExecutionContext.getTenantId()).thenReturn(TENANT_ID);
    lenient().when(userTenantsService.getCentralTenant(any())).thenReturn(Optional.of("central"));
  }

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

    NotFoundException ex = assertThrows(NotFoundException.class, () -> service.get(id));
    assertThat(ex.getMessage()).contains("Instance record with id: " + id + " not found");
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
  void getInstanceIdByHrid_shouldThrowWhenEmptyResult() {
    lenient().when(userTenantsService.getCentralTenant(any())).thenReturn(Optional.empty());
    var instances = new Instances();
    instances.setTotalRecords(0L);
    when(storageClient.getInstances(HRID)).thenReturn(instances);

    assertThrows(NotFoundException.class, () -> service.getInstanceIdByHrid(HRID));
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

    var instances = getInstances(List.of(instanceRecord), 1L);

    when(storageClient.getInstances(HRID)).thenReturn(instances);

    var id = service.getInstanceIdByHrid(HRID);

    assertThat(id).isEqualTo(instanceId);
  }

  @Test
  void getInstanceIdByHrid_shouldThrowWhenTotalCountMoreOne() {
    var instanceId = UUID.randomUUID().toString();
    var instanceRecord = new InstanceRecord().withId(instanceId);

    var instances = getInstances(List.of(instanceRecord), 3L);
    when(storageClient.getInstances(HRID)).thenReturn(instances);

    IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.getInstanceIdByHrid(HRID));
    assertThat(ex.getMessage()).contains("Multiple instances found for HRID: " + HRID + " in tenant: " + TENANT_ID);
  }

  @Test
  void getInstanceIdByHrid_shouldCreateShadowCopyWhenNoLocalInstanceAndConsortiumMember() {
    // Central tenant returns one shared instance
    var sharedInstance = new Instance().withId(UUID.randomUUID().toString());
    var centralInstances = getInstances(List.of(sharedInstance), 1L);

    // Local tenant has no instances
    var localInstances = getInstances(null, 0L);
    when(storageClient.getInstances(SHARED_INSTANCE_HRID))
      .thenReturn(localInstances)   // first call → local tenant
      .thenReturn(centralInstances); // second call → central tenant

    // Titles for the shared instance
    var titles = new InstancePrecedingSucceedingTitles().withTotalRecords(0L);
    when(precedingSucceedingTitlesClient.getTitles(sharedInstance.getId())).thenReturn(titles);

    mockExecutionServiceExecute();

    // Mapper returns a new InstanceRecord
    var mappedRecord = new InstanceRecord();
    when(instanceMapper.toInstanceRecord(sharedInstance)).thenReturn(mappedRecord);

    // Shadow copy created
    var shadowCopy = new InstanceRecord();
    shadowCopy.setId(UUID.randomUUID().toString());
    when(storageClient.createInstance(mappedRecord)).thenReturn(shadowCopy);

    // Act
    var resultId = service.getInstanceIdByHrid(SHARED_INSTANCE_HRID);

    // Assert
    assertThat(resultId).isEqualTo(shadowCopy.getId());
    // Verify that shadow copy was created
    verify(storageClient).createInstance(mappedRecord);
    // Verify that titles were not updated since there are no titles for the shared instance
    verify(precedingSucceedingTitlesClient, never()).updateTitles(any(), any());
    // Ensure execute was called with correct tenant
    verify(executionService).execute(eq("central"), eq(folioExecutionContext), any(Callable.class));
  }

  @Test
  void getInstanceIdByHrid_shouldCreateShadowCopyAndTitlesWhenNoLocalInstanceAndConsortiumMember() {
    // Central tenant returns one shared instance
    var sharedInstance = new Instance().withId(UUID.randomUUID().toString());
    var centralInstances = getInstances(List.of(sharedInstance), 1L);
    // Local tenant has no instances
    var localInstances = getInstances(null, 0L);
    when(storageClient.getInstances(SHARED_INSTANCE_HRID))
      .thenReturn(localInstances)   // first call → local tenant
      .thenReturn(centralInstances); // second call → central tenant

    // Titles for the shared instance
    var titles = new InstancePrecedingSucceedingTitles().withTotalRecords(3L);
    when(precedingSucceedingTitlesClient.getTitles(sharedInstance.getId())).thenReturn(titles);
    doNothing().when(precedingSucceedingTitlesClient).updateTitles(any(), any());

    mockExecutionServiceExecute();

    // Mapper returns a new InstanceRecord
    var mappedRecord = new InstanceRecord();
    when(instanceMapper.toInstanceRecord(sharedInstance)).thenReturn(mappedRecord);

    // Shadow copy created
    var shadowCopy = new InstanceRecord();
    shadowCopy.setId(UUID.randomUUID().toString());
    when(storageClient.createInstance(mappedRecord)).thenReturn(shadowCopy);

    // Act
    var resultId = service.getInstanceIdByHrid(SHARED_INSTANCE_HRID);

    // Assert
    assertThat(resultId).isEqualTo(shadowCopy.getId());
    // Verify that shadow copy was created and titles were updated
    verify(storageClient).createInstance(mappedRecord);
    verify(precedingSucceedingTitlesClient).updateTitles(any(), any());

    // Ensure execute was called with correct tenant
    verify(executionService).execute(eq("central"), eq(folioExecutionContext), any(Callable.class));
  }

  @Test
  void getInstanceIdByHrid_shouldThrowWhenNoSharedInstanceInConsortiumMember() {
    // Central tenant returns one shared instance
    var centralInstances = getInstances(null, 0L);
    // Local tenant has no instances
    var localInstances = getInstances(null, 0L);
    when(storageClient.getInstances(SHARED_INSTANCE_HRID))
      .thenReturn(localInstances)   // first call → local tenant
      .thenReturn(centralInstances); // second call → central tenant

    mockExecutionServiceExecute();

    // Act
    var ex = assertThrows(NotFoundException.class,
      () -> service.getInstanceIdByHrid(SHARED_INSTANCE_HRID));

    assertThat(ex.getMessage()).contains("No instance found for HRID: " + SHARED_INSTANCE_HRID + " in tenant: central");
    // Verify that shadow copy was not created since there is no shared instance in central tenant
    verify(storageClient, never()).createInstance(any());
    verify(precedingSucceedingTitlesClient, never()).updateTitles(any(), any());

    // Ensure execute was called with correct tenant
    verify(executionService).execute(eq("central"), eq(folioExecutionContext), any(Callable.class));
  }

  private void mockExecutionServiceExecute() {
    when(executionService.execute(anyString(), any(FolioExecutionContext.class), any(Callable.class)))
      .thenAnswer(invocation -> {
        Callable<?> callable = invocation.getArgument(2);
        return callable.call();
      });
  }

  private Instances getInstances(List<Instance> instances, long totalRecords) {
    var centralInstances = new Instances();
    centralInstances.setInstances(instances);
    centralInstances.setTotalRecords(totalRecords);
    return centralInstances;
  }
}
