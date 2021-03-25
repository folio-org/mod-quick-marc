package org.folio.qm.utils.testentities;

public enum LccnFieldsTestEntities {

  POST_2000_NO_PREFIX("mockdata/quick-marc-json/lccn_fields/post2000_no_prefix.json"),
  POST_2000_PREFIX1("mockdata/quick-marc-json/lccn_fields/post2000_prefix1.json"),
  POST_2000_PREFIX2("mockdata/quick-marc-json/lccn_fields/post2000_prefix2.json"),
  PRE_2001_NO_PREFIX("mockdata/quick-marc-json/lccn_fields/pre2001_no_prefix.json"),
  PRE_2001_NO_PREFIX_SUFFIX("mockdata/quick-marc-json/lccn_fields/pre2001_no_prefix_suffix.json"),
  PRE_2001_PREFIX1("mockdata/quick-marc-json/lccn_fields/pre2001_prefix1.json"),
  PRE_2001_PREFIX2("mockdata/quick-marc-json/lccn_fields/pre2001_prefix2.json"),
  PRE_2001_PREFIX3("mockdata/quick-marc-json/lccn_fields/pre2001_prefix3.json"),
  PRE_2001_PREFIX3_SUFFIX("mockdata/quick-marc-json/lccn_fields/pre2001_prefix3_suffix.json"),
  WRONG_LENGTH("mockdata/quick-marc-json/lccn_fields/lccn_wrong_length.json");

  private final String filename;

  LccnFieldsTestEntities(String filename) {
    this.filename = filename;
  }

  public String getFilename() {
    return filename;
  }
}
