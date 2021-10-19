package org.folio.qm.messaging.listener.providers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.folio.qm.messaging.domain.QmEventTypes;
import org.folio.rest.jaxrs.model.DataImportEventTypes;

@Component("groupIdProvider")
@RequiredArgsConstructor
public class GroupIdProviderImpl implements GroupIdProvider {

  private static final String GROUP_ID_TEMPLATE = "%s.%s";
  private static final String GROUP_PREFIX = "QUICK_MARC";

  @Override
  public String diCompletedGroupId() {
    return getGroupId(DataImportEventTypes.DI_COMPLETED.name());
  }

  @Override
  public String diErrorGroupId() {
    return getGroupId(DataImportEventTypes.DI_ERROR.name());
  }

  @Override
  public String qmCompletedGroupId() {
    return getGroupId(QmEventTypes.QM_COMPLETED.name());
  }

  private String getGroupId(String eventName) {
    return String.format(GROUP_ID_TEMPLATE, eventName, GROUP_PREFIX);
  }
}
