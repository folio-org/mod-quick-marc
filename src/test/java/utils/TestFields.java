package utils;

import org.folio.converter.ContentType;

public enum TestFields {
  BOOKS(ContentType.BOOKS, "mockdata/samples008/books.json", "abcdefghijklmnopqrabcdefghijklmn opstuvw"),
  FILES(ContentType.FILES, "mockdata/samples008/files.json", "abcdefghijklmnopqr    ab  c d      stuvw"),
  CONTINUING(ContentType.CONTINUING, "mockdata/samples008/continuing.json", "abcdefghijklmnopqrab cdefghijk   lmstuvw"),
  MAPS(ContentType.MAPS, "mockdata/samples008/maps.json", "abcdefghijklmnopqrabcdef g  hi j klstuvw"),
  MIXED(ContentType.MIXED, "mockdata/samples008/mixed.json", "abcdefghijklmnopqr     a           stuvw"),
  SCORES(ContentType.SCORES, "mockdata/samples008/scores.json", "abcdefghijklmnopqrabcdefghijklmn o stuvw"),
  SOUND(ContentType.SOUND, "mockdata/samples008/sound.json", "abcdefghijklmnopqrabcdefghijklmn o stuvw"),
  VISUAL(ContentType.VISUAL, "mockdata/samples008/visual.json", "abcdefghijklmnopqrabc d     ef   ghstuvw"),
  UNKNOWN(ContentType.UNKNOWN, "mockdata/samples008/unknown.json", "abcdefghijklmnopqrabcdefghijklmnopqstuvw");

  private ContentType contentType;
  private String samplePath;
  private String expectedString;

  TestFields(ContentType contentType, String samplePath, String expectedString){
    this.contentType = contentType;
    this.samplePath = samplePath;
    this.expectedString = expectedString;
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
}
