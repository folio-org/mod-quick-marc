import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static utils.TestUtils.getMockAsJson;

import io.vertx.core.json.JsonObject;
import org.folio.converter.Field008RestoreFactory;
import org.folio.converter.QuickMarcToRecordConverter;
import org.folio.converter.RecordToQuickMarcConverter;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.Record;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.TestFields;

public class QuickMarcToRecordConverterTest {
  private static final Logger logger = LoggerFactory.getLogger(QuickMarcToRecordConverterTest.class);

  @ParameterizedTest
  @EnumSource(TestFields.class)
  public void testRestoreField008(TestFields testField) {
    logger.info("Field 008: Test restoring {} content type", testField);
    JsonObject jsonObject = getMockAsJson(testField.getMockDataPath());
    String testString = Field008RestoreFactory.getStrategy(testField.getContentType()).restore(jsonObject);
    assertEquals(testField.getExpectedString(), testString);
  }

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
    logger.info("Testing field 008 wrong length after editing - RuntimeException expected");
    QuickMarcToRecordConverter converter = new QuickMarcToRecordConverter();
    QuickMarcJson wrongFile = getMockAsJson("mockData/quickMarcWrong008Items.json").mapTo(QuickMarcJson.class);
    assertThrows(RuntimeException.class, () -> converter.convert(wrongFile));
  }
}
