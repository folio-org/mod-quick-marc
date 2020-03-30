package org.folio.rest.impl;

import org.junit.jupiter.api.Nested;

public class TestSuite {
  @Nested
  class ContentTypeTestNested extends ContentTypeTest {}
  @Nested
  class Field008SplitterFactoryTestNested extends Field008SplitterFactoryTest {}
  @Nested
  class Field008RestoreFactoryTestNested extends Field008RestoreFactoryTest {}
  @Nested
  class ParsedRecordToQuickMarcConverterTestNested extends ParsedRecordToQuickMarcConverterTest {}
  @Nested
  class QuickMarcToParsedRecordConverterTestNested extends QuickMarcToParsedRecordConverterTest {}
}
