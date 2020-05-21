package org.folio.converter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.folio.rest.jaxrs.model.Field;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

import static org.folio.converter.QuickMarcToParsedRecordConverterTest.TESTED_TAG_NAME;

public class TestUtils {
  private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);

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
    return new Field()
      .withTag(TESTED_TAG_NAME)
      .withContent("$333 content")
      .withIndicators(indicators);
  }

  public static QuickMarcJson getQuickMarcJsonWithMinContent(Field... fields) {
    return new QuickMarcJson()
      .withLeader("01542ccm a2200361   4500")
      .withFields(Arrays.asList(fields));
  }
}
