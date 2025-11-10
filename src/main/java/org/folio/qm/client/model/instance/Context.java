package org.folio.qm.client.model.instance;

public interface Context {
  String getTenantId();

  String getToken();

  String getOkapiLocation();

  String getUserId();

  String getRequestId();
}
