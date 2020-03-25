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

import org.folio.converter.ContentType;
import org.folio.converter.Field008SplitterFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Field008SplitterFactoryTests {
  private static final Logger logger = LoggerFactory.getLogger(Field008SplitterFactoryTests.class);

  @Test
  public void testField008SplitHeaderAndFooter() {
    logger.info("Field 008: Test splitting common fields");
    String sample = "abcdefghijklmnopqr#################stuvw";
    Map<String, Object> map = Field008SplitterFactory.getStrategy(ContentType.BOOKS).split(sample);
    // header values
    assertEquals("abcdef", map.get(ENTERED));  // 0-5
    assertEquals("g", map.get(DTST));          // 6
    assertEquals("hijk", map.get(DATE1));      // 7-10
    assertEquals("lmno", map.get(DATE2));      // 11-14
    assertEquals("pqr", map.get(CTRY));        // 15-17

    // footer values
    assertEquals("stu", map.get(LANG));        // 35-37
    assertEquals("v", map.get(MREC));          // 38
    assertEquals("w", map.get(SRCE));          // 39
  }

  @Test
  public void testField008SplitForBooks() {
    logger.info("Field 008: Test splitting for Books content type");
    String sample = "##################abcdefghijklmn#op#####";
    Map<String, Object> map = Field008SplitterFactory.getStrategy(ContentType.BOOKS).split(sample);
    assertLinesMatch(Arrays.asList("a", "b", "c", "d"), (List) map.get(ILLS));
    assertEquals("e", map.get(AUDN));
    assertEquals("f", map.get(FORM));
    assertLinesMatch(Arrays.asList("g", "h", "i", "j"), (List) map.get(CONT));
    assertEquals("k", map.get(GPUB));
    assertEquals("l", map.get(CONF));
    assertEquals("m", map.get(FEST));
    assertEquals("n", map.get(INDX));
    assertEquals("o", map.get(LITF));
    assertEquals("p", map.get(BIOG));
  }

  @Test
  public void testField008SplitForFiles() {
    logger.info("Field 008: Test splitting for ComputerFiles content type");
    String sample = "######################ab##c#d###########";
    Map<String, Object> map = Field008SplitterFactory.getStrategy(ContentType.FILES).split(sample);
    assertEquals("a", map.get(AUDN));
    assertEquals("b", map.get(FORM));
    assertEquals("c", map.get(FILE));
    assertEquals("d", map.get(GPUB));
  }

  @Test
  public void testField008SplitForContinuing() {
    logger.info("Field 008: Test splitting for Continuing Resources content type");
    String sample = "##################ab#cdefghijk###lm#####";
    Map<String, Object> map = Field008SplitterFactory.getStrategy(ContentType.CONTINUING).split(sample);
    assertEquals("a", map.get(FREQ));
    assertEquals("b", map.get(REGL));
    assertEquals("c", map.get(SRTP));
    assertEquals("d", map.get(ORIG));
    assertEquals("e", map.get(FORM));
    assertEquals("f", map.get(ENTW));
    assertLinesMatch(Arrays.asList("g", "h", "i"), (List) map.get(CONT));
    assertEquals("j", map.get(GPUB));
    assertEquals("k", map.get(CONF));
    assertEquals("l", map.get(ALPH));
    assertEquals("m", map.get(SL));
  }

  @Test
  public void testField008SplitForMaps() {
    logger.info("Field 008: Test splitting for Maps content type");
    String sample = "##################abcdef#g##hi#j#kl#####";
    Map<String, Object> map = Field008SplitterFactory.getStrategy(ContentType.MAPS).split(sample);
    assertLinesMatch(Arrays.asList("a", "b", "c", "d"), (List) map.get(RELF));
    assertEquals("ef", map.get(PROJ));
    assertEquals("g", map.get(CRTP));
    assertEquals("h", map.get(GPUB));
    assertEquals("i", map.get(FORM));
    assertEquals("j", map.get(INDX));
    assertEquals("kl", map.get(SPFM));
  }

  @Test
  public void testField008SplitForMixed() {
    logger.info("Field 008: Test splitting for Mixed Materials content type");
    String sample = "#######################a################";
    Map<String, Object> map = Field008SplitterFactory.getStrategy(ContentType.MIXED).split(sample);
    assertEquals("a", map.get(FORM));
  }

  @Test
  public void testField008SplitForScores() {
    logger.info("Field 008: Test splitting for Musical Scores content type");
    String sample = "##################abcdefghijklmn#o######";
    Map<String, Object> map = Field008SplitterFactory.getStrategy(ContentType.SCORES).split(sample);
    assertEquals("ab", map.get(COMP));
    assertEquals("c", map.get(FMUS));
    assertEquals("d", map.get(PART));
    assertEquals("e", map.get(AUDN));
    assertEquals("f", map.get(FORM));
    assertLinesMatch(Arrays.asList("g", "h", "i", "j", "k", "l"), (List) map.get(ACCM));
    assertLinesMatch(Arrays.asList("m", "n"), (List) map.get(LTXT));
    assertEquals("o", map.get(TRAR));
  }

  @Test
  public void testField008SplitForSound() {
    logger.info("Field 008: Test splitting for Sound Recordings content type");
    String sample = "##################abcdefghijklmn#o######";
    Map<String, Object> map = Field008SplitterFactory.getStrategy(ContentType.SCORES).split(sample);
    assertEquals("ab", map.get(COMP));
    assertEquals("c", map.get(FMUS));
    assertEquals("d", map.get(PART));
    assertEquals("e", map.get(AUDN));
    assertEquals("f", map.get("Form"));
    assertLinesMatch(Arrays.asList("g", "h", "i", "j", "k", "l"), (List) map.get(ACCM));
    assertLinesMatch(Arrays.asList("m", "n"), (List) map.get(LTXT));
    assertEquals("o", map.get(TRAR));
  }

  @Test
  public void testField008SplitForVisual() {
    logger.info("Field 008: Test splitting for Visual Materials content type");
    String sample = "##################abc#d#####ef###gh#####";
    Map<String, Object> map = Field008SplitterFactory.getStrategy(ContentType.VISUAL).split(sample);
    assertLinesMatch(Arrays.asList("a", "b", "c"), (List) map.get(TIME));
    assertEquals("d", map.get(AUDN));
    assertEquals("e", map.get(GPUB));
    assertEquals("f", map.get(FORM));
    assertEquals("g", map.get(TMAT));
    assertEquals("h", map.get(TECH));
  }

  @Test
  public void testField008SplitForUnknown() {
    logger.info("Field 008: Test splitting for Unknown content type");
    String sample = "##################abcdefghijklmnopq#####";
    Map<String, Object> map = Field008SplitterFactory.getStrategy(ContentType.UNKNOWN).split(sample);
    assertEquals("abcdefghijklmnopq", map.get(VALUE));
  }
}
