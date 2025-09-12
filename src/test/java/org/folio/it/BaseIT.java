package org.folio.it;

import static java.util.Objects.requireNonNull;
import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.folio.spring.integration.XOkapiHeaders.URL;
import static org.folio.support.utils.ApiTestUtils.JOHN_USER_ID_HEADER;
import static org.folio.support.utils.ApiTestUtils.TENANT_ID;
import static org.folio.support.utils.InputOutputTestUtils.readFile;
import static org.folio.support.utils.JsonTestUtils.getObjectAsJson;
import static org.folio.support.utils.TestEntitiesUtils.JOHN_USER_ID;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.folio.qm.ModQuickMarcApplication;
import org.folio.rspec.domain.dto.SpecificationUpdatedEvent;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.testing.extension.EnableKafka;
import org.folio.spring.testing.extension.EnableOkapi;
import org.folio.spring.testing.extension.EnablePostgres;
import org.folio.spring.testing.extension.impl.OkapiConfiguration;
import org.folio.support.DisplayNameLogger;
import org.folio.tenant.domain.dto.TenantAttributes;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@EnableOkapi
@EnableKafka
@EnablePostgres
@SpringBootTest(classes = ModQuickMarcApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(DisplayNameLogger.class)
@SuppressWarnings("java:S5786")
public class BaseIT {

  protected static final String DI_COMPLETE_TOPIC_NAME = "folio.Default.test.DI_COMPLETED";
  protected static final String DI_ERROR_TOPIC_NAME = "folio.Default.test.DI_ERROR";
  protected static final String QM_COMPLETE_TOPIC_NAME = "folio.Default.test.QM_COMPLETED";
  protected static final String SPECIFICATION_COMPLETE_TOPIC_NAME =
    "folio.test.specification-storage.specification.updated";

  protected static OkapiConfiguration okapiConfiguration;
  private static boolean dbInitialized = false;

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
  @Autowired
  private ObjectMapper objectMapper;
  protected final WireMockServer wireMockServer = okapiConfiguration.wireMockServer();

  @Value("${folio.okapi-url}")
  private String okapiUrl;

  @BeforeEach
  void before() throws Exception {
    if (!dbInitialized) {
      var body = new TenantAttributes().moduleTo("mod-quick-marc");
      doPost("/_/tenant", body, getHeaders().toSingleValueMap())
        .andExpect(status().isNoContent());

      dbInitialized = true;
    }
    cacheManager.getCacheNames().forEach(name -> requireNonNull(cacheManager.getCache(name)).clear());
  }

  @AfterEach
  void afterEach() {
    this.wireMockServer.resetAll();
  }

  protected ResultActions doGet(String uri) throws Exception {
    return mockMvc.perform(get(uri)
        .headers(getHeaders())
        .contentType(APPLICATION_JSON_VALUE))
      .andDo(log());
  }

  protected ResultActions doPost(String uri, Object body) throws Exception {
    return doPost(uri, body, JOHN_USER_ID_HEADER);
  }

  protected ResultActions doPost(String uri, Object body, Map<String, String> headers) throws Exception {
    return mockMvc.perform(post(uri)
        .headers(getCustomHeaders(headers))
        .contentType(APPLICATION_JSON_VALUE)
        .content(getObjectAsJson(body)))
      .andDo(log());
  }

  protected ResultActions doPut(String uri, Object body) throws Exception {
    return mockMvc.perform(put(uri)
        .headers(defaultHeaders())
        .contentType(APPLICATION_JSON)
        .content(getObjectAsJson(body)))
      .andDo(log());
  }

  protected ResultActions doPut(String uri) throws Exception {
    return mockMvc.perform(put(uri)
        .headers(defaultHeaders())
        .contentType(APPLICATION_JSON)
        .content(""))
      .andDo(log());
  }

  @SneakyThrows
  protected void sendDataImportKafkaRecord(String eventPayloadFilePath, String topicName) {
    var jsonObject = new JSONObject();
    jsonObject.put("eventPayload", readFile(eventPayloadFilePath));
    String message = jsonObject.toString();
    sendKafkaRecord(message, topicName);
  }

  @SneakyThrows
  protected void sendQuickMarcKafkaRecord(String eventPayload) {
    var jsonObject = new JSONObject();
    jsonObject.put("eventPayload", eventPayload);
    sendKafkaRecord(jsonObject.toString(), BaseIT.QM_COMPLETE_TOPIC_NAME);
  }

  @SneakyThrows
  protected void sendSpecificationKafkaRecord(SpecificationUpdatedEvent eventPayload) {
    var payload = objectMapper.writeValueAsString(eventPayload);
    sendKafkaRecord(payload, BaseIT.SPECIFICATION_COMPLETE_TOPIC_NAME);
  }

  protected void sendKafkaRecord(String eventPayload, String topicName) {
    ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, eventPayload);
    producerRecord.headers()
      .add(createKafkaHeader(TENANT, TENANT_ID))
      .add(createKafkaHeader(URL, okapiUrl));
    kafkaTemplate.send(producerRecord);
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
    httpHeaders.add(XOkapiHeaders.USER_ID, JOHN_USER_ID);
    httpHeaders.add(XOkapiHeaders.URL, getOkapiUrl());

    return httpHeaders;
  }

  private HttpHeaders getCustomHeaders(Map<String, String> headers) {
    final HttpHeaders httpHeaders = getHeaders();

    headers.forEach(httpHeaders::set);

    return httpHeaders;
  }

  private HttpHeaders defaultHeaders() {
    final HttpHeaders httpHeaders = getHeaders();

    httpHeaders.setContentType(APPLICATION_JSON);
    httpHeaders.add(XOkapiHeaders.USER_ID, JOHN_USER_ID);

    return httpHeaders;
  }
}
