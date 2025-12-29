package org.folio.qm.service.support;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.folio.qm.domain.model.InstanceRecord;
import org.folio.rest.jaxrs.model.InstancePrecedingSucceedingTitle;
import org.folio.rest.jaxrs.model.InstancePrecedingSucceedingTitles;

@UtilityClass
public class PrecedingSucceedingTitlesHelper {

  public static InstancePrecedingSucceedingTitles updatePrecedingSucceedingTitles(InstanceRecord instance) {
    List<InstancePrecedingSucceedingTitle> titlesList = new ArrayList<>();
    preparePrecedingTitles(instance, titlesList);
    prepareSucceedingTitles(instance, titlesList);
    return new InstancePrecedingSucceedingTitles(titlesList, (long) titlesList.size());
  }

  private static void preparePrecedingTitles(InstanceRecord instance,
                                             List<InstancePrecedingSucceedingTitle> preparedTitles) {
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

  private static void prepareSucceedingTitles(InstanceRecord instance,
                                              List<InstancePrecedingSucceedingTitle> preparedTitles) {
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
