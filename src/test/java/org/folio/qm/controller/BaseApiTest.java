package org.folio.qm.controller;

import static org.folio.qm.utils.APITestUtils.TENANT_ID;
import static org.folio.qm.utils.IOTestUtils.readFile;
import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.folio.spring.integration.XOkapiHeaders.URL;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import org.folio.qm.extension.DatabaseExtension;
import org.folio.qm.extension.WireMockInitializer;
import org.folio.qm.util.ZIPArchiver;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.tenant.domain.dto.TenantAttributes;


@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
@ExtendWith(DatabaseExtension.class)
@ConfigurationPropertiesScan("org.folio.qm.util")
@ContextConfiguration(initializers = {WireMockInitializer.class})
@EmbeddedKafka(partitions = 1, topics = {KafkaListenerApiTest.COMPLETE_TOPIC_NAME, KafkaListenerApiTest.ERROR_TOPIC_NAME})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BaseApiTest {

  protected static final String COMPLETE_TOPIC_NAME = "folio.Default.test.DI_COMPLETED";
  protected static final String ERROR_TOPIC_NAME = "folio.Default.test.DI_ERROR";

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
  private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
  @Autowired
  private EmbeddedKafkaBroker broker;
  @Value("${x-okapi-url}")
  private String okapiUrl;
  @LocalServerPort
  private Integer port;

  @BeforeEach
  void before() {
    if (!dbInitialized) {
      verifyPost("/_/tenant", new TenantAttributes().moduleTo(""), HttpStatus.SC_OK);
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

  protected Response verifyGet(String path, int code) {
    return RestAssured.with()
      .header(new Header(XOkapiHeaders.URL, okapiUrl))
      .header(new Header(XOkapiHeaders.TENANT, TENANT_ID))
      .get(getRequestUrl(path))
      .then()
      .statusCode(code)
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .extract()
      .response();
  }

  protected Response verifyPut(String path, Object body, int code) {
    return RestAssured.with()
      .header(new Header(XOkapiHeaders.URL, okapiUrl))
      .header(new Header(XOkapiHeaders.TENANT, TENANT_ID))
      .body(body)
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .put(getRequestUrl(path))
      .then()
      .statusCode(code)
      .contentType(StringUtils.EMPTY)
      .extract()
      .response();
  }

  protected Response verifyPost(String path, Object body, int code) {
    return RestAssured.with()
      .header(new Header(XOkapiHeaders.URL, okapiUrl))
      .header(new Header(XOkapiHeaders.TENANT, TENANT_ID))
      .body(body)
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .post(getRequestUrl(path))
      .then()
      .statusCode(code)
      .contentType(StringUtils.EMPTY)
      .extract()
      .response();
  }

  protected Response verifyPost(String path, Object body, int code, Header... headers) {
    return RestAssured.with()
      .header(new Header(XOkapiHeaders.URL, okapiUrl))
      .header(new Header(XOkapiHeaders.TENANT, TENANT_ID))
      .headers(new Headers(headers))
      .body(body)
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .post(getRequestUrl(path))
      .then()
      .statusCode(code)
      .contentType(StringUtils.EMPTY)
      .extract()
      .response();
  }

  private String getRequestUrl(String path) {
    return "http://localhost:" + port + path;
  }

  protected void sendKafkaRecord(String eventPayload, String topicName) throws IOException {
    String message = String.format("{"
      + "\"eventPayload\": \"%s\""
      + "}", ZIPArchiver.zip(readFile(eventPayload)));
    ProducerRecord<String, String> record = new ProducerRecord<>(topicName, message);
    record.headers()
      .add(createKafkaHeader(TENANT, TENANT_ID))
      .add(createKafkaHeader(URL, okapiUrl));
    kafkaTemplate.send(record);
    kafkaTemplate.flush();
  }

  private RecordHeader createKafkaHeader(String headerName, String headerValue) {
    return new RecordHeader(headerName, headerValue.getBytes(StandardCharsets.UTF_8));
  }
}
