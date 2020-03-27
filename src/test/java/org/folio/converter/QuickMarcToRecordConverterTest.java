package org.folio.converter;

import static org.folio.converter.TestUtils.getMockAsJson;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.Record;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuickMarcToRecordConverterTest {
  private static final Logger logger = LoggerFactory.getLogger(QuickMarcToRecordConverterTest.class);

  @Test
  public void testRecordsAreEqual(){
    logger.info("Source record and converted/restored one should be equal");
    QuickMarcToRecordConverter quickMarcToRecordConverter = new QuickMarcToRecordConverter();
    RecordToQuickMarcConverter recordToQuickMarcConverter = new RecordToQuickMarcConverter();
    Record sourceRecord = getMockAsJson("mockdata/record.json").mapTo(Record.class);
    String sourceRawRecord = sourceRecord.getRawRecord().getContent();
    QuickMarcJson quickMarcJson = recordToQuickMarcConverter.convert(sourceRecord);
    Record convertedRecord = quickMarcToRecordConverter.convert(quickMarcJson);
    String convertedRawRecord = convertedRecord.getRawRecord().getContent();
    assertEquals(sourceRawRecord, convertedRawRecord);
  }

  @Test
  public void exceptionWhen008WrongLength() {
    logger.info("Testing field 008 wrong length after editing - WrongField008LengthException expected");
    QuickMarcToRecordConverter converter = new QuickMarcToRecordConverter();
    QuickMarcJson wrongFile = getMockAsJson("mockdata/quickMarcWrong008Items.json").mapTo(QuickMarcJson.class);
    assertThrows(IllegalArgumentException.class, () -> converter.convert(wrongFile));
  }
}
