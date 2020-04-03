package org.folio.converter;

public enum FixedLengthControlFieldItems {
  ENTERED("Entered", 0, 6, false),
  DTST("DtSt", 6, 1, false),
  DATE1("Date1", 7, 4, false),
  DATE2("Date2", 11, 4, false),
  CTRY("Ctry", 15, 3, false),
  VALUE("Value", 18, 17, false),
  ILLS("Ills", 18, 4, true),
  FREQ("Freq", 18, 1, false),
  RELF("Relf", 18, 4, true),
  COMP("Comp", 18, 2, false),
  TIME("Time", 18, 3, true),
  REGL("Regl", 19, 1, false),
  FMUS("FMus", 20, 1, false),
  SRTP("SrTp", 21, 1, false),
  PART("Part", 21, 1, false),
  AUDN("Audn", 22, 1, false),
  ORIG("Orig", 22, 1, false),
  PROJ("Proj", 22, 2, false),
  FORM("Form", 23, 1, false),
  CONT_B("Cont", 24, 4, true),
  ENTW("EntW", 24, 1, false),
  ACCM("AccM", 24, 6, true),
  CONT_C("Cont", 25, 3, true),
  CRTP("CrTp", 25, 1, false),
  FILE("File", 26, 1, false),
  GPUB("GPub", 28, 1, false),
  CONF("Conf", 29, 1, false),
  FORM_MV("Form", 29, 1, false),
  FEST("Fest",30, 1, false),
  LTXT("LTxt", 30, 2, true),
  INDX("Indx", 31, 1, false),
  LITF("LitF", 33, 1 ,false),
  ALPH("Alph", 33, 1, false),
  SPFM("SpFm", 33, 2, false),
  TRAR("TrAr", 33, 1, false),
  TMAT("TMat", 33, 1, false),
  BIOG("Biog", 34, 1, false),
  SL("S/L", 34, 1, false),
  TECH("Tech", 34, 1, false),
  LANG("Lang", 35, 3, false),
  MREC("MRec", 38, 1, false),
  SRCE("Srce", 39, 1, false);

  private String name;
  private int position;
  private int length;
  private boolean array;

  FixedLengthControlFieldItems(String name, int position, int length, boolean array) {
    this.name = name;
    this.position = position;
    this.length = length;
    this.array = array;
  }

  public String getName() {
    return name;
  }

  public int getPosition() {
    return position;
  }

  public int getLength() {
    return length;
  }

  public boolean isArray() {
    return array;
  }
}
