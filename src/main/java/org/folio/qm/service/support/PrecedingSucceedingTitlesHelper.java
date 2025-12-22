package org.folio.qm.service.support;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.folio.qm.client.model.Instance;
import org.folio.rest.jaxrs.model.InstancePrecedingSucceedingTitle;
import org.folio.rest.jaxrs.model.InstancePrecedingSucceedingTitles;
import org.springframework.stereotype.Component;

@Component
public class PrecedingSucceedingTitlesHelper {

  public InstancePrecedingSucceedingTitles updatePrecedingSucceedingTitles(Instance instance) {
    List<InstancePrecedingSucceedingTitle> titlesList = new ArrayList<>();
    preparePrecedingTitles(instance, titlesList);
    prepareSucceedingTitles(instance, titlesList);
    return new InstancePrecedingSucceedingTitles(titlesList, (long) titlesList.size());
  }

  private void preparePrecedingTitles(Instance instance, List<InstancePrecedingSucceedingTitle> preparedTitles) {
    if (instance.getPrecedingTitles() != null) {
      for (InstancePrecedingSucceedingTitle parent : instance.getPrecedingTitles()) {
        var precedingSucceedingTitle = new InstancePrecedingSucceedingTitle(
          UUID.randomUUID().toString(),
          parent.getPrecedingInstanceId(),
          instance.getId(),
          parent.getTitle(),
          parent.getHrid(),
          parent.getIdentifiers(),
          parent.getMetadata());
        preparedTitles.add(precedingSucceedingTitle);
      }
    }
  }

  private void prepareSucceedingTitles(Instance instance, List<InstancePrecedingSucceedingTitle> preparedTitles) {
    if (instance.getSucceedingTitles() != null) {
      for (InstancePrecedingSucceedingTitle child : instance.getSucceedingTitles()) {
        var precedingSucceedingTitle = new InstancePrecedingSucceedingTitle(
          UUID.randomUUID().toString(),
          instance.getId(),
          child.getSucceedingInstanceId(),
          child.getTitle(),
          child.getHrid(),
          child.getIdentifiers(),
          child.getMetadata());
        preparedTitles.add(precedingSucceedingTitle);
      }
    }
  }
}
