package org.folio.qm.messaging.listener.providers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.folio.qm.messaging.domain.QmEventTypes;
import org.folio.rest.jaxrs.model.DataImportEventTypes;
import org.folio.spring.FolioModuleMetadata;

@Component("groupIdProvider")
@RequiredArgsConstructor
public class GroupIdProviderImpl implements GroupIdProvider {

  private static final String GROUP_ID_TEMPLATE = "%s.%s";

  private final FolioModuleMetadata metadata;

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
    return String.format(GROUP_ID_TEMPLATE, eventName, metadata.getModuleName());
  }
}
