import static org.folio.converter.StringConstants.ACCM;
import static org.folio.converter.StringConstants.ALPH;
import static org.folio.converter.StringConstants.AUDN;
import static org.folio.converter.StringConstants.BIOG;
import static org.folio.converter.StringConstants.COMP;
import static org.folio.converter.StringConstants.CONF;
import static org.folio.converter.StringConstants.CONT;
import static org.folio.converter.StringConstants.CRTP;
import static org.folio.converter.StringConstants.CTRY;
import static org.folio.converter.StringConstants.DATE1;
import static org.folio.converter.StringConstants.DATE2;
import static org.folio.converter.StringConstants.DTST;
import static org.folio.converter.StringConstants.ENTERED;
import static org.folio.converter.StringConstants.ENTW;
import static org.folio.converter.StringConstants.FEST;
import static org.folio.converter.StringConstants.FILE;
import static org.folio.converter.StringConstants.FMUS;
import static org.folio.converter.StringConstants.FORM;
import static org.folio.converter.StringConstants.FREQ;
import static org.folio.converter.StringConstants.GPUB;
import static org.folio.converter.StringConstants.ILLS;
import static org.folio.converter.StringConstants.INDX;
import static org.folio.converter.StringConstants.LANG;
import static org.folio.converter.StringConstants.LITF;
import static org.folio.converter.StringConstants.LTXT;
import static org.folio.converter.StringConstants.MREC;
import static org.folio.converter.StringConstants.ORIG;
import static org.folio.converter.StringConstants.PART;
import static org.folio.converter.StringConstants.PROJ;
import static org.folio.converter.StringConstants.REGL;
import static org.folio.converter.StringConstants.RELF;
import static org.folio.converter.StringConstants.SL;
import static org.folio.converter.StringConstants.SPFM;
import static org.folio.converter.StringConstants.SRCE;
import static org.folio.converter.StringConstants.SRTP;
import static org.folio.converter.StringConstants.TECH;
import static org.folio.converter.StringConstants.TIME;
import static org.folio.converter.StringConstants.TMAT;
import static org.folio.converter.StringConstants.TRAR;
import static org.folio.converter.StringConstants.VALUE;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import io.vertx.core.json.JsonObject;
import org.folio.converter.ContentType;
import org.folio.converter.Field008SplitterFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class Field008SplitterFactoryTests {
  private static final Logger logger = LoggerFactory.getLogger(Field008SplitterFactoryTests.class);

  @Test
  public void testField008SplitHeaderAndFooter() {
    logger.info("Field 008: Test splitting common fields");
    String sample = "abcdefghijklmnopqr#################stuvw";
    JsonObject jsonObject = Field008SplitterFactory.getStrategy(ContentType.BOOKS).split(sample);
    // header values
    assertEquals("abcdef", jsonObject.getString(ENTERED));  // 0-5
    assertEquals("g", jsonObject.getString(DTST));          // 6
    assertEquals("hijk", jsonObject.getString(DATE1));      // 7-10
    assertEquals("lmno", jsonObject.getString(DATE2));      // 11-14
    assertEquals("pqr", jsonObject.getString(CTRY));        // 15-17

    // footer values
    assertEquals("stu", jsonObject.getString(LANG));        // 35-37
    assertEquals("v", jsonObject.getString(MREC));          // 38
    assertEquals("w", jsonObject.getString(SRCE));          // 39
  }

  @Test
  public void testField008SplitForBooks() {
    logger.info("Field 008: Test splitting for Books content type");
    String sample = "##################abcdefghijklmn#op#####";
    JsonObject jsonObject = Field008SplitterFactory.getStrategy(ContentType.BOOKS).split(sample);
    assertLinesMatch(Arrays.asList("a", "b", "c", "d"), jsonObject.getJsonArray(ILLS).getList());
    assertEquals("e", jsonObject.getString(AUDN));
    assertEquals("f", jsonObject.getString(FORM));
    assertLinesMatch(Arrays.asList("g", "h", "i", "j"), jsonObject.getJsonArray(CONT).getList());
    assertEquals("k", jsonObject.getString(GPUB));
    assertEquals("l", jsonObject.getString(CONF));
    assertEquals("m", jsonObject.getString(FEST));
    assertEquals("n", jsonObject.getString(INDX));
    assertEquals("o", jsonObject.getString(LITF));
    assertEquals("p", jsonObject.getString(BIOG));
  }

  @Test
  public void testField008SplitForFiles() {
    logger.info("Field 008: Test splitting for ComputerFiles content type");
    String sample = "######################ab##c#d###########";
    JsonObject jsonObject = Field008SplitterFactory.getStrategy(ContentType.FILES).split(sample);
    assertEquals("a", jsonObject.getString(AUDN));
    assertEquals("b", jsonObject.getString(FORM));
    assertEquals("c", jsonObject.getString(FILE));
    assertEquals("d", jsonObject.getString(GPUB));
  }

  @Test
  public void testField008SplitForContinuing() {
    logger.info("Field 008: Test splitting for Continuing Resources content type");
    String sample = "##################ab#cdefghijk###lm#####";
    JsonObject jsonObject = Field008SplitterFactory.getStrategy(ContentType.CONTINUING).split(sample);
    assertEquals("a", jsonObject.getString(FREQ));
    assertEquals("b", jsonObject.getString(REGL));
    assertEquals("c", jsonObject.getString(SRTP));
    assertEquals("d", jsonObject.getString(ORIG));
    assertEquals("e", jsonObject.getString(FORM));
    assertEquals("f", jsonObject.getString(ENTW));
    assertLinesMatch(Arrays.asList("g", "h", "i"), jsonObject.getJsonArray(CONT).getList());
    assertEquals("j", jsonObject.getString(GPUB));
    assertEquals("k", jsonObject.getString(CONF));
    assertEquals("l", jsonObject.getString(ALPH));
    assertEquals("m", jsonObject.getString(SL));
  }

  @Test
  public void testField008SplitForMaps() {
    logger.info("Field 008: Test splitting for Maps content type");
    String sample = "##################abcdef#g##hi#j#kl#####";
    JsonObject jsonObject = Field008SplitterFactory.getStrategy(ContentType.MAPS).split(sample);
    assertLinesMatch(Arrays.asList("a", "b", "c", "d"), jsonObject.getJsonArray(RELF).getList());
    assertEquals("ef", jsonObject.getString(PROJ));
    assertEquals("g", jsonObject.getString(CRTP));
    assertEquals("h", jsonObject.getString(GPUB));
    assertEquals("i", jsonObject.getString(FORM));
    assertEquals("j", jsonObject.getString(INDX));
    assertEquals("kl", jsonObject.getString(SPFM));
  }

  @Test
  public void testField008SplitForMixed() {
    logger.info("Field 008: Test splitting for Mixed Materials content type");
    String sample = "#######################a################";
    JsonObject jsonObject = Field008SplitterFactory.getStrategy(ContentType.MIXED).split(sample);
    assertEquals("a", jsonObject.getString(FORM));
  }

  @Test
  public void testField008SplitForScores() {
    logger.info("Field 008: Test splitting for Musical Scores content type");
    String sample = "##################abcdefghijklmn#o######";
    JsonObject jsonObject = Field008SplitterFactory.getStrategy(ContentType.SCORES).split(sample);
    assertEquals("ab", jsonObject.getString(COMP));
    assertEquals("c", jsonObject.getString(FMUS));
    assertEquals("d", jsonObject.getString(PART));
    assertEquals("e", jsonObject.getString(AUDN));
    assertEquals("f", jsonObject.getString(FORM));
    assertLinesMatch(Arrays.asList("g", "h", "i", "j", "k", "l"), jsonObject.getJsonArray(ACCM).getList());
    assertLinesMatch(Arrays.asList("m", "n"), jsonObject.getJsonArray(LTXT).getList());
    assertEquals("o", jsonObject.getString(TRAR));
  }

  @Test
  public void testField008SplitForSound() {
    logger.info("Field 008: Test splitting for Sound Recordings content type");
    String sample = "##################abcdefghijklmn#o######";
    JsonObject jsonObject = Field008SplitterFactory.getStrategy(ContentType.SCORES).split(sample);
    assertEquals("ab", jsonObject.getString(COMP));
    assertEquals("c", jsonObject.getString(FMUS));
    assertEquals("d", jsonObject.getString(PART));
    assertEquals("e", jsonObject.getString(AUDN));
    assertEquals("f", jsonObject.getString("Form"));
    assertLinesMatch(Arrays.asList("g", "h", "i", "j", "k", "l"), jsonObject.getJsonArray(ACCM).getList());
    assertLinesMatch(Arrays.asList("m", "n"), jsonObject.getJsonArray(LTXT).getList());
    assertEquals("o", jsonObject.getString(TRAR));
  }

  @Test
  public void testField008SplitForVisual() {
    logger.info("Field 008: Test splitting for Visual Materials content type");
    String sample = "##################abc#d#####ef###gh#####";
    JsonObject jsonObject = Field008SplitterFactory.getStrategy(ContentType.VISUAL).split(sample);
    assertLinesMatch(Arrays.asList("a", "b", "c"), jsonObject.getJsonArray(TIME).getList());
    assertEquals("d", jsonObject.getString(AUDN));
    assertEquals("e", jsonObject.getString(GPUB));
    assertEquals("f", jsonObject.getString(FORM));
    assertEquals("g", jsonObject.getString(TMAT));
    assertEquals("h", jsonObject.getString(TECH));
  }

  @Test
  public void testField008SplitForUnknown() {
    logger.info("Field 008: Test splitting for Unknown content type");
    String sample = "##################abcdefghijklmnopq#####";
    JsonObject jsonObject = Field008SplitterFactory.getStrategy(ContentType.UNKNOWN).split(sample);
    assertEquals("abcdefghijklmnopq", jsonObject.getString(VALUE));
  }
}
