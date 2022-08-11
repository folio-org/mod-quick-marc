package org.folio.qm.support.utils;

import static org.folio.qm.converter.elements.Constants.DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD;
import static org.folio.qm.util.MarcUtils.decodeFromMarcDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.node.ArrayNode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.skyscreamer.jsonassert.JSONAssert;

@UtilityClass
public class AssertionUtils {

  public static void verifyDateTimeUpdating(ParsedRecordDto parsedRecordDto) {
    var jsonNode = JsonTestUtils.getObjectAsJsonNode(parsedRecordDto.getParsedRecord().getContent());
    var entriesJsonArray = (ArrayNode) jsonNode.withArray("fields");

    String value = entriesJsonArray.findValue(DATE_AND_TIME_OF_LATEST_TRANSACTION_FIELD).asText();

    LocalDateTime actual = decodeFromMarcDateTime(value);

    // Compare the values of up to a minutes to prevent test failure in case of any execution delays
    assertEquals(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES), actual.truncatedTo(ChronoUnit.MINUTES));
  }

  @SneakyThrows
  public static void mockIsEqualToObject(String mockPath, Object object) {
    JSONAssert.assertEquals(InputOutputTestUtils.readFile(mockPath), JsonTestUtils.getObjectAsJson(object), true);
  }
}
