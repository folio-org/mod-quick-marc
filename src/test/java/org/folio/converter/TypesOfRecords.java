package org.folio.converter;

public enum TypesOfRecords {
  LANG('a', "Language material"),
  NOTATED('c', "Notated music"),
  MAN_NOTATED('d', "Manuscript notated music"),
  CARTOGRAPHIC('e', "Cartographic material"),
  MAN_CARTOGRAPHIC('f', "Manuscript cartographic material"),
  PROJECTED('g', "Projected medium"),
  NONMUS_SOUND('i', "Nonmusical sound recording"),
  MUS_SOUND('j', "Musical sound recording"),
  TWO_DIM('k', "Two-dimensional nonprojectable graphic"),
  COMP('m', "Computer file"),
  KIT('o', "Kit"),
  MIXED('p', "Mixed materials"),
  THREE_DIM('r', "Three-dimensional artifact or naturally occurring object"),
  SERIAL('s', "Serial/integrating resource"),
  MAN_LANG('t', "Manuscript language material"),
  UNKNOWN('b', "b");

  private char code;
  private String name;

  TypesOfRecords(char code, String name) {
    this.code = code;
    this.name = name;
  }

  public char getCode() {
    return code;
  }

  public String getName() {
    return name;
  }
}
