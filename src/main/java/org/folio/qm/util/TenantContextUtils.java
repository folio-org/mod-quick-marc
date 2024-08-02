package org.folio.qm.util;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.folio.qm.domain.dto.DataImportEventPayload;
import org.folio.spring.DefaultFolioExecutionContext;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.springframework.messaging.MessageHeaders;

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

  public static FolioExecutionContext getFolioExecutionContextFromDataImportEvent(DataImportEventPayload data,
                                                                                  MessageHeaders headers,
                                                                                  FolioModuleMetadata moduleMetadata) {
    return getContextFromKafkaHeaders(data.getTenant(), data.getOkapiUrl(), data.getToken(), headers, moduleMetadata);
  }

  public static FolioExecutionContext getFolioExecutionContextFromQuickMarcEvent(MessageHeaders headers,
                                                                                 FolioModuleMetadata moduleMetadata) {
    return getContextFromKafkaHeaders(null, null, null, headers, moduleMetadata);
  }

  public static FolioExecutionContext getFolioExecutionContextFromSpecification(MessageHeaders headers, String tenantId,
                                                               FolioModuleMetadata moduleMetadata) {
    return getContextFromKafkaHeaders(tenantId, null, null, headers, moduleMetadata);
  }

  public static void runInFolioContext(FolioExecutionContext context, Runnable runnable) {
    try (var fec = new FolioExecutionContextSetter(context)) {
      runnable.run();
    }
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
