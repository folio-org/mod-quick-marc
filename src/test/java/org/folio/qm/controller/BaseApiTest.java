package org.folio.qm.controller;

import static org.folio.qm.utils.APITestUtils.TENANT_ID;
import static org.folio.qm.utils.IOTestUtils.readFile;
import static org.folio.qm.utils.JsonTestUtils.getObjectAsJson;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.JOHN_USER_ID;
import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.folio.spring.integration.XOkapiHeaders.URL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import org.folio.qm.extension.DatabaseExtension;
import org.folio.qm.extension.WireMockInitializer;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.test.web.servlet.ResultActions;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureEmbeddedDatabase
@ExtendWith(DatabaseExtension.class)
@ConfigurationPropertiesScan("org.folio.qm.util")
@ContextConfiguration(initializers = {WireMockInitializer.class})
@EmbeddedKafka(partitions = 1)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BaseApiTest {

  protected static final String DI_COMPLETE_TOPIC_NAME = "folio.Default.test.DI_COMPLETED";
  protected static final String DI_ERROR_TOPIC_NAME = "folio.Default.test.DI_ERROR";
  protected static final String QM_COMPLETE_TOPIC_NAME = "folio.Default.test.QM_COMPLETED";

  private static boolean dbInitialized = false;
  private static boolean kafkaInitialized = false;

  @Autowired
  protected WireMockServer wireMockServer;
  @Autowired
  protected FolioModuleMetadata metadata;
  @Autowired
  protected JdbcTemplate jdbcTemplate;
  @Autowired
  protected KafkaTemplate<String, String> kafkaTemplate;
  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
  @Autowired
  private EmbeddedKafkaBroker broker;
  @Value("${x-okapi-url}")
  private String okapiUrl;
  @LocalServerPort
  private Integer port;

  @BeforeEach
  void before() throws Exception {
    if (!dbInitialized) {
      postResultActions("/_/tenant",new TenantAttributes().moduleTo(""))
      .andExpect(status().isOk());

      dbInitialized = true;
    }
    if (!kafkaInitialized) {
      for (MessageListenerContainer messageListenerContainer : kafkaListenerEndpointRegistry.getListenerContainers()) {
        ContainerTestUtils.waitForAssignment(messageListenerContainer, broker.getPartitionsPerTopic());
      }
      kafkaInitialized = true;
    }
  }

  @AfterEach
  void afterEach() {
    this.wireMockServer.resetAll();
  }

  protected ResultActions getResultActions(String uri) throws Exception {
    return mockMvc.perform(get(uri)
      .headers(getHeaders())
      .contentType(APPLICATION_JSON_VALUE))
      .andDo(log());
  }

  protected ResultActions postResultActions(String uri, Object body) throws Exception {
    return mockMvc.perform(post(uri)
      .headers(getHeaders())
      .contentType(APPLICATION_JSON_VALUE)
      .content(getObjectAsJson(body)))
      .andDo(log());
  }

  protected ResultActions postResultActions(String uri, Object body, Map<String, String> headers) throws Exception {
    return mockMvc.perform(post(uri)
      .headers(getCustomHeaders(headers))
      .contentType(APPLICATION_JSON_VALUE)
      .content(getObjectAsJson(body)))
      .andDo(log());
  }

  protected ResultActions putResultActions(String uri, Object body) throws Exception {
    return mockMvc.perform(put(uri)
      .headers(defaultHeaders())
      .contentType(APPLICATION_JSON)
      .content(getObjectAsJson(body)))
      .andDo(log());
  }

  protected ResultActions putResultActions(String uri) throws Exception {
    return mockMvc.perform(put(uri)
      .headers(defaultHeaders())
      .contentType(APPLICATION_JSON)
      .content(""))
      .andDo(log());
  }

  @SneakyThrows
  protected void sendDIKafkaRecord(String eventPayloadFilePath, String topicName) {
    var jsonObject = new JSONObject();
    jsonObject.put("eventPayload", readFile(eventPayloadFilePath));
    String message = jsonObject.toString();
    sendKafkaRecord(message, topicName);
  }

  @SneakyThrows
  protected void sendQMKafkaRecord(String eventPayload, String topicName) {
    var jsonObject = new JSONObject();
    jsonObject.put("eventPayload", eventPayload);
    sendKafkaRecord(jsonObject.toString(), topicName);
  }

  protected void sendKafkaRecord(String eventPayload, String topicName) {
    ProducerRecord<String, String> record = new ProducerRecord<>(topicName, eventPayload);
    record.headers()
      .add(createKafkaHeader(TENANT, TENANT_ID))
      .add(createKafkaHeader(URL, okapiUrl));
    kafkaTemplate.send(record);
    kafkaTemplate.flush();
  }

  protected String getOkapiUrl() {
    return okapiUrl;
  }

  private RecordHeader createKafkaHeader(String headerName, String headerValue) {
    return new RecordHeader(headerName, headerValue.getBytes(StandardCharsets.UTF_8));
  }

  private HttpHeaders getHeaders() {
    final HttpHeaders httpHeaders = new HttpHeaders();

    httpHeaders.add(XOkapiHeaders.TENANT, TENANT_ID);
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
}
