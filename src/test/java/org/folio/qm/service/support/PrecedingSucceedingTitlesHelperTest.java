package org.folio.qm.service.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.folio.qm.domain.model.InstanceRecord;
import org.folio.rest.jaxrs.model.InstancePrecedingSucceedingTitle;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class PrecedingSucceedingTitlesHelperTest {

  private @Mock InstanceRecord instance;

  @Test
  void shouldUpdatePrecedingTitles() {
    var instanceId = UUID.randomUUID().toString();
    var precedingInstanceId = UUID.randomUUID().toString();
    var precedingTitle = new InstancePrecedingSucceedingTitle(
      null, precedingInstanceId, null, "Preceding Title", "hrid1", List.of(), null);

    when(instance.getId()).thenReturn(instanceId);
    when(instance.getPrecedingTitles()).thenReturn(List.of(precedingTitle));
    when(instance.getSucceedingTitles()).thenReturn(null);

    var result = PrecedingSucceedingTitlesHelper.updatePrecedingSucceedingTitles(instance);

    assertNotNull(result);
    assertEquals(1, result.getTotalRecords());
    assertEquals(1, result.getPrecedingSucceedingTitles().size());

    var updatedTitle = result.getPrecedingSucceedingTitles().getFirst();
    assertNotNull(updatedTitle.getId());
    assertEquals(precedingInstanceId, updatedTitle.getPrecedingInstanceId());
    assertEquals(instanceId, updatedTitle.getSucceedingInstanceId());
    assertEquals("Preceding Title", updatedTitle.getTitle());
  }

  @Test
  void shouldUpdateSucceedingTitles() {
    var instanceId = UUID.randomUUID().toString();
    var succeedingInstanceId = UUID.randomUUID().toString();
    var succeedingTitle = new InstancePrecedingSucceedingTitle(
      null, null, succeedingInstanceId, "Succeeding Title", "hrid2", List.of(), null);

    when(instance.getId()).thenReturn(instanceId);
    when(instance.getPrecedingTitles()).thenReturn(null);
    when(instance.getSucceedingTitles()).thenReturn(List.of(succeedingTitle));

    var result = PrecedingSucceedingTitlesHelper.updatePrecedingSucceedingTitles(instance);

    assertNotNull(result);
    assertEquals(1, result.getTotalRecords());
    assertEquals(1, result.getPrecedingSucceedingTitles().size());

    var updatedTitle = result.getPrecedingSucceedingTitles().getFirst();
    assertNotNull(updatedTitle.getId());
    assertEquals(instanceId, updatedTitle.getPrecedingInstanceId());
    assertEquals(succeedingInstanceId, updatedTitle.getSucceedingInstanceId());
    assertEquals("Succeeding Title", updatedTitle.getTitle());
  }

  @Test
  void shouldUpdateBothPrecedingAndSucceedingTitles() {
    var instanceId = UUID.randomUUID().toString();
    var precedingInstanceId = UUID.randomUUID().toString();
    var succeedingInstanceId = UUID.randomUUID().toString();

    var precedingTitle = new InstancePrecedingSucceedingTitle(
      null, precedingInstanceId, null, "Preceding Title", "hrid1", List.of(), null);
    var succeedingTitle = new InstancePrecedingSucceedingTitle(
      null, null, succeedingInstanceId, "Succeeding Title", "hrid2", List.of(), null);

    when(instance.getId()).thenReturn(instanceId);
    when(instance.getPrecedingTitles()).thenReturn(List.of(precedingTitle));
    when(instance.getSucceedingTitles()).thenReturn(List.of(succeedingTitle));

    var result = PrecedingSucceedingTitlesHelper.updatePrecedingSucceedingTitles(instance);

    assertNotNull(result);
    assertEquals(2, result.getTotalRecords());
    assertEquals(2, result.getPrecedingSucceedingTitles().size());
  }

  @Test
  void shouldReturnEmptyListWhenNoTitles() {
    var result = PrecedingSucceedingTitlesHelper.updatePrecedingSucceedingTitles(instance);

    assertNotNull(result);
    assertEquals(0, result.getTotalRecords());
    assertEquals(0, result.getPrecedingSucceedingTitles().size());
  }
}

