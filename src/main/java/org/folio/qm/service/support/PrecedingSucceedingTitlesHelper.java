package org.folio.qm.service.support;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.folio.qm.client.model.Instance;
import org.folio.qm.client.model.PrecedingSucceedingTitle;
import org.folio.qm.client.model.PrecedingSucceedingTitleCollection;
import org.springframework.stereotype.Component;

@Component
public class PrecedingSucceedingTitlesHelper {

  public PrecedingSucceedingTitleCollection updatePrecedingSucceedingTitles(Instance instance) {
    List<PrecedingSucceedingTitle> titlesList = new ArrayList<>();
    preparePrecedingTitles(instance, titlesList);
    prepareSucceedingTitles(instance, titlesList);
    return new PrecedingSucceedingTitleCollection(titlesList, titlesList.size());
  }

  private void preparePrecedingTitles(Instance instance, List<PrecedingSucceedingTitle> preparedTitles) {
    if (instance.getPrecedingTitles() != null) {
      for (PrecedingSucceedingTitle parent : instance.getPrecedingTitles()) {
        var precedingSucceedingTitle = new PrecedingSucceedingTitle(
          UUID.randomUUID().toString(),
          parent.getPrecedingInstanceId(),
          instance.getId(),
          parent.getTitle(),
          parent.getHrid(),
          parent.getIdentifiers());
        preparedTitles.add(precedingSucceedingTitle);
      }
    }
  }

  private void prepareSucceedingTitles(Instance instance, List<PrecedingSucceedingTitle> preparedTitles) {
    if (instance.getSucceedingTitles() != null) {
      for (PrecedingSucceedingTitle child : instance.getSucceedingTitles()) {
        var precedingSucceedingTitle = new PrecedingSucceedingTitle(
          UUID.randomUUID().toString(),
          instance.getId(),
          child.getSucceedingInstanceId(),
          child.getTitle(),
          child.getHrid(),
          child.getIdentifiers());
        preparedTitles.add(precedingSucceedingTitle);
      }
    }
  }
}
