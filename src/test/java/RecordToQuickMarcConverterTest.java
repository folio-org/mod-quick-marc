import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static utils.TestUtils.getMockAsJson;

import io.vertx.core.json.JsonObject;
import org.folio.converter.ContentType;
import org.folio.converter.Field008SplitterFactory;
import org.folio.converter.RecordToQuickMarcConverter;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.srs.model.Record;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class RecordToQuickMarcConverterTest {
  private static final Logger logger = LoggerFactory.getLogger(RecordToQuickMarcConverterTest.class);

  @Test
  public void testField008SplitHeaderAndFooter() {
    logger.info("Field 008: Test splitting common fields");
    String sample = "abcdefghijklmnopqr#################stuvw";
    JsonObject jsonObject = Field008SplitterFactory.getStrategy(ContentType.BOOKS).split(sample);
    // header values
    assertEquals("abcdef", jsonObject.getString("Entered"));  // 0-5
    assertEquals("g", jsonObject.getString("DtSt"));          // 6
    assertEquals("hijk", jsonObject.getString("Date1"));      // 7-10
    assertEquals("lmno", jsonObject.getString("Date2"));      // 11-14
    assertEquals("pqr", jsonObject.getString("Ctry"));        // 15-17

    // footer values
    assertEquals("stu", jsonObject.getString("Lang"));        // 35-37
    assertEquals("v", jsonObject.getString("MRec"));          // 38
    assertEquals("w", jsonObject.getString("Srce"));          // 39
  }

  @Test
  public void testField008SplitForBooks() {
    logger.info("Field 008: Test splitting for Books content type");
    String sample = "##################abcdefghijklmn#op#####";
    JsonObject jsonObject = Field008SplitterFactory.getStrategy(ContentType.BOOKS).split(sample);
    assertLinesMatch(Arrays.asList("a", "b", "c", "d"), jsonObject.getJsonArray("Ills").getList());
    assertEquals("e", jsonObject.getString("Audn"));
    assertEquals("f", jsonObject.getString("Form"));
    assertLinesMatch(Arrays.asList("g", "h", "i", "j"), jsonObject.getJsonArray("Cont").getList());
    assertEquals("k", jsonObject.getString("GPub"));
    assertEquals("l", jsonObject.getString("Conf"));
    assertEquals("m", jsonObject.getString("Fest"));
    assertEquals("n", jsonObject.getString("Indx"));
    assertEquals("o", jsonObject.getString("LitF"));
    assertEquals("p", jsonObject.getString("Biog"));
  }

  @Test
  public void testField008SplitForFiles() {
    logger.info("Field 008: Test splitting for ComputerFiles content type");
    String sample = "######################ab##c#d###########";
    JsonObject jsonObject = Field008SplitterFactory.getStrategy(ContentType.FILES).split(sample);
    assertEquals("a", jsonObject.getString("Audn"));
    assertEquals("b", jsonObject.getString("Form"));
    assertEquals("c", jsonObject.getString("File"));
    assertEquals("d", jsonObject.getString("GPub"));
  }

  @Test
  public void testField008SplitForContinuing() {
    logger.info("Field 008: Test splitting for Continuing Resources content type");
    String sample = "##################ab#cdefghijk###lm#####";
    JsonObject jsonObject = Field008SplitterFactory.getStrategy(ContentType.CONTINUING).split(sample);
    assertEquals("a", jsonObject.getString("Freq"));
    assertEquals("b", jsonObject.getString("Regl"));
    assertEquals("c", jsonObject.getString("SrTp"));
    assertEquals("d", jsonObject.getString("Orig"));
    assertEquals("e", jsonObject.getString("Form"));
    assertEquals("f", jsonObject.getString("EntW"));
    assertLinesMatch(Arrays.asList("g", "h", "i"), jsonObject.getJsonArray("Cont").getList());
    assertEquals("j", jsonObject.getString("GPub"));
    assertEquals("k", jsonObject.getString("Conf"));
    assertEquals("l", jsonObject.getString("Alph"));
    assertEquals("m", jsonObject.getString("S/L"));
  }

  @Test
  public void testField008SplitForMaps() {
    logger.info("Field 008: Test splitting for Maps content type");
    String sample = "##################abcdef#g##hi#j#kl#####";
    JsonObject jsonObject = Field008SplitterFactory.getStrategy(ContentType.MAPS).split(sample);
    assertLinesMatch(Arrays.asList("a", "b", "c", "d"), jsonObject.getJsonArray("Relf").getList());
    assertEquals("ef", jsonObject.getString("Proj"));
    assertEquals("g", jsonObject.getString("CrTp"));
    assertEquals("h", jsonObject.getString("GPub"));
    assertEquals("i", jsonObject.getString("Form"));
    assertEquals("j", jsonObject.getString("Indx"));
    assertEquals("kl", jsonObject.getString("SpFm"));
  }

  @Test
  public void testField008SplitForMixed() {
    logger.info("Field 008: Test splitting for Mixed Materials content type");
    String sample = "#######################a################";
    JsonObject jsonObject = Field008SplitterFactory.getStrategy(ContentType.MIXED).split(sample);
    assertEquals("a", jsonObject.getString("Form"));
  }

  @Test
  public void testField008SplitForScores() {
    logger.info("Field 008: Test splitting for Musical Scores content type");
    String sample = "##################abcdefghijklmn#o######";
    JsonObject jsonObject = Field008SplitterFactory.getStrategy(ContentType.SCORES).split(sample);
    assertEquals("ab", jsonObject.getString("Comp"));
    assertEquals("c", jsonObject.getString("FMus"));
    assertEquals("d", jsonObject.getString("Part"));
    assertEquals("e", jsonObject.getString("Audn"));
    assertEquals("f", jsonObject.getString("Form"));
    assertLinesMatch(Arrays.asList("g", "h", "i", "j", "k", "l"), jsonObject.getJsonArray("AccM").getList());
    assertLinesMatch(Arrays.asList("m", "n"), jsonObject.getJsonArray("LTxt").getList());
    assertEquals("o", jsonObject.getString("TrAr"));
  }

  @Test
  public void testField008SplitForSound() {
    logger.info("Field 008: Test splitting for Sound Recordings content type");
    String sample = "##################abcdefghijklmn#o######";
    JsonObject jsonObject = Field008SplitterFactory.getStrategy(ContentType.SCORES).split(sample);
    assertEquals("ab", jsonObject.getString("Comp"));
    assertEquals("c", jsonObject.getString("FMus"));
    assertEquals("d", jsonObject.getString("Part"));
    assertEquals("e", jsonObject.getString("Audn"));
    assertEquals("f", jsonObject.getString("Form"));
    assertLinesMatch(Arrays.asList("g", "h", "i", "j", "k", "l"), jsonObject.getJsonArray("AccM").getList());
    assertLinesMatch(Arrays.asList("m", "n"), jsonObject.getJsonArray("LTxt").getList());
    assertEquals("o", jsonObject.getString("TrAr"));
  }

  @Test
  public void testField008SplitForVisual() {
    logger.info("Field 008: Test splitting for Visual Materials content type");
    String sample = "##################abc#d#####ef###gh#####";
    JsonObject jsonObject = Field008SplitterFactory.getStrategy(ContentType.VISUAL).split(sample);
    assertLinesMatch(Arrays.asList("a", "b", "c"), jsonObject.getJsonArray("Time").getList());
    assertEquals("d", jsonObject.getString("Audn"));
    assertEquals("e", jsonObject.getString("GPub"));
    assertEquals("f", jsonObject.getString("Form"));
    assertEquals("g", jsonObject.getString("TMat"));
    assertEquals("h", jsonObject.getString("Tech"));
  }

  @Test
  public void testField008SplitForUnknown() {
    logger.info("Field 008: Test splitting for Unknown content type");
    String sample = "##################abcdefghijklmnopq#####";
    JsonObject jsonObject = Field008SplitterFactory.getStrategy(ContentType.UNKNOWN).split(sample);
    assertEquals("abcdefghijklmnopq", jsonObject.getString("Value"));
  }

  @Test
  public void exceptionIsThrownWhenNoRecord() {
    logger.info("Testing empty field 008  - RuntimeException expected");
    RecordToQuickMarcConverter converter = new RecordToQuickMarcConverter();
    Record emptyRecord = getMockAsJson("mockdata/recordWithEmptyRawRecord.json").mapTo(Record.class);
    assertThrows(RuntimeException.class, () -> converter.convert(emptyRecord));
  }

  @Test
  public void testRecordToQuickMarcJsonConversion(){
    logger.info("Testing Record -> QuickMarcJson conversion");
    RecordToQuickMarcConverter converter = new RecordToQuickMarcConverter();
    Record record = getMockAsJson("mockdata/record.json").mapTo(Record.class);
    QuickMarcJson quickMarcJson = converter.convert(record);
    QuickMarcJson expected = getMockAsJson("mockdata/quickMarcJson.json").mapTo(QuickMarcJson.class);
    String expectedString = JsonObject.mapFrom(expected).encodePrettily();
    String convertedString = JsonObject.mapFrom(quickMarcJson).encodePrettily();
    Assertions.assertEquals(expectedString, convertedString);
  }
}
