package org.folio.converter;

import org.marc4j.marc.Leader;
import org.marc4j.marc.impl.LeaderImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum TestEntities {
  BOOKS_MISSING_ITEMS(ContentType.BOOKS, "mockdata/parsed-records/books_missing_items.json", "mockdata/quick-marc-json/books_missing_items.json",
    Collections.singletonList(new LeaderImpl("xxxxxxaaxxxxxxxxxxxxxxxx"))),
  BOOKS(ContentType.BOOKS, "mockdata/parsed-records/books.json", "mockdata/quick-marc-json/books.json",
    Arrays.asList(new LeaderImpl("xxxxxxaaxxxxxxxxxxxxxxxx"),
                  new LeaderImpl("xxxxxxtaxxxxxxxxxxxxxxxx"))),
  FILES(ContentType.FILES, "mockdata/parsed-records/files.json", "mockdata/quick-marc-json/files.json",
    Collections.singletonList(new LeaderImpl("xxxxxxmaxxxxxxxxxxxxxxxx"))),
  CONTINUING(ContentType.CONTINUING, "mockdata/parsed-records/continuing.json", "mockdata/quick-marc-json/continuing.json",
    Collections.singletonList(new LeaderImpl("xxxxxxssxxxxxxxxxxxxxxxx"))),
  MAPS(ContentType.MAPS, "mockdata/parsed-records/maps.json", "mockdata/quick-marc-json/maps.json",
    Arrays.asList(new LeaderImpl("xxxxxxeaxxxxxxxxxxxxxxxx"),
                  new LeaderImpl("xxxxxxfaxxxxxxxxxxxxxxxx"))),
  MIXED(ContentType.MIXED, "mockdata/parsed-records/mixed.json", "mockdata/quick-marc-json/mixed.json",
    Collections.singletonList(new LeaderImpl("xxxxxxpcxxxxxxxxxxxxxxxx"))),
  SCORES(ContentType.SCORES, "mockdata/parsed-records/scores.json", "mockdata/quick-marc-json/scores.json",
    Arrays.asList(new LeaderImpl("xxxxxxcaxxxxxxxxxxxxxxxx"),
                  new LeaderImpl("xxxxxxdaxxxxxxxxxxxxxxxx"))),
  SOUND(ContentType.SOUND, "mockdata/parsed-records/sound.json", "mockdata/quick-marc-json/sound.json",
    Arrays.asList(new LeaderImpl("xxxxxxiaxxxxxxxxxxxxxxxx"),
                  new LeaderImpl("xxxxxxjaxxxxxxxxxxxxxxxx"))),
  VISUAL(ContentType.VISUAL, "mockdata/parsed-records/visual.json", "mockdata/quick-marc-json/visual.json",
    Arrays.asList(new LeaderImpl("xxxxxxgaxxxxxxxxxxxxxxxx"),
                  new LeaderImpl("xxxxxxkaxxxxxxxxxxxxxxxx"),
                  new LeaderImpl("xxxxxxoaxxxxxxxxxxxxxxxx"),
                  new LeaderImpl("xxxxxxraxxxxxxxxxxxxxxxx"))),
  UNKNOWN(ContentType.UNKNOWN, "mockdata/parsed-records/unknown.json", "mockdata/quick-marc-json/unknown.json",
    Arrays.asList(new LeaderImpl("xxxxxx axxxxxxxxxxxxxxxx"),
                  new LeaderImpl("xxxxxxbaxxxxxxxxxxxxxxxx"),
                  new LeaderImpl("xxxxxxhaxxxxxxxxxxxxxxxx"),
                  new LeaderImpl("xxxxxxlaxxxxxxxxxxxxxxxx"),
                  new LeaderImpl("xxxxxx1axxxxxxxxxxxxxxxx"),
                  new LeaderImpl("xxxxxx$axxxxxxxxxxxxxxxx"),
                  new LeaderImpl("xxxxxx%axxxxxxxxxxxxxxxx")));

  private ContentType contentType;
  private String parsedRecordPath;
  private String quickMarcJsonPath;
  private List<Leader> leaders;

  TestEntities(ContentType contentType, String parsedRecordPath, String quickMarcJsonPath, List<Leader> leaders) {
    this.contentType = contentType;
    this.parsedRecordPath = parsedRecordPath;
    this.quickMarcJsonPath = quickMarcJsonPath;
    this.leaders = leaders;
  }

  public ContentType getContentType() {
    return contentType;
  }

  public String getParsedRecordPath() {
    return parsedRecordPath;
  }

  public String getQuickMarcJsonPath() {
    return quickMarcJsonPath;
  }

  public List<Leader> getLeaders() {
    return leaders;
  }
}
