package org.folio.converter;

import static org.folio.converter.QuickMarcToParsedRecordDtoConverterTest.TESTED_TAG_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.rest.jaxrs.model.Field;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.AdditionalInfo;
import org.folio.srs.model.ExternalIdsHolder;
import org.folio.srs.model.Metadata;
import org.folio.srs.model.ParsedRecord;
import org.folio.srs.model.ParsedRecordDto;

import io.vertx.core.json.JsonObject;

public class TestUtils {

  public static final String VALID_PARSED_RECORD_DTO_ID = "c56b70ce-4ef6-47ef-8bc3-c470bafa0b8c";
  public static final String EXISTED_INSTANCE_ID = "b9a5f035-de63-4e2c-92c2-07240c89b817";
  public static final String VALID_PARSED_RECORD_ID = "c9db5d7a-e1d4-11e8-9f32-f2801f1b9fd1";
  private static final Logger logger = LogManager.getLogger(TestUtils.class);

  public static JsonObject getMockAsJson(String fullPath) {
    try {
      return new JsonObject(getMockData(fullPath));
    } catch (IOException e) {
      logger.error(e.toString());
    }
    return new JsonObject();
  }

  public static String getMockData(String path) throws IOException {
    logger.info("Using mock datafile: {}", path);
    try (InputStream resourceAsStream = TestUtils.class.getClassLoader()
      .getResourceAsStream(path)) {
      if (resourceAsStream != null) {
        return IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
      } else {
        StringBuilder sb = new StringBuilder();
        try (Stream<String> lines = Files.lines(Paths.get(path))) {
          lines.forEach(sb::append);
        }
        return sb.toString();
      }
    }
  }

  public static Field getFieldWithIndicators(List<Object> indicators) {
    return new Field().withTag(TESTED_TAG_NAME)
      .withContent("$333 content")
      .withIndicators(indicators);
  }

  public static QuickMarcJson getQuickMarcJsonWithMinContent(Field... fields) {
    return new QuickMarcJson().withLeader("01542ccm a2200361   4500")
      .withFields(Arrays.asList(fields));
  }

  public static ParsedRecordDto getParsedRecordDtoWithMinContent(ParsedRecord parsedRecord) {
    return new ParsedRecordDto().withId(VALID_PARSED_RECORD_DTO_ID)
      .withExternalIdsHolder(new ExternalIdsHolder().withInstanceId(EXISTED_INSTANCE_ID))
      .withAdditionalInfo(new AdditionalInfo().withSuppressDiscovery(false))
      .withRecordType(ParsedRecordDto.RecordType.MARC)
      .withRecordState(ParsedRecordDto.RecordState.ACTUAL)
      .withMetadata(new Metadata().withUpdatedDate(new Date(1594901616879L)))
      .withParsedRecord(parsedRecord);
  }
}
