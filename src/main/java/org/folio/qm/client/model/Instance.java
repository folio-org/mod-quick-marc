package org.folio.qm.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "id",
  "_version",
  "hrid",
  "matchKey",
  "sourceUri",
  "source",
  "title",
  "indexTitle",
  "alternativeTitles",
  "editions",
  "series",
  "identifiers",
  "contributors",
  "subjects",
  "classifications",
  "publication",
  "publicationFrequency",
  "publicationRange",
  "electronicAccess",
  "dates",
  "instanceTypeId",
  "instanceFormatIds",
  "instanceFormats",
  "physicalDescriptions",
  "languages",
  "notes",
  "administrativeNotes",
  "modeOfIssuanceId",
  "catalogedDate",
  "previouslyHeld",
  "staffSuppress",
  "discoverySuppress",
  "deleted",
  "statisticalCodeIds",
  "sourceRecordFormat",
  "statusId",
  "statusUpdatedDate",
  "tags",
  "metadata",
  "holdingsRecords2",
  "natureOfContentTermIds"
})
public class Instance {

  @JsonProperty("id")
  private String id;

  @JsonProperty("_version")
  @JsonPropertyDescription("Record version for optimistic locking")
  private Integer version;

  @JsonProperty("hrid")
  private String hrid;

  @JsonProperty("matchKey")
  private String matchKey;

  @JsonProperty("sourceUri")
  private String sourceUri;

  @JsonProperty("source")
  private String source;

  @JsonProperty("title")
  private String title;

  @JsonProperty("indexTitle")
  private String indexTitle;

  @JsonProperty("alternativeTitles")
  private Set<AlternativeTitle> alternativeTitles = new LinkedHashSet<>();

  @JsonProperty("editions")
  @JsonDeserialize(as = LinkedHashSet.class)
  private Set<String> editions = new LinkedHashSet<String>();

  @JsonProperty("series")
  @JsonDeserialize(as = LinkedHashSet.class)
  private Set<Series> series = new LinkedHashSet<>();

  @JsonProperty("identifiers")
  private List<Identifier> identifiers = new ArrayList<>();

  @JsonProperty("contributors")
  private List<Contributor> contributors = new ArrayList<>();

  @JsonProperty("subjects")
  @JsonDeserialize(as = LinkedHashSet.class)
  private Set<Subject> subjects = new LinkedHashSet<>();

  @JsonProperty("classifications")
  private List<Classification> classifications = new ArrayList<>();

  @JsonProperty("publication")
  private List<Publication> publication = new ArrayList<>();

  @JsonProperty("publicationFrequency")
  @JsonDeserialize(as = LinkedHashSet.class)
  private Set<String> publicationFrequency = new LinkedHashSet<String>();

  @JsonProperty("publicationRange")
  @JsonDeserialize(as = LinkedHashSet.class)
  private Set<String> publicationRange = new LinkedHashSet<>();

  @JsonProperty("electronicAccess")
  private List<ElectronicAccess> electronicAccess = new ArrayList<>();

  @JsonProperty("dates")
  private Dates dates;

  @JsonProperty("instanceTypeId")
  private String instanceTypeId;

  @JsonProperty("instanceFormatIds")
  private List<String> instanceFormatIds = new ArrayList<String>();

  @JsonProperty("instanceFormats")
  private List<InstanceFormat> instanceFormats = new ArrayList<>();

  @JsonProperty("physicalDescriptions")
  private List<String> physicalDescriptions = new ArrayList<String>();

  @JsonProperty("languages")
  private List<String> languages = new ArrayList<String>();

  @JsonProperty("notes")
  private List<Note> notes = new ArrayList<>();

  @JsonProperty("administrativeNotes")
  private List<String> administrativeNotes = new ArrayList<String>();

  @JsonProperty("modeOfIssuanceId")
  private String modeOfIssuanceId;

