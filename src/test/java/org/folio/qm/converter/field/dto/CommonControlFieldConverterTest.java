package org.folio.qm.converter.field.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.impl.ControlFieldImpl;

@UnitTest
class CommonControlFieldConverterTest {

  private final CommonControlFieldConverter converter = new CommonControlFieldConverter();

  private static Stream<Arguments> fieldData() {
    return Stream.of(
      arguments(new ControlFieldImpl("001", "content")),
      arguments(new ControlFieldImpl("002", "content")),
      arguments(new ControlFieldImpl("003", "content")),
      arguments(new ControlFieldImpl("004", "content")),
      arguments(new ControlFieldImpl("005", "content")),
      arguments(new ControlFieldImpl("009", "content"))
    );
  }

  @ParameterizedTest
  @MethodSource("fieldData")
  void testConvertField(ControlField dtoField) {
    var actualQmField = converter.convert(dtoField, null);
    assertThat(actualQmField)
      .hasFieldOrPropertyWithValue("tag", dtoField.getTag())
      .hasFieldOrPropertyWithValue("content", dtoField.getData());
  }

  @ParameterizedTest
  @MethodSource("fieldData")
  void testCanProcessField(ControlField dtoField) {
    assertTrue(converter.canProcess(dtoField, null));
  }

  @ParameterizedTest
  @ValueSource(strings = {"006", "007", "008"})
  void testCanProcessField(String tag) {
    assertFalse(converter.canProcess(new ControlFieldImpl(tag), null));
  }
}
