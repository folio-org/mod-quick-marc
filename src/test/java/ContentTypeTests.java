import org.folio.converter.ContentType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.TestFields;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContentTypeTests {
  private static final Logger logger = LoggerFactory.getLogger(ContentTypeTests.class);

  @ParameterizedTest
  @EnumSource(TestFields.class)
  public void testDetectContentType(TestFields testField){
    logger.info("Test content type detection");
    testField.getTypes().forEach(t ->
      testField.getBlvls().forEach(b ->
        assertEquals(testField.getContentType().getName(), ContentType.detectContentType(t, b).getName())));
  }

}
