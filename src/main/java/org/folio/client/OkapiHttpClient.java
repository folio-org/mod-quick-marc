package org.folio.client;

import static org.folio.util.ErrorUtils.buildGenericError;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import io.vertx.ext.web.client.WebClientOptions;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.folio.exception.HttpException;
import org.folio.rest.tools.client.HttpClientFactory;
import org.folio.rest.tools.client.Response;
import org.folio.rest.tools.client.interfaces.HttpClientInterface;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;

public class OkapiHttpClient implements HttpClient {

  public static final String OKAPI_URL = "x-okapi-url";
  private static final String OPTIONS = "options";
  private final HttpClientInterface client;

  private OkapiHttpClient() {
    this.client = HttpClientFactory.getHttpClient(StringUtils.EMPTY, StringUtils.EMPTY);
  }

  public static OkapiHttpClient getInstance() {
    return Holder.instance;
  }

  @Override
  public CompletableFuture<Response> request(HttpMethod method, String endpoint, Map<String, String> okapiHeaders) {
    try {
      setOkapiUrl(client, okapiHeaders);
      return client.request(method, endpoint, okapiHeaders);
    } catch(Exception e) {
      throw new HttpException(buildGenericError());
    }
  }

  @Override
  public CompletableFuture<Response> request(HttpMethod method, Buffer body, String endpoint, Map<String, String> okapiHeaders) {
    try {
      setOkapiUrl(client, okapiHeaders);
      return client.request(method, body, endpoint, okapiHeaders);
    } catch(Exception e) {
      throw new HttpException(buildGenericError());
    }
  }

  private void setOkapiUrl(HttpClientInterface client, Map<String, String> okapiHeaders) throws IllegalAccessException {
    final String okapiURL = okapiHeaders.getOrDefault(OKAPI_URL, StringUtils.EMPTY);
    WebClientOptions options = (WebClientOptions) FieldUtils.readDeclaredField(client, OPTIONS, true);
    options.setDefaultHost(okapiURL);
  }

  private static class Holder {
    private static final OkapiHttpClient instance = new OkapiHttpClient();
  }
}