  @JsonProperty("catalogedDate")
  private String catalogedDate;

  @JsonProperty("previouslyHeld")
  private Boolean previouslyHeld = false;

  @JsonProperty("staffSuppress")
  private Boolean staffSuppress;

  @JsonProperty("discoverySuppress")
  @JsonPropertyDescription("Records the fact that the record should not be displayed in a discovery system")
  private Boolean discoverySuppress = false;

  @JsonProperty("deleted")
  @JsonPropertyDescription("Indicates whether the record was marked for deletion")
  private Boolean deleted = false;

  @JsonProperty("statisticalCodeIds")
  @JsonDeserialize(as = LinkedHashSet.class)
  private Set<String> statisticalCodeIds = new LinkedHashSet<String>();

  @JsonProperty("sourceRecordFormat")
  private SourceRecordFormat sourceRecordFormat;

  @JsonProperty("statusId")
  private String statusId;

  @JsonProperty("statusUpdatedDate")
  private String statusUpdatedDate;

  @JsonProperty("tags")
  private Tags tags;

  @JsonProperty("metadata")
  private Metadata metadata;

  @JsonProperty("holdingsRecords2")
  private List<HoldingsRecord> holdingsRecords2 = new ArrayList<>();

  @JsonProperty("natureOfContentTermIds")
  @JsonDeserialize(as = LinkedHashSet.class)
  private Set<String> natureOfContentTermIds = new LinkedHashSet<String>();

  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  public Instance withId(String id) {
    this.id = id;
    return this;
  }

  @JsonProperty("_version")
  public Integer getVersion() {
    return version;
  }

  @JsonProperty("_version")
  public void setVersion(Integer version) {
    this.version = version;
  }

  public Instance withVersion(Integer version) {
    this.version = version;
    return this;
  }

  @JsonProperty("hrid")
  public String getHrid() {
    return hrid;
  }

  @JsonProperty("hrid")
  public void setHrid(String hrid) {
    this.hrid = hrid;
  }

  public Instance withHrid(String hrid) {
    this.hrid = hrid;
    return this;
  }

  @JsonProperty("matchKey")
  public String getMatchKey() {
    return matchKey;
  }

  @JsonProperty("matchKey")
  public void setMatchKey(String matchKey) {
    this.matchKey = matchKey;
  }

  public Instance withMatchKey(String matchKey) {
    this.matchKey = matchKey;
    return this;
  }

  @JsonProperty("sourceUri")
  public String getSourceUri() {
    return sourceUri;
  }

  @JsonProperty("sourceUri")
  public void setSourceUri(String sourceUri) {
    this.sourceUri = sourceUri;
  }

  public Instance withSourceUri(String sourceUri) {
    this.sourceUri = sourceUri;
    return this;
  }

  @JsonProperty("source")
  public String getSource() {
    return source;
  }

  @JsonProperty("source")
  public void setSource(String source) {
    this.source = source;
  }

  public Instance withSource(String source) {
    this.source = source;
    return this;
  }

  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  public Instance withTitle(String title) {
    this.title = title;
    return this;
  }

  @JsonProperty("indexTitle")
  public String getIndexTitle() {
    return indexTitle;
  }

  @JsonProperty("indexTitle")
  public void setIndexTitle(String indexTitle) {
    this.indexTitle = indexTitle;
  }

  public Instance withIndexTitle(String indexTitle) {
    this.indexTitle = indexTitle;
    return this;
  }

  @JsonProperty("alternativeTitles")
  public Set<AlternativeTitle> getAlternativeTitles() {
    return alternativeTitles;
  }

  @JsonProperty("alternativeTitles")
  public void setAlternativeTitles(Set<AlternativeTitle> alternativeTitles) {
    this.alternativeTitles = alternativeTitles;
  }

  public Instance withAlternativeTitles(Set<AlternativeTitle> alternativeTitles) {
    this.alternativeTitles = alternativeTitles;
    return this;
  }

