package org.folio.qm.util;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.experimental.UtilityClass;
import org.springframework.messaging.MessageHeaders;

import org.folio.qm.domain.dto.DataImportEventPayload;
import org.folio.spring.DefaultFolioExecutionContext;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;

@UtilityClass
public class TenantContextUtils {

  public static FolioExecutionContext getFolioExecutionContextCopyForTenant(FolioExecutionContext context,
                                                                            String tenant) {
    var headers = context.getAllHeaders() != null
                  ? context.getAllHeaders()
                  : new HashMap<String, Collection<String>>();
    headers.put(XOkapiHeaders.TENANT, Collections.singletonList(tenant));

    return new DefaultFolioExecutionContext(context.getFolioModuleMetadata(), headers);
  }

  public static FolioExecutionContext getFolioExecutionContextFromDIEvent(DataImportEventPayload data,
                                                                          MessageHeaders headers,
                                                                          FolioModuleMetadata moduleMetadata) {
    return getContextFromKafkaHeaders(data.getTenant(), data.getOkapiUrl(), data.getToken(), headers, moduleMetadata);
  }

  private static FolioExecutionContext getContextFromKafkaHeaders(String tenantDefault, String urlDefault,
                                                                  String tokenDefault, MessageHeaders headers,
                                                                  FolioModuleMetadata moduleMetadata) {
    Map<String, Collection<String>> map = new HashMap<>();
    map.put(XOkapiHeaders.TENANT, getHeaderValue(headers, XOkapiHeaders.TENANT, tenantDefault));
    map.put(XOkapiHeaders.URL, getHeaderValue(headers, XOkapiHeaders.URL, urlDefault));
    map.put(XOkapiHeaders.TOKEN, getHeaderValue(headers, XOkapiHeaders.TOKEN, tokenDefault));
    map.put(XOkapiHeaders.USER_ID, getHeaderValue(headers, XOkapiHeaders.USER_ID, null));

    return new DefaultFolioExecutionContext(moduleMetadata, map);
  }

  private static List<String> getHeaderValue(MessageHeaders headers, String headerName, String defaultValue) {
    var headerValue = headers.get(headerName);
    var value = headerValue == null
                ? defaultValue
                : new String((byte[]) headerValue, StandardCharsets.UTF_8);
    return value == null ? Collections.emptyList() : Collections.singletonList(value);
  }
}
