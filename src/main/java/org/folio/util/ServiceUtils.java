package org.folio.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionException;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ServiceUtils {

  private ServiceUtils() {}

  private static final Logger logger = LoggerFactory.getLogger(ServiceUtils.class);

  public static String buildQuery(String query) {
    return "?query=" + encodeQuery(query);
  }

  private static String encodeQuery(String query) {
    try {
      return URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      logger.error("Error happened while attempting to encode query: '{}'", e, query);
      throw new CompletionException(e);
    }
  }
}
