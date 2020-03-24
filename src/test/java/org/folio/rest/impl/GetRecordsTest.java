package org.folio.rest.impl;

import static org.folio.rest.impl.MockServer.EXISTED_ID;
import static org.folio.rest.impl.MockServer.EXISTED_INSTANCE_ID;
import static org.folio.rest.impl.MockServer.ID_DOES_NOT_EXIST;
import static org.folio.rest.impl.MockServer.ID_FOR_INTERNAL_SERVER_ERROR;
import static org.folio.util.ResourcePathResolver.SRS_RECORDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.vertx.core.http.HttpMethod;

public class GetRecordsTest extends TestBase {

  @ParameterizedTest
  @ValueSource(strings = {
    "/records-editor/records?idx=" + EXISTED_ID,
    "/records-editor/records",
    "/records-editor/records?id=" + EXISTED_ID + "&instanceId=" + EXISTED_INSTANCE_ID
  })
  void testBadQueryParameters(String url) {
    verifyGetRequest(url, 422);
    assertThat(MockServer.getRqRsEntries(HttpMethod.GET, SRS_RECORDS).entrySet(), empty());
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "/records-editor/records?id=" + ID_DOES_NOT_EXIST,
    "/records-editor/records?instanceId=" + ID_DOES_NOT_EXIST
  })
  void testNotFound(String url) {
    verifyGetRequest(url, 404);
    assertThat(MockServer.getRqRsEntries(HttpMethod.GET, SRS_RECORDS).entrySet(), hasSize(1));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "/records-editor/records?id=" + EXISTED_ID,
    "/records-editor/records?instanceId=" + EXISTED_INSTANCE_ID
  })
  void testGetById(String url) {
    verifyGetRequest(url, 200);
    assertThat(MockServer.getRqRsEntries(HttpMethod.GET, SRS_RECORDS).entrySet(), hasSize(1));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "/records-editor/records?id=" + ID_FOR_INTERNAL_SERVER_ERROR,
    "/records-editor/records?instanceId=" + ID_FOR_INTERNAL_SERVER_ERROR
  })
  void testInternalServerError(String url) {
    verifyGetRequest(url, 500);
    assertThat(MockServer.getRqRsEntries(HttpMethod.GET, SRS_RECORDS).entrySet(), hasSize(1));
  }
}
