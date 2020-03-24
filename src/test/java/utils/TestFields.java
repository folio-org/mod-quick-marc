package utils;

import com.sun.tools.javac.util.List;
import org.folio.converter.ContentType;

public enum TestFields {
  BOOKS(ContentType.BOOKS, "mockdata/samples008/books.json", "abcdefghijklmnopqrabcdefghijklmn opstuvw", List.of("a", "t"), List.of("a")),
  FILES(ContentType.FILES, "mockdata/samples008/files.json", "abcdefghijklmnopqr    ab  c d      stuvw", List.of("m"), List.of("a")),
  CONTINUING(ContentType.CONTINUING, "mockdata/samples008/continuing.json", "abcdefghijklmnopqrab cdefghijk   lmstuvw", List.of("a"), List.of("b", "i", "s")),
  MAPS(ContentType.MAPS, "mockdata/samples008/maps.json", "abcdefghijklmnopqrabcdef g  hi j klstuvw", List.of("e", "f"), List.of("a")),
  MIXED(ContentType.MIXED, "mockdata/samples008/mixed.json", "abcdefghijklmnopqr     a           stuvw", List.of("p"), List.of("c")),
  SCORES(ContentType.SCORES, "mockdata/samples008/scores.json", "abcdefghijklmnopqrabcdefghijklmn o stuvw", List.of("c", "d"), List.of("a")),
  SOUND(ContentType.SOUND, "mockdata/samples008/sound.json", "abcdefghijklmnopqrabcdefghijklmn o stuvw", List.of("i", "j"), List.of("a")),
  VISUAL(ContentType.VISUAL, "mockdata/samples008/visual.json", "abcdefghijklmnopqrabc d     ef   ghstuvw", List.of("g", "k", "o", "r"), List.of("a")),
  UNKNOWN(ContentType.UNKNOWN, "mockdata/samples008/unknown.json", "abcdefghijklmnopqrabcdefghijklmnopqstuvw", List.of(" ", "b", "h", "l", "n", "q", "s", "u", "v", "w", "x", "y", "z", "1", "$", "%"), List.of("a"));

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
