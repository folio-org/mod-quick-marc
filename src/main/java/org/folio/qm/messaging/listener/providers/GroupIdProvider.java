package org.folio.qm.messaging.listener.providers;

public interface GroupIdProvider {

  String diCompletedGroupId();

  String diErrorGroupId();

  String qmCompletedGroupId();
}
