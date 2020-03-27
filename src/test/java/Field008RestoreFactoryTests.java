import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import org.folio.converter.Field008RestoreFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.TestEntities;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static utils.TestUtils.getMockAsJson;
import static utils.TestUtils.getMockData;

public class Field008RestoreFactoryTests {
  private static final Logger logger = LoggerFactory.getLogger(Field008RestoreFactoryTests.class);

  @ParameterizedTest
  @EnumSource(TestEntities.class)
  public void testRestoreField008(TestEntities testField) throws IOException {
    logger.info("Field 008: Test restoring {} content type", testField);
    Map<String, Object> map = new ObjectMapper().readValue(getMockData(testField.getMockDataPath()), LinkedHashMap.class);
    JsonObject jsonObject = getMockAsJson(testField.getMockDataPath());
    String testString = Field008RestoreFactory.getStrategy(testField.getContentType()).restore(map);
    assertEquals(testField.getExpectedString(), testString);
  }
}
