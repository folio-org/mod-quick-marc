package org.folio.converter;

import static org.folio.converter.TestUtils.getMockAsJson;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuickMarcToRecordConverterTest {
  private static final Logger logger = LoggerFactory.getLogger(QuickMarcToRecordConverterTest.class);

  @Test
  public void exceptionWhen008WrongLength() {
    logger.info("Testing field 008 wrong length after editing - WrongField008LengthException expected");
    QuickMarcToRecordConverter converter = new QuickMarcToRecordConverter();
    QuickMarcJson wrongFile = getMockAsJson("mockdata/quick-marc-json/quickMarcWrong008Items.json").mapTo(QuickMarcJson.class);
    assertThrows(IllegalArgumentException.class, () -> converter.convert(wrongFile));
  }
}