  @JsonProperty("editions")
  public Set<String> getEditions() {
    return editions;
  }

  @JsonProperty("editions")
  public void setEditions(Set<String> editions) {
    this.editions = editions;
  }

  public Instance withEditions(Set<String> editions) {
    this.editions = editions;
    return this;
  }

  @JsonProperty("series")
  public Set<Series> getSeries() {
    return series;
  }

  @JsonProperty("series")
  public void setSeries(Set<Series> series) {
    this.series = series;
  }

  public Instance withSeries(Set<Series> series) {
    this.series = series;
    return this;
  }

  @JsonProperty("identifiers")
  public List<Identifier> getIdentifiers() {
    return identifiers;
  }

  @JsonProperty("identifiers")
  public void setIdentifiers(List<Identifier> identifiers) {
    this.identifiers = identifiers;
  }

  public Instance withIdentifiers(List<Identifier> identifiers) {
    this.identifiers = identifiers;
    return this;
  }

  @JsonProperty("contributors")
  public List<Contributor> getContributors() {
    return contributors;
  }

  @JsonProperty("contributors")
  public void setContributors(List<Contributor> contributors) {
    this.contributors = contributors;
  }

  public Instance withContributors(List<Contributor> contributors) {
    this.contributors = contributors;
    return this;
  }

  @JsonProperty("subjects")
  public Set<Subject> getSubjects() {
    return subjects;
  }

  @JsonProperty("subjects")
  public void setSubjects(Set<Subject> subjects) {
    this.subjects = subjects;
  }

  public Instance withSubjects(Set<Subject> subjects) {
    this.subjects = subjects;
    return this;
  }

  @JsonProperty("classifications")
  public List<Classification> getClassifications() {
    return classifications;
  }

  @JsonProperty("classifications")
  public void setClassifications(List<Classification> classifications) {
    this.classifications = classifications;
  }

  public Instance withClassifications(List<Classification> classifications) {
    this.classifications = classifications;
    return this;
  }

  @JsonProperty("publication")
  public List<Publication> getPublication() {
    return publication;
  }

  @JsonProperty("publication")
  public void setPublication(List<Publication> publication) {
    this.publication = publication;
  }

  public Instance withPublication(List<Publication> publication) {
    this.publication = publication;
    return this;
  }

  @JsonProperty("publicationFrequency")
  public Set<String> getPublicationFrequency() {
    return publicationFrequency;
  }

  @JsonProperty("publicationFrequency")
  public void setPublicationFrequency(Set<String> publicationFrequency) {
    this.publicationFrequency = publicationFrequency;
  }

  public Instance withPublicationFrequency(Set<String> publicationFrequency) {
    this.publicationFrequency = publicationFrequency;
    return this;
  }

  @JsonProperty("publicationRange")
  public Set<String> getPublicationRange() {
    return publicationRange;
  }

  @JsonProperty("publicationRange")
  public void setPublicationRange(Set<String> publicationRange) {
    this.publicationRange = publicationRange;
  }

  public Instance withPublicationRange(Set<String> publicationRange) {
    this.publicationRange = publicationRange;
    return this;
  }

  @JsonProperty("electronicAccess")
  public List<ElectronicAccess> getElectronicAccess() {
    return electronicAccess;
  }

  @JsonProperty("electronicAccess")
  public void setElectronicAccess(List<ElectronicAccess> electronicAccess) {
    this.electronicAccess = electronicAccess;
  }

  public Instance withElectronicAccess(List<ElectronicAccess> electronicAccess) {
    this.electronicAccess = electronicAccess;
    return this;
  }

  @JsonProperty("dates")
  public Dates getDates() {
    return dates;
  }

  @JsonProperty("dates")
  public void setDates(Dates dates) {
    this.dates = dates;
  }

  public Instance withDates(Dates dates) {
    this.dates = dates;
    return this;
  }

