package org.folio.qm.support.utils.testdata;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum Tag008FieldTestData {

  BIB_BOOKS("abcdefghijklmnopqrabcdefghijklmn opstuvw", "00158caa a2200073   4500", getBooksContent()),
  BIB_BOOKS_WITH_EMPTY_ITEMS("abcdefghijklmnopqrabcde     klmn opstuvw", "00158caa a2200073   4500",
    getBooksWithEmptyItemsContent()),
  BIB_CONTINUING("abcdefghijklmnopqrab cdefghijk   lmstuvw", "00158csb a2200073   4500", getContinuingContent()),
  BIB_FILED("abcdefghijklmnopqr    ab  c d      stuvw", "00158cma a2200073   4500", getFiledContent()),
  BIB_MAPS("abcdefghijklmnopqrabcdef g  hi j klstuvw", "00158cea a2200073   4500", getMapsContent()),
  BIB_MIXED("abcdefghijklmnopqr     a           stuvw", "00158cpa a2200073   4500", getMixedContent()),
  BIB_SCORES("abcdefghijklmnopqrabcdefghijklmn o stuvw", "00158cca a2200073   4500", getScoresContent()),
  BIB_SOUND("abcdefghijklmnopqrabcdefghijklmn o stuvw", "00158cia a2200073   4500", getSoundContent()),
  BIB_UNKNOWN("abcdefghijklmnopqrabcdefghijklmnopqstuvw", "00158cha a2200073   4500", getUnknownContent()),
  BIB_VISUAL("abcdefghijklmnopqrabc d     ef   ghstuvw", "00158cga a2200073   4500", getVisualContent()),
  HOLDINGS("9301235u    8   0   uu     1    ", "00158cga a2200073   4500", getHoldingsContent()),
  AUTHORITY("810824n| azannaabn   a      |b aaa      ", "00158cga a2200073   4500", getAuthorityContent());

  private final String dtoData;
  private final String leader;
  private final Map<String, Object> qmContent;

  Tag008FieldTestData(String dtoData, String leader, Map<String, Object> qmContent) {
    this.dtoData = dtoData;
    this.leader = leader;
    this.qmContent = qmContent;
  }

  private static Map<String, Object> getAuthorityContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("Date Ent", "810824");
    content.put("Geo Subd", "n");
    content.put("Roman", "|");
    content.put("Lang", "\\");
    content.put("Kind rec", "a");
    content.put("Cat Rules", "z");
    content.put("SH Sys", "a");
    content.put("Series", "n");
    content.put("Numb Series", "n");
    content.put("Main use", "a");
    content.put("Subj use", "a");
    content.put("Series use", "b");
    content.put("Type Subd", "n");
    content.put("Undef_18", "\\\\\\a\\\\\\\\\\\\");
    content.put("Govt Ag", "|");
    content.put("RefEval", "b");
    content.put("Undef_30", "\\");
    content.put("RecUpd", "a");
    content.put("Pers Name", "a");
    content.put("Level Est", "a");
    content.put("Undef_34", "\\\\\\\\");
    content.put("Mod Rec Est", "\\");
    content.put("Source", "\\");
    return content;
  }

  private static Map<String, Object> getHoldingsContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("AcqStatus", "5");
    content.put("AcqMethod", "u");
    content.put("AcqEndDate", "\\\\\\\\");
    content.put("Compl", "0");
    content.put("Copies", "\\\\\\");
    content.put("Date Ent", "930123");
    content.put("Gen ret", "8");
    content.put("Lang", "\\\\\\");
    content.put("Lend", "u");
    content.put("Repro", "u");
    content.put("Rept date", "\\1\\\\\\\\");
    content.put("Sep/comp", "\\");
    content.put("Spec ret", List.of("\\", "\\", "\\"));
    return content;
  }

  private static Map<String, Object> getBooksContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("Type", "a");
    content.put("BLvl", "a");
    content.put("Desc", "\\");
    content.put("Entered", "abcdef");
    content.put("DtSt", "g");
    content.put("Date1", "hijk");
    content.put("Date2", "lmno");
    content.put("Ctry", "pqr");
    content.put("Lang", "stu");
    content.put("MRec", "v");
    content.put("Srce", "w");
    content.put("Ills", List.of("a", "b", "c", "d"));
    content.put("Audn", "e");
    content.put("Form", "f");
    content.put("Cont", List.of("g", "h", "i", "j"));
    content.put("GPub", "k");
    content.put("Conf", "l");
    content.put("Fest", "m");
    content.put("Indx", "n");
    content.put("LitF", "o");
    content.put("Biog", "p");
    return content;
  }

  private static Map<String, Object> getBooksWithEmptyItemsContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("Type", "a");
    content.put("BLvl", "a");
    content.put("Desc", "\\");
    content.put("Entered", "abcdef");
    content.put("DtSt", "g");
    content.put("Date1", "hijk");
    content.put("Date2", "lmno");
    content.put("Ctry", "pqr");
    content.put("Lang", "stu");
    content.put("MRec", "v");
    content.put("Srce", "w");
    content.put("Ills", List.of("a", "b", "c", "d"));
    content.put("Audn", "e");
    content.put("Form", "\\");
    content.put("Cont", List.of("\\", "\\", "\\", "\\"));
    content.put("GPub", "k");
    content.put("Conf", "l");
    content.put("Fest", "m");
    content.put("Indx", "n");
    content.put("LitF", "o");
    content.put("Biog", "p");
    return content;
  }

  private static Map<String, Object> getContinuingContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("Type", "s");
    content.put("BLvl", "b");
    content.put("Desc", "\\");
    content.put("Entered", "abcdef");
    content.put("DtSt", "g");
    content.put("Date1", "hijk");
    content.put("Date2", "lmno");
    content.put("Ctry", "pqr");
    content.put("Lang", "stu");
    content.put("MRec", "v");
    content.put("Srce", "w");
    content.put("Freq", "a");
    content.put("Regl", "b");
    content.put("SrTp", "c");
    content.put("Orig", "d");
    content.put("Form", "e");
    content.put("EntW", "f");
    content.put("Cont", List.of("g", "h", "i"));
    content.put("GPub", "j");
    content.put("Conf", "k");
    content.put("Alph", "l");
    content.put("S/L", "m");
    return content;
  }

  private static Map<String, Object> getFiledContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("Type", "m");
    content.put("BLvl", "a");
    content.put("Desc", "\\");
    content.put("Entered", "abcdef");
    content.put("DtSt", "g");
    content.put("Date1", "hijk");
    content.put("Date2", "lmno");
    content.put("Ctry", "pqr");
    content.put("Lang", "stu");
    content.put("MRec", "v");
    content.put("Srce", "w");
    content.put("Audn", "a");
    content.put("Form", "b");
    content.put("File", "c");
    content.put("GPub", "d");
    return content;
  }

  private static Map<String, Object> getMapsContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("Type", "e");
    content.put("BLvl", "a");
    content.put("Desc", "\\");
    content.put("Entered", "abcdef");
    content.put("DtSt", "g");
    content.put("Date1", "hijk");
    content.put("Date2", "lmno");
    content.put("Ctry", "pqr");
    content.put("Lang", "stu");
    content.put("MRec", "v");
    content.put("Srce", "w");
    content.put("Relf", List.of("a", "b", "c", "d"));
    content.put("Proj", List.of("e", "f"));
    content.put("CrTp", "g");
    content.put("GPub", "h");
    content.put("Form", "i");
    content.put("Indx", "j");
    content.put("SpFm", List.of("k", "l"));
    return content;
  }

  private static Map<String, Object> getMixedContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("Type", "p");
    content.put("BLvl", "a");
    content.put("Desc", "\\");
    content.put("Entered", "abcdef");
    content.put("DtSt", "g");
    content.put("Date1", "hijk");
    content.put("Date2", "lmno");
    content.put("Ctry", "pqr");
    content.put("Lang", "stu");
    content.put("MRec", "v");
    content.put("Srce", "w");
    content.put("Form", "a");
    return content;
  }

  private static Map<String, Object> getScoresContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("Type", "c");
    content.put("BLvl", "a");
    content.put("Desc", "\\");
    content.put("Entered", "abcdef");
    content.put("DtSt", "g");
    content.put("Date1", "hijk");
    content.put("Date2", "lmno");
    content.put("Ctry", "pqr");
    content.put("Lang", "stu");
    content.put("MRec", "v");
    content.put("Srce", "w");
    content.put("Comp", "ab");
    content.put("FMus", "c");
    content.put("Part", "d");
    content.put("Audn", "e");
    content.put("Form", "f");
    content.put("AccM", List.of("g", "h", "i", "j", "k", "l"));
    content.put("LTxt", List.of("m", "n"));
    content.put("TrAr", "o");
    return content;
  }

  private static Map<String, Object> getSoundContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("Type", "i");
    content.put("BLvl", "a");
    content.put("Desc", "\\");
    content.put("Entered", "abcdef");
    content.put("DtSt", "g");
    content.put("Date1", "hijk");
    content.put("Date2", "lmno");
    content.put("Ctry", "pqr");
    content.put("Lang", "stu");
    content.put("MRec", "v");
    content.put("Srce", "w");
    content.put("Comp", "ab");
    content.put("FMus", "c");
    content.put("Part", "d");
    content.put("Audn", "e");
    content.put("Form", "f");
    content.put("AccM", List.of("g", "h", "i", "j", "k", "l"));
    content.put("LTxt", List.of("m", "n"));
    content.put("TrAr", "o");
    return content;
  }

  private static Map<String, Object> getUnknownContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("Type", "h");
    content.put("BLvl", "a");
    content.put("Desc", "\\");
    content.put("Entered", "abcdef");
    content.put("DtSt", "g");
    content.put("Date1", "hijk");
    content.put("Date2", "lmno");
    content.put("Ctry", "pqr");
    content.put("Lang", "stu");
    content.put("MRec", "v");
    content.put("Srce", "w");
    content.put("Value", "abcdefghijklmnopq");
    return content;
  }

  private static Map<String, Object> getVisualContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("Type", "g");
    content.put("BLvl", "a");
    content.put("Desc", "\\");
    content.put("Entered", "abcdef");
    content.put("DtSt", "g");
    content.put("Date1", "hijk");
    content.put("Date2", "lmno");
    content.put("Ctry", "pqr");
    content.put("Lang", "stu");
    content.put("MRec", "v");
    content.put("Srce", "w");
    content.put("Time", List.of("a", "b", "c"));
    content.put("Audn", "d");
    content.put("GPub", "e");
    content.put("Form", "f");
    content.put("TMat", "g");
    content.put("Tech", "h");
    return content;
  }

  public String getDtoData() {
    return dtoData;
  }

  public Map<String, Object> getQmContent() {
    return qmContent;
  }

  public String getLeader() {
    return leader;
  }
}
