package org.folio.qm.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import lombok.experimental.UtilityClass;

import org.folio.spring.DefaultFolioExecutionContext;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.integration.XOkapiHeaders;

@UtilityClass
public class TenantUtils {

  public static FolioExecutionContext getFolioExecutionContextCopyForTenant(FolioExecutionContext context,
                                                                            String tenant) {
    var headers = context.getAllHeaders() != null
                  ? context.getAllHeaders()
                  : new HashMap<String, Collection<String>>();
    headers.put(XOkapiHeaders.TENANT, Collections.singletonList(tenant));

    return new DefaultFolioExecutionContext(context.getFolioModuleMetadata(), headers);
  }

}