  @JsonProperty("instanceTypeId")
  public String getInstanceTypeId() {
    return instanceTypeId;
  }

  @JsonProperty("instanceTypeId")
  public void setInstanceTypeId(String instanceTypeId) {
    this.instanceTypeId = instanceTypeId;
  }

  public Instance withInstanceTypeId(String instanceTypeId) {
    this.instanceTypeId = instanceTypeId;
    return this;
  }

  @JsonProperty("instanceFormatIds")
  public List<String> getInstanceFormatIds() {
    return instanceFormatIds;
  }

  @JsonProperty("instanceFormatIds")
  public void setInstanceFormatIds(List<String> instanceFormatIds) {
    this.instanceFormatIds = instanceFormatIds;
  }

  public Instance withInstanceFormatIds(List<String> instanceFormatIds) {
    this.instanceFormatIds = instanceFormatIds;
    return this;
  }

  @JsonProperty("instanceFormats")
  public List<InstanceFormat> getInstanceFormats() {
    return instanceFormats;
  }

  @JsonProperty("instanceFormats")
  public void setInstanceFormats(List<InstanceFormat> instanceFormats) {
    this.instanceFormats = instanceFormats;
  }

  public Instance withInstanceFormats(List<InstanceFormat> instanceFormats) {
    this.instanceFormats = instanceFormats;
    return this;
  }

  @JsonProperty("physicalDescriptions")
  public List<String> getPhysicalDescriptions() {
    return physicalDescriptions;
  }

  @JsonProperty("physicalDescriptions")
  public void setPhysicalDescriptions(List<String> physicalDescriptions) {
    this.physicalDescriptions = physicalDescriptions;
  }

  public Instance withPhysicalDescriptions(List<String> physicalDescriptions) {
    this.physicalDescriptions = physicalDescriptions;
    return this;
  }

  @JsonProperty("languages")
  public List<String> getLanguages() {
    return languages;
  }

  @JsonProperty("languages")
  public void setLanguages(List<String> languages) {
    this.languages = languages;
  }

  public Instance withLanguages(List<String> languages) {
    this.languages = languages;
    return this;
  }

  @JsonProperty("notes")
  public List<Note> getNotes() {
    return notes;
  }

  @JsonProperty("notes")
  public void setNotes(List<Note> notes) {
    this.notes = notes;
  }

  public Instance withNotes(List<Note> notes) {
    this.notes = notes;
    return this;
  }

  @JsonProperty("administrativeNotes")
  public List<String> getAdministrativeNotes() {
    return administrativeNotes;
  }

  @JsonProperty("administrativeNotes")
  public void setAdministrativeNotes(List<String> administrativeNotes) {
    this.administrativeNotes = administrativeNotes;
  }

  public Instance withAdministrativeNotes(List<String> administrativeNotes) {
    this.administrativeNotes = administrativeNotes;
    return this;
  }

  @JsonProperty("modeOfIssuanceId")
  public String getModeOfIssuanceId() {
    return modeOfIssuanceId;
  }

  @JsonProperty("modeOfIssuanceId")
  public void setModeOfIssuanceId(String modeOfIssuanceId) {
    this.modeOfIssuanceId = modeOfIssuanceId;
  }

  public Instance withModeOfIssuanceId(String modeOfIssuanceId) {
    this.modeOfIssuanceId = modeOfIssuanceId;
    return this;
  }

  @JsonProperty("catalogedDate")
  public String getCatalogedDate() {
    return catalogedDate;
  }

  @JsonProperty("catalogedDate")
  public void setCatalogedDate(String catalogedDate) {
    this.catalogedDate = catalogedDate;
  }

  public Instance withCatalogedDate(String catalogedDate) {
    this.catalogedDate = catalogedDate;
    return this;
  }

  @JsonProperty("previouslyHeld")
  public Boolean getPreviouslyHeld() {
    return previouslyHeld;
  }

