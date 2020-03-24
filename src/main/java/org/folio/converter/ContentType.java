package org.folio.converter;

public enum ContentType {
  BOOKS("Books"),
  FILES("Computer Files"),
  CONTINUING("Continuing Resources"),
  MAPS("Maps"),
  MIXED("Mixed Materials"),
  SCORES("Scores"),
  SOUND("Sound Recordings"),
  VISUAL("Visual Materials"),
  UNKNOWN("Unknown Type");

  private String name;

  ContentType(String name){
    this.name = name;
  }

  public String getName(){
    return name;
  }

  public static ContentType getByName(String name) {
    for(ContentType type: values()) {
      if(name.equals(type.name)) return type;
    }
    return UNKNOWN;
  }

  public static ContentType detectContentType(String type, String bLvl) {
    switch (type) {
      case "a":
        switch(bLvl) {
          case "b":
          case "i":
          case "s":
            return CONTINUING;
        }
      case "t":
        return BOOKS;
      case "m":
        return FILES;
      case "e":
      case "f":
        return MAPS;
      case "p":
        return MIXED;
      case "i":
      case "j":
        return SOUND;
      case "c":
      case "d":
        return SCORES;
      case "g":
      case "k":
      case "o":
      case "r":
        return VISUAL;
      default:
        return UNKNOWN;
    }
  }
}
