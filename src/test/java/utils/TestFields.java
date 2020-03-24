package utils;

import org.folio.converter.ContentType;

import java.util.Arrays;
import java.util.List;

public enum TestFields {
  BOOKS(ContentType.BOOKS, "mockdata/samples008/books.json", "abcdefghijklmnopqrabcdefghijklmn opstuvw", Arrays.asList("a", "t"), Arrays.asList("a")),
  FILES(ContentType.FILES, "mockdata/samples008/files.json", "abcdefghijklmnopqr    ab  c d      stuvw", Arrays.asList("m"), Arrays.asList("a")),
  CONTINUING(ContentType.CONTINUING, "mockdata/samples008/continuing.json", "abcdefghijklmnopqrab cdefghijk   lmstuvw", Arrays.asList("a"), Arrays.asList("b", "i", "s")),
  MAPS(ContentType.MAPS, "mockdata/samples008/maps.json", "abcdefghijklmnopqrabcdef g  hi j klstuvw", Arrays.asList("e", "f"), Arrays.asList("a")),
  MIXED(ContentType.MIXED, "mockdata/samples008/mixed.json", "abcdefghijklmnopqr     a           stuvw", Arrays.asList("p"), Arrays.asList("c")),
  SCORES(ContentType.SCORES, "mockdata/samples008/scores.json", "abcdefghijklmnopqrabcdefghijklmn o stuvw", Arrays.asList("c", "d"), Arrays.asList("a")),
  SOUND(ContentType.SOUND, "mockdata/samples008/sound.json", "abcdefghijklmnopqrabcdefghijklmn o stuvw", Arrays.asList("i", "j"), Arrays.asList("a")),
  VISUAL(ContentType.VISUAL, "mockdata/samples008/visual.json", "abcdefghijklmnopqrabc d     ef   ghstuvw", Arrays.asList("g", "k", "o", "r"), Arrays.asList("a")),
  UNKNOWN(ContentType.UNKNOWN, "mockdata/samples008/unknown.json", "abcdefghijklmnopqrabcdefghijklmnopqstuvw", Arrays.asList(" ", "b", "h", "l", "n", "q", "s", "u", "v", "w", "x", "y", "z", "1", "$", "%"), Arrays.asList("a"));

  private ContentType contentType;
  private String samplePath;
  private String expectedString;
  private List<String> types;
  private List<String> blvls;

  TestFields(ContentType contentType, String samplePath, String expectedString, List<String> types, List<String> blvls){
    this.contentType = contentType;
    this.samplePath = samplePath;
    this.expectedString = expectedString;
    this.types = types;
    this.blvls = blvls;
  }

  public ContentType getContentType() {
    return contentType;
  }

  public String getMockDataPath() {
    return samplePath;
  }

  public String getExpectedString() {
    return expectedString;
  }

  public List<String> getTypes() {
    return types;
  }

  public List<String> getBlvls() {
    return blvls;
  }
}
