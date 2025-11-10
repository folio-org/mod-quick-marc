package org.folio.qm.client.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "id",
  "name",
  "source",
  "metadata"
})
public class HoldingsRecordsSource {

  @JsonProperty("id")
  private String id;

  @JsonProperty("name")
  private String name;

  @JsonProperty("source")
  @JsonPropertyDescription("The holdings records source")
  private Source source;

  @JsonProperty("metadata")
  private Metadata metadata;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  public HoldingsRecordsSource withId(String id) {
    this.id = id;
    return this;
  }

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public HoldingsRecordsSource withName(String name) {
    this.name = name;
    return this;
  }

  @JsonProperty("source")
  public Source getSource() {
    return source;
  }

  @JsonProperty("source")
  public void setSource(Source source) {
    this.source = source;
  }

  public HoldingsRecordsSource withSource(Source source) {
    this.source = source;
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

  public HoldingsRecordsSource withMetadata(Metadata metadata) {
    this.metadata = metadata;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  public HoldingsRecordsSource withAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(name).append(metadata).append(id).append(source)
      .append(additionalProperties).toHashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof HoldingsRecordsSource rhs)) {
      return false;
    }
    return new EqualsBuilder().append(name, rhs.name).append(metadata, rhs.metadata).append(id, rhs.id)
      .append(source, rhs.source).append(additionalProperties, rhs.additionalProperties).isEquals();
  }

  public enum Source {

    FOLIO("folio"),
    LOCAL("local"),
    CONSORTIUM("consortium");
    private static final Map<String, Source> CONSTANTS = new HashMap<>();
    private final String value;

    static {
      for (Source c : values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    Source(String value) {
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
    public static Source fromValue(String value) {
      Source constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }
  }
}
