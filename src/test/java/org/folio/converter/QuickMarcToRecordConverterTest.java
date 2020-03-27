import static org.junit.jupiter.api.Assertions.assertThrows;
import static utils.TestUtils.getMockAsJson;

import org.folio.converter.QuickMarcToRecordConverter;
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
    QuickMarcJson wrongFile = getMockAsJson("mockdata/quickMarcWrong008Items.json").mapTo(QuickMarcJson.class);
    assertThrows(IllegalArgumentException.class, () -> converter.convert(wrongFile));
  }
}
