import org.junit.jupiter.api.Nested;

public class TestSuite {
  @Nested
  class ContentTypeTestsNested extends ContentTypeTests {}
  @Nested
  class Field008SplitterFactoryTestsNested extends Field008SplitterFactoryTests {}
  @Nested
  class Field008RestoreFactoryTestsNested extends Field008RestoreFactoryTests {}
  @Nested
  class RecordToQuickMarcConverterTestNested extends RecordToQuickMarcConverterTest {}
  @Nested
  class QuickMarcToRecordConverterTestNested extends QuickMarcToRecordConverterTest {}
}
