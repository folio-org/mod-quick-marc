package org.folio.qm.support.utils.testdata;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public enum Tag006FieldTestData {

  BOOKS("aabcdefghijklmn op", getBooksContent()),
  BOOKS_WITH_EMPTY_ITEMS("aabcde     klmn op", getBooksWithEmptyItemsContent()),
  CONTINUING("sab cdefghijk   lm", getContinuingContent()),
  FILED("m    ab  c d      ", getFiledContent()),
  MAPS("eabcdef g  hi j kl", getMapsContent()),
  MIXED("p     a           ", getMixedContent()),
  SCORES("cabcdefghijklmn o ", getScoresContent()),
  SOUND("iabcdefghijklmn o ", getSoundContent()),
  UNKNOWN("babcdefghijklmnopq", getUnknownContent()),
  VISUAL("gabc d     ef   gh", getVisualContent());

  private final String dtoData;
  private final Map<String, Object> qmContent;

  Tag006FieldTestData(String dtoData, Map<String, Object> qmContent) {
    this.dtoData = dtoData;
    this.qmContent = qmContent;
  }

  private static Map<String, Object> getBooksContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("Type", "a");
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
    content.put("Audn", "a");
    content.put("Form", "b");
    content.put("File", "c");
    content.put("GPub", "d");
    return content;
  }

  private static Map<String, Object> getMapsContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("Type", "e");
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
    content.put("Form", "a");
    return content;
  }

  private static Map<String, Object> getScoresContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("Type", "c");
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
    content.put("Type", "b");
    content.put("Value", "abcdefghijklmnopq");
    return content;
  }

  private static Map<String, Object> getVisualContent() {
    Map<String, Object> content = new LinkedHashMap<>();
    content.put("Type", "g");
    content.put("Time", List.of("a", "b", "c"));
    content.put("Audn", "d");
    content.put("GPub", "e");
    content.put("Form", "f");
    content.put("TMat", "g");
    content.put("Tech", "h");
    return content;
  }

}
