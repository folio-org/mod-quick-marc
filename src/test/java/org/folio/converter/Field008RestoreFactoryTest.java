package org.folio.converter;

import static org.folio.converter.TestUtils.getMockData;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Field008RestoreFactoryTest {
  private static final Logger logger = LoggerFactory.getLogger(Field008RestoreFactoryTest.class);

  @ParameterizedTest
  @EnumSource(TestEntities.class)
  public void testRestoreField008(TestEntities testField) throws IOException {
    logger.info("Field 008: Test restoring {} content type", testField);
    Map<String, Object> map = new ObjectMapper().readValue(getMockData(testField.getMockDataPath()), LinkedHashMap.class);
    String testString = Field008RestoreFactory.getStrategy(testField.getContentType()).apply(map);
    assertEquals(testField.getExpectedString(), testString);
  }
}
