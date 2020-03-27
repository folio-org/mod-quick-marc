import org.folio.converter.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.TestEntities;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContentTypeTests {
  private static final Logger logger = LoggerFactory.getLogger(ContentTypeTests.class);

  @ParameterizedTest
  @EnumSource(TestEntities.class)
  public void testDetectContentType(TestEntities testField){
    logger.info("Test content type detection");
    testField.getTypes().forEach(t ->
      testField.getBlvls().forEach(b ->
        assertEquals(testField.getContentType().getName(), ContentType.resolveContentType(t, b).getName())));
  }

  @Test
  public void testGetContentTypeByUndefinedName(){
    logger.info("Test get ContentType by undefined name");
    assertEquals(ContentType.UNKNOWN, ContentType.getByName("Undefined"));
  }
}
