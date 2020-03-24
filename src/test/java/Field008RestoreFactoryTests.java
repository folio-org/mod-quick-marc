import io.vertx.core.json.JsonObject;
import org.folio.converter.Field008RestoreFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.TestFields;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static utils.TestUtils.getMockAsJson;

public class Field008RestoreFactoryTests {
  private static final Logger logger = LoggerFactory.getLogger(Field008RestoreFactoryTests.class);

  @ParameterizedTest
  @EnumSource(TestFields.class)
  public void testRestoreField008(TestFields testField) {
    logger.info("Field 008: Test restoring {} content type", testField);
    JsonObject jsonObject = getMockAsJson(testField.getMockDataPath());
    String testString = Field008RestoreFactory.getStrategy(testField.getContentType()).restore(jsonObject);
    assertEquals(testField.getExpectedString(), testString);
  }
}
