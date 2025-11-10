package org.folio.qm.service.support;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.folio.qm.client.model.instance.Instance;
import org.folio.qm.client.model.instance.PrecedingSucceedingTitle;
import org.folio.qm.client.model.instance.PrecedingSucceedingTitleCollection;
import org.springframework.stereotype.Component;

@Component
public class PrecedingSucceedingTitlesHelper {

  public PrecedingSucceedingTitleCollection updatePrecedingSucceedingTitles(Instance instance) {
    List<PrecedingSucceedingTitle> titlesList = new ArrayList<>();
    preparePrecedingTitles(instance, titlesList);
    prepareSucceedingTitles(instance, titlesList);
    return new PrecedingSucceedingTitleCollection(titlesList, titlesList.size());
  }

  public PrecedingSucceedingTitleCollection createPrecedingSucceedingTitles(Instance instance) {
    List<PrecedingSucceedingTitle> precedingSucceedingTitles = new ArrayList<>();
    preparePrecedingTitles(instance, precedingSucceedingTitles);
    prepareSucceedingTitles(instance, precedingSucceedingTitles);
    return new PrecedingSucceedingTitleCollection(precedingSucceedingTitles, precedingSucceedingTitles.size());
  }

  private void preparePrecedingTitles(Instance instance, List<PrecedingSucceedingTitle> preparedTitles) {
    if (instance.getPrecedingTitles() != null) {
      for (PrecedingSucceedingTitle parent : instance.getPrecedingTitles()) {
        PrecedingSucceedingTitle precedingSucceedingTitle = new PrecedingSucceedingTitle(
          UUID.randomUUID().toString(),
          parent.precedingInstanceId,
          instance.getId(),
          parent.title,
          parent.hrid,
          parent.identifiers);
        preparedTitles.add(precedingSucceedingTitle);
      }
    }
  }

  private void prepareSucceedingTitles(Instance instance, List<PrecedingSucceedingTitle> preparedTitles) {
    if (instance.getSucceedingTitles() != null) {
      for (PrecedingSucceedingTitle child : instance.getSucceedingTitles()) {
        PrecedingSucceedingTitle precedingSucceedingTitle = new PrecedingSucceedingTitle(
          UUID.randomUUID().toString(),
          instance.getId(),
          child.succeedingInstanceId,
          child.title,
          child.hrid,
          child.identifiers);
        preparedTitles.add(precedingSucceedingTitle);
      }
    }
  }
}
