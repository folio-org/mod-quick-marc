package org.folio.qm.service.population.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


@UnitTest
class Tag010FieldItemPopulationServiceTest {

  private Tag010FieldItemPopulationService populationService = new Tag010FieldItemPopulationService();

  @ParameterizedTest
  @MethodSource("fieldData")
  void shouldPopulateSubfieldValue(String dtoContent, String expectedContent) {
    var field = new FieldItem().tag("010").content(dtoContent);

    populationService.populate(new BaseMarcRecord().fields(List.of(field)));

    assertEquals(expectedContent, field.getContent());
  }

  private static Stream<Arguments> fieldData() {
    return Stream.of(
      arguments("$a  2001000234", "$a  2001000234"),
      arguments("$a2001000234", "$a  2001000234"),
      arguments("$a 2001000234", "$a  2001000234"),
      arguments("$aa 2002003456  ", "$aa 2002003456"),
      arguments("$a sn2003045678 ", "$asn2003045678"),
      arguments("$a 34005678", "$a   34005678 "),
      arguments("$a   34005678 /M", "$a   34005678 /M"),
      arguments("$ae  45000067 ", "$ae  45000067 "),
      arguments("$a  sn 85000678 ", "$asn 85000678 "),
      arguments("$a agr25000003  ", "$aagr25000003 "),
      arguments("$aagr25000003 /M", "$aagr25000003 /M")
    );
  }

  @Test
  void testCanProcess() {
    assertTrue(populationService.canProcess(new FieldItem().tag("010")));
  }

  @Test
  void testCannotProcess() {
    assertFalse(populationService.canProcess(new FieldItem().tag("011")));
  }
}
