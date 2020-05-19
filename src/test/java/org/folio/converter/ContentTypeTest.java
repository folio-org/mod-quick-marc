package org.folio.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ContentTypeTest {
  private static final Logger logger = LoggerFactory.getLogger(ContentTypeTest.class);

  @ParameterizedTest
  @EnumSource(TestEntities.class)
  public void testDetectContentType(TestEntities testField){
    logger.info("Test content type detection");
    testField.getLeaders().forEach(leader -> assertEquals(testField.getContentType().getName(), ContentType.resolveContentType(leader.getTypeOfRecord()).getName()));
  }

  @ParameterizedTest
  @EnumSource(TypesOfRecords.class)
  public void testResolveTypeOfRecord(TypesOfRecords typeOfRecord) {
    logger.info("Test resolve type of record");
    assertEquals(typeOfRecord.getName(), ContentType.resolveTypeOfRecord(typeOfRecord.getCode()));
  }

  @Test
  public void testGetContentTypeByUndefinedName(){
    logger.info("Test get ContentType by undefined name");
    assertEquals(ContentType.UNKNOWN, ContentType.getByName("Undefined"));
  }
}
