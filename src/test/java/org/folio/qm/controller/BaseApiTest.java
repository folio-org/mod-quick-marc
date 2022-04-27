package org.folio.qm.controller;

import static java.util.Objects.requireNonNull;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.folio.qm.support.utils.APITestUtils.TENANT_ID;
import static org.folio.qm.support.utils.DBTestUtils.getCreationStatusById;
import static org.folio.qm.support.utils.IOTestUtils.readFile;
import static org.folio.qm.support.utils.JsonTestUtils.getObjectAsJson;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.JOHN_USER_ID;
import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.folio.spring.integration.XOkapiHeaders.URL;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import org.folio.qm.domain.entity.ActionStatusEnum;
import org.folio.qm.support.extension.EnableKafka;
import org.folio.qm.support.extension.EnablePostgres;
import org.folio.qm.support.extension.impl.DatabaseCleanupExtension;
import org.folio.qm.support.extension.impl.WireMockInitializer;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.tenant.domain.dto.TenantAttributes;

@EnableKafka
@EnablePostgres
@ExtendWith(DatabaseCleanupExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = {WireMockInitializer.class})
class BaseApiTest {

  protected static final String DI_COMPLETE_TOPIC_NAME = "folio.Default.test.DI_COMPLETED";
  protected static final String DI_ERROR_TOPIC_NAME = "folio.Default.test.DI_ERROR";

  private static boolean dbInitialized = false;

  @Autowired
  protected WireMockServer mockServer;
  @Autowired
  protected FolioModuleMetadata metadata;
  @Autowired
  protected JdbcTemplate jdbcTemplate;
  @Autowired
  protected KafkaTemplate<String, String> kafkaTemplate;
  @Autowired
  protected MockMvc mockMvc;
  @Autowired
  private CacheManager cacheManager;

  @Value("${x-okapi-url}")
  private String okapiUrl;

  @BeforeEach
  void before() throws Exception {
    if (!dbInitialized) {
      var body = new TenantAttributes().moduleTo("mod-quick-marc");
      performPost("/_/tenant", body, getHeaders().toSingleValueMap())
        .andExpect(status().isNoContent());

      dbInitialized = true;
    }
    cacheManager.getCacheNames().forEach(name -> requireNonNull(cacheManager.getCache(name)).clear());
  }

  @AfterEach
  void afterEach() {
    this.mockServer.resetAll();
  }

  private RecordHeader createKafkaHeader(String headerName, String headerValue) {
    return new RecordHeader(headerName, headerValue.getBytes(StandardCharsets.UTF_8));
  }

  private HttpHeaders getHeaders() {
    final HttpHeaders httpHeaders = new HttpHeaders();

    httpHeaders.add(XOkapiHeaders.TENANT, TENANT_ID);
    httpHeaders.add(XOkapiHeaders.USER_ID, JOHN_USER_ID);
    httpHeaders.add(XOkapiHeaders.URL, getOkapiUrl());

    return httpHeaders;
  }

  private HttpHeaders getCustomHeaders(Map<String, String> headers) {
    final HttpHeaders httpHeaders = getHeaders();

    headers.forEach(httpHeaders::add);

    return httpHeaders;
  }

  private HttpHeaders defaultHeaders() {
    final HttpHeaders httpHeaders = getHeaders();

    httpHeaders.setContentType(APPLICATION_JSON);
    httpHeaders.add(XOkapiHeaders.USER_ID, JOHN_USER_ID);

    return httpHeaders;
  }

  @SneakyThrows
  protected ResultActions performGet(String uri) {
    return mockMvc.perform(get(uri)
        .headers(getHeaders())
        .contentType(APPLICATION_JSON_VALUE))
      .andDo(log());
  }

  @SneakyThrows
  protected ResultActions performPost(String uri, Object body, Map<String, String> headers) {
    return mockMvc.perform(post(uri)
        .headers(getCustomHeaders(headers))
        .contentType(APPLICATION_JSON_VALUE)
        .content(getObjectAsJson(body)))
      .andDo(log());
  }

  @SneakyThrows
  protected ResultActions performPut(String uri, Object body) {
    return mockMvc.perform(put(uri)
        .headers(defaultHeaders())
        .contentType(APPLICATION_JSON)
        .content(getObjectAsJson(body)))
      .andDo(log());
  }

  @SneakyThrows
  protected ResultActions performDelete(String uri) {
    return mockMvc.perform(delete(uri)
        .headers(defaultHeaders())
        .contentType(APPLICATION_JSON)
        .content(""))
      .andDo(log());
  }

  @SneakyThrows
  protected void sendDIKafkaRecord(String eventPayloadFilePath, String topicName) {
    var jsonObject = new JSONObject();
    jsonObject.put("eventPayload", readFile(eventPayloadFilePath));
    var message = jsonObject.toString();
    sendKafkaRecord(message, topicName);
  }

  protected void sendKafkaRecord(String eventPayload, String topicName) {
    ProducerRecord<String, String> record = new ProducerRecord<>(topicName, eventPayload);
    record.headers()
      .add(createKafkaHeader(TENANT, TENANT_ID))
      .add(createKafkaHeader(URL, okapiUrl));
    kafkaTemplate.send(record);
    kafkaTemplate.flush();
  }

  protected void sendEventAndWaitStatusChange(UUID actionId, ActionStatusEnum status, String diCompleteTopicName,
                                              String payloadFilePath) {
    sendDIKafkaRecord(payloadFilePath, diCompleteTopicName);

    await().atMost(Duration.ofSeconds(20))
      .untilAsserted(() -> Assertions.assertThat(getCreationStatusById(actionId, metadata, jdbcTemplate).getStatus())
        .isEqualTo(status)
      );
  }

  protected String getOkapiUrl() {
    return okapiUrl;
  }

  @NotNull
  protected ResultMatcher errorHasMessage(String expectedMessage) {
    return jsonPath("$.message").value(containsString(expectedMessage));
  }

  protected ResultMatcher errorMessageMatch(Matcher<String> errorMessageMatcher) {
    return jsonPath("$.message", errorMessageMatcher);
  }

}
