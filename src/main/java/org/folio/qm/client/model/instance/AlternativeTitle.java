package org.folio.qm.client.model.instance;

import io.vertx.core.json.JsonObject;

public class AlternativeTitle extends Authorized {
  public static final String ALTERNATIVE_TITLE_TYPE_ID_KEY = "alternativeTitleTypeId";
  public static final String ALTERNATIVE_TITLE_KEY = "alternativeTitle";

  public final String alternativeTitleTypeId;
  public final String alternativeTitle;

  public AlternativeTitle(String alternativeTitleTypeId, String alternativeTitle, String authorityId) {
    super(authorityId);
    this.alternativeTitleTypeId = alternativeTitleTypeId;
    this.alternativeTitle = alternativeTitle;
  }

  public AlternativeTitle(JsonObject json) {
    this(json.getString(ALTERNATIVE_TITLE_TYPE_ID_KEY), json.getString(ALTERNATIVE_TITLE_KEY),
      json.getString(AUTHORITY_ID_KEY));
  }
}
