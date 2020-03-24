import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
@SelectClasses({
  ContentTypeTests.class,
  Field008SpliterFactoryTests.class,
  Field008RestoreFactoryTests.class,
  RecordToQuickMarcConverterTest.class,
  QuickMarcToRecordConverterTest.class
})
public class TestSuite {
}
