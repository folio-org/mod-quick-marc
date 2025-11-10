package org.folio.qm.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.folio.AlternativeTitle;
import org.folio.Classification;
import org.folio.Contributor;
import org.folio.Dates;
import org.folio.ElectronicAccess;
import org.folio.Identifier;
import org.folio.InstanceFormat;
import org.folio.Metadata;
import org.folio.Note;
import org.folio.Publication;
import org.folio.Series;
import org.folio.Subject;
import org.folio.Tags;
import org.folio.qm.domain.entity.HoldingsRecord;

@Data
@NoArgsConstructor
public class Instance {

  private String id;
  @JsonProperty("_version")
  private Integer version;
  private String hrid;
  private String matchKey;
  private String sourceUri;
  private String source;
  private String title;
  private String indexTitle;
  private Set<AlternativeTitle> alternativeTitles;
  private Set<String> editions;
  private Set<Series> series;
  private List<Identifier> identifiers;
  private List<Contributor> contributors;
  private Set<Subject> subjects;
  private List<Classification> classifications;
  private List<Publication> publication;
  private Set<String> publicationFrequency;
  private Set<String> publicationRange;
  private List<ElectronicAccess> electronicAccess;
  private Dates dates;
  private String instanceTypeId;
  private List<String> instanceFormatIds;
  private List<InstanceFormat> instanceFormats;
  private List<String> physicalDescriptions;
  private List<String> languages;
  private List<Note> notes;
  private List<String> administrativeNotes;
  private String modeOfIssuanceId;
  private String catalogedDate;
  private Boolean previouslyHeld = false;
  private Boolean staffSuppress;
  private Boolean discoverySuppress = false;
  private Boolean deleted = false;
  private Set<String> statisticalCodeIds;
  private SourceRecordFormat sourceRecordFormat;
  private String statusId;
  private String statusUpdatedDate;
  private Tags tags;
  private Metadata metadata;
  private List<HoldingsRecord> holdingsRecords2;
  private Set<String> natureOfContentTermIds;
  @JsonIgnore
  private List<PrecedingSucceedingTitle> precedingTitles;
  @JsonIgnore
  private List<PrecedingSucceedingTitle> succeedingTitles;

  public enum SourceRecordFormat {

    MARC_JSON("MARC-JSON");
    private static final Map<String, SourceRecordFormat> CONSTANTS = new HashMap<>();
    private final String value;

    static {
      for (SourceRecordFormat c : values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    SourceRecordFormat(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return this.value;
    }

    @JsonValue
    public String value() {
      return this.value;
    }

    @JsonCreator
    public static SourceRecordFormat fromValue(String value) {
      SourceRecordFormat constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }
  }
}