  @JsonProperty("previouslyHeld")
  public void setPreviouslyHeld(Boolean previouslyHeld) {
    this.previouslyHeld = previouslyHeld;
  }

  public Instance withPreviouslyHeld(Boolean previouslyHeld) {
    this.previouslyHeld = previouslyHeld;
    return this;
  }

  @JsonProperty("staffSuppress")
  public Boolean getStaffSuppress() {
    return staffSuppress;
  }

  @JsonProperty("staffSuppress")
  public void setStaffSuppress(Boolean staffSuppress) {
    this.staffSuppress = staffSuppress;
  }

  public Instance withStaffSuppress(Boolean staffSuppress) {
    this.staffSuppress = staffSuppress;
    return this;
  }

  @JsonProperty("discoverySuppress")
  public Boolean getDiscoverySuppress() {
    return discoverySuppress;
  }

  @JsonProperty("discoverySuppress")
  public void setDiscoverySuppress(Boolean discoverySuppress) {
    this.discoverySuppress = discoverySuppress;
  }

  public Instance withDiscoverySuppress(Boolean discoverySuppress) {
    this.discoverySuppress = discoverySuppress;
    return this;
  }

  @JsonProperty("deleted")
  public Boolean getDeleted() {
    return deleted;
  }

  @JsonProperty("deleted")
  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }

  public Instance withDeleted(Boolean deleted) {
    this.deleted = deleted;
    return this;
  }

  @JsonProperty("statisticalCodeIds")
  public Set<String> getStatisticalCodeIds() {
    return statisticalCodeIds;
  }

  @JsonProperty("statisticalCodeIds")
  public void setStatisticalCodeIds(Set<String> statisticalCodeIds) {
    this.statisticalCodeIds = statisticalCodeIds;
  }

  public Instance withStatisticalCodeIds(Set<String> statisticalCodeIds) {
    this.statisticalCodeIds = statisticalCodeIds;
    return this;
  }

  @JsonProperty("sourceRecordFormat")
  public SourceRecordFormat getSourceRecordFormat() {
    return sourceRecordFormat;
  }

  @JsonProperty("sourceRecordFormat")
  public void setSourceRecordFormat(SourceRecordFormat sourceRecordFormat) {
    this.sourceRecordFormat = sourceRecordFormat;
  }

  public Instance withSourceRecordFormat(SourceRecordFormat sourceRecordFormat) {
    this.sourceRecordFormat = sourceRecordFormat;
    return this;
  }

  @JsonProperty("statusId")
  public String getStatusId() {
    return statusId;
  }

  @JsonProperty("statusId")
  public void setStatusId(String statusId) {
    this.statusId = statusId;
  }

  public Instance withStatusId(String statusId) {
    this.statusId = statusId;
    return this;
  }

  @JsonProperty("statusUpdatedDate")
  public String getStatusUpdatedDate() {
    return statusUpdatedDate;
  }

  @JsonProperty("statusUpdatedDate")
  public void setStatusUpdatedDate(String statusUpdatedDate) {
    this.statusUpdatedDate = statusUpdatedDate;
  }

  public Instance withStatusUpdatedDate(String statusUpdatedDate) {
    this.statusUpdatedDate = statusUpdatedDate;
    return this;
  }

  @JsonProperty("tags")
  public Tags getTags() {
    return tags;
  }

  @JsonProperty("tags")
  public void setTags(Tags tags) {
    this.tags = tags;
  }

  public Instance withTags(Tags tags) {
    this.tags = tags;
    return this;
  }

  @JsonProperty("metadata")
  public Metadata getMetadata() {
    return metadata;
  }

  @JsonProperty("metadata")
  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  public Instance withMetadata(Metadata metadata) {
    this.metadata = metadata;
    return this;
  }

  @JsonProperty("holdingsRecords2")
  public List<HoldingsRecord> getHoldingsRecords2() {
    return holdingsRecords2;
  }

  @JsonProperty("holdingsRecords2")
  public void setHoldingsRecords2(List<HoldingsRecord> holdingsRecords2) {
    this.holdingsRecords2 = holdingsRecords2;
  }

  public Instance withHoldingsRecords2(List<HoldingsRecord> holdingsRecords2) {
    this.holdingsRecords2 = holdingsRecords2;
    return this;
  }

  @JsonProperty("natureOfContentTermIds")
  public Set<String> getNatureOfContentTermIds() {
    return natureOfContentTermIds;
  }

  @JsonProperty("natureOfContentTermIds")
  public void setNatureOfContentTermIds(Set<String> natureOfContentTermIds) {
    this.natureOfContentTermIds = natureOfContentTermIds;
  }

  public Instance withNatureOfContentTermIds(Set<String> natureOfContentTermIds) {
    this.natureOfContentTermIds = natureOfContentTermIds;
    return this;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(metadata).append(notes).append(previouslyHeld).append(instanceFormats)
      .append(modeOfIssuanceId).append(catalogedDate).append(source).append(title).append(indexTitle)
      .append(publicationFrequency).append(electronicAccess).append(statisticalCodeIds).append(statusUpdatedDate)
      .append(natureOfContentTermIds).append(hrid).append(sourceUri).append(instanceFormatIds).append(publication)
      .append(sourceRecordFormat).append(id).append(alternativeTitles).append(physicalDescriptions).append(languages)
      .append(identifiers).append(instanceTypeId).append(subjects).append(holdingsRecords2).append(matchKey)
      .append(dates).append(version).append(tags).append(classifications).append(publicationRange).append(editions)
      .append(discoverySuppress).append(deleted).append(statusId).append(series).append(staffSuppress)
      .append(contributors).append(administrativeNotes).toHashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof Instance rhs)) {
      return false;
    }
    return new EqualsBuilder().append(metadata, rhs.metadata).append(notes, rhs.notes)
      .append(previouslyHeld, rhs.previouslyHeld).append(instanceFormats, rhs.instanceFormats)
      .append(modeOfIssuanceId, rhs.modeOfIssuanceId).append(catalogedDate, rhs.catalogedDate)
      .append(source, rhs.source).append(title, rhs.title).append(indexTitle, rhs.indexTitle)
      .append(publicationFrequency, rhs.publicationFrequency).append(electronicAccess, rhs.electronicAccess)
      .append(statisticalCodeIds, rhs.statisticalCodeIds).append(statusUpdatedDate, rhs.statusUpdatedDate)
      .append(natureOfContentTermIds, rhs.natureOfContentTermIds).append(hrid, rhs.hrid)
      .append(sourceUri, rhs.sourceUri).append(instanceFormatIds, rhs.instanceFormatIds)
      .append(publication, rhs.publication).append(sourceRecordFormat, rhs.sourceRecordFormat)
      .append(id, rhs.id).append(alternativeTitles, rhs.alternativeTitles)
      .append(physicalDescriptions, rhs.physicalDescriptions).append(languages, rhs.languages)
      .append(identifiers, rhs.identifiers).append(instanceTypeId, rhs.instanceTypeId)
      .append(subjects, rhs.subjects).append(holdingsRecords2, rhs.holdingsRecords2).append(matchKey, rhs.matchKey)
      .append(dates, rhs.dates).append(version, rhs.version).append(tags, rhs.tags)
      .append(classifications, rhs.classifications).append(publicationRange, rhs.publicationRange)
      .append(editions, rhs.editions).append(discoverySuppress, rhs.discoverySuppress)
      .append(deleted, rhs.deleted).append(statusId, rhs.statusId).append(series, rhs.series)
      .append(staffSuppress, rhs.staffSuppress).append(contributors, rhs.contributors)
      .append(administrativeNotes, rhs.administrativeNotes).isEquals();
  }

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
