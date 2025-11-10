package org.folio.qm.client.model;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class PrecedingSucceedingTitle {
  public static final String PRECEDING_INSTANCE_ID_KEY = "precedingInstanceId";
  public static final String SUCCEEDING_INSTANCE_ID_KEY = "succeedingInstanceId";
  public static final String TITLE_KEY = "title";
  public static final String HRID_KEY = "hrid";
  public static final String IDENTIFIERS_KEY = "identifiers";

  @JsonProperty("id")
  private String id;
  @JsonProperty("precedingInstanceId")
  private String precedingInstanceId;
  @JsonProperty("succeedingInstanceId")
  private String succeedingInstanceId;
  @JsonProperty("title")
  private String title;
  @JsonProperty("hrid")
  private String hrid;
  @JsonProperty("identifiers")
  private List<Map<String, Object>> identifiers;

  public PrecedingSucceedingTitle(String id, String precedingInstanceId,
                                  String succeedingInstanceId, String title, String hrid,
                                  List<Map<String, Object>> identifiers) {

    this.id = id;
    this.precedingInstanceId = precedingInstanceId;
    this.succeedingInstanceId = succeedingInstanceId;
    this.title = title;
    this.hrid = hrid;
    this.identifiers = identifiers;
  }

  public static PrecedingSucceedingTitle from(Map<String, Object> rel) {
    return new PrecedingSucceedingTitle(
      (String) rel.get("id"),
      (String) rel.get(PRECEDING_INSTANCE_ID_KEY),
      (String) rel.get(SUCCEEDING_INSTANCE_ID_KEY),
      (String) rel.get(TITLE_KEY),
      (String) rel.get(HRID_KEY),
      (List<Map<String, Object>>) rel.get(IDENTIFIERS_KEY)
    );
  }

  public static PrecedingSucceedingTitle from(Map<String, Object> rel, String title, String hrid,
                                              List<Map<String, Object>> identifiers) {
    return new PrecedingSucceedingTitle(
      (String) rel.get("id"),
      (String) rel.get(PRECEDING_INSTANCE_ID_KEY),
      (String) rel.get(SUCCEEDING_INSTANCE_ID_KEY),
      title, hrid, identifiers
    );
  }

  public Map<String, Object> toPrecedingTitleJson() {
    return toJson(PRECEDING_INSTANCE_ID_KEY, precedingInstanceId);
  }

  public Map<String, Object> toSucceedingTitleJson() {
    return toJson(SUCCEEDING_INSTANCE_ID_KEY, succeedingInstanceId);
  }

  private Map<String, Object> toJson(String relatedInstanceIdKey, String relatedInstanceId) {
    Map<String, Object> json = new HashMap<>();

    includeIfPresent(json, "id", id);
    includeIfPresent(json, TITLE_KEY, title);
    includeIfPresent(json, HRID_KEY, hrid);
    includeIfPresent(json, relatedInstanceIdKey, relatedInstanceId);

    if (identifiers != null) {
      json.put(IDENTIFIERS_KEY, identifiers);
    }
    return json;
  }

  public static <T> void includeIfPresent(
    Map<String, Object> representation, String propertyName, T value) {

    if (representation != null && isNotBlank(propertyName) && value != null) {
      representation.put(propertyName, value);
    }
  }
}
