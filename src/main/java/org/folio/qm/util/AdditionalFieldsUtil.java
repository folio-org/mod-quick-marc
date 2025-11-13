package org.folio.qm.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.qm.client.model.Record;
import org.marc4j.MarcJsonReader;
import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.VariableField;

public final class AdditionalFieldsUtil {

  public static final String TAG_00X_PREFIX = "00";
  public static final String TAG_999 = "999";
  public static final String TAG_001 = "001";
  public static final String TAG_005 = "005";
  public static final char INDICATOR = 'f';
  public static final char SUBFIELD_I = 'i';
  public static final String FIELDS = "fields";

  private static final Logger LOGGER = LogManager.getLogger();
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private AdditionalFieldsUtil() {
  }

  public static boolean addFieldToMarcRecord(Record record, String field, char subfield, String value) {
    boolean result = false;
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      if (record != null
        && record.getParsedRecord() != null
        && record.getParsedRecord().getContent() != null) {

        MarcWriter streamWriter = new MarcStreamWriter(new ByteArrayOutputStream());
        MarcJsonWriter jsonWriter = new MarcJsonWriter(os);
        MarcFactory factory = MarcFactory.newInstance();
        org.marc4j.marc.Record marcRecord = computeMarcRecord(record);

        if (marcRecord != null) {
          VariableField variableField =
            getSingleFieldByIndicators(marcRecord.getVariableFields(field), INDICATOR, INDICATOR);
          DataField dataField;

          if (variableField != null
            && ((DataField) variableField).getIndicator1() == INDICATOR
            && ((DataField) variableField).getIndicator2() == INDICATOR) {
            dataField = (DataField) variableField;
            marcRecord.removeVariableField(variableField);
            dataField.removeSubfield(dataField.getSubfield(subfield));
          } else {
            dataField = factory.newDataField(field, INDICATOR, INDICATOR);
          }

          dataField.addSubfield(factory.newSubfield(subfield, value));
          marcRecord.addVariableField(dataField);

          // Recalculate leader
          streamWriter.write(marcRecord);
          jsonWriter.write(marcRecord);

          String parsedContentString = new JsonObject(os.toString()).encode();
          // Update the record directly, no caching
          var content = reorderMarcRecordFields(
            record.getParsedRecord().getContent().toString(),
            parsedContentString
          );

          record.setParsedRecord(record.getParsedRecord().setContent(content));
          result = true;
        }
      }
    } catch (Exception e) {
      LOGGER.warn("addFieldToMarcRecord:: Failed to add additional subfield {} for field {} to record {}",
        subfield, field, record.getId(), e);
    }
    return result;
  }

  /**
   * Take field values from system modified record content while preserving incoming record content`s field order.
   * Put system fields (001, 005) first, regardless of incoming record fields order.
   *
   * @param sourceOrderContent content with incoming record fields order
   * @param systemOrderContent system modified record content with reordered fields
   * @return MARC record parsed content with desired fields order
   */
  public static String reorderMarcRecordFields(String sourceOrderContent, String systemOrderContent) {
    try {
      var parsedContent = OBJECT_MAPPER.readTree(systemOrderContent);
      var fieldsArrayNode = (ArrayNode) parsedContent.path(FIELDS);

      var nodes = toNodeList(fieldsArrayNode);
      var nodes00X = removeAndGetNodesByTagPrefix(nodes, TAG_00X_PREFIX);
      var sourceOrderTags = getSourceFields(sourceOrderContent);
      var reorderedFields = OBJECT_MAPPER.createArrayNode();

      var node001 = removeAndGetNodeByTag(nodes00X, TAG_001);
      if (node001 != null && !node001.isEmpty()) {
        reorderedFields.add(node001);
      }

      var node005 = removeAndGetNodeByTag(nodes00X, TAG_005);
      if (node005 != null && !node005.isEmpty()) {
        reorderedFields.add(node005);
      }

      for (String tag : sourceOrderTags) {
        var nodeTag = tag;
        //loop will add system generated fields that are absent in initial record, preserving their order, f.e. 035
        do {
          var node = tag.startsWith(TAG_00X_PREFIX) ? removeAndGetNodeByTag(nodes00X, tag) : nodes.remove(0);
          if (node != null && !node.isEmpty()) {
            nodeTag = getTagFromNode(node);
            reorderedFields.add(node);
          }
        } while (!tag.equals(nodeTag) && !nodes.isEmpty());
      }

      reorderedFields.addAll(nodes);

      ((ObjectNode) parsedContent).set(FIELDS, reorderedFields);
      return parsedContent.toString();
    } catch (Exception e) {
      LOGGER.error("An error occurred while reordering Marc record fields: {}", e.getMessage(), e);
      return systemOrderContent;
    }
  }

  private static List<JsonNode> toNodeList(ArrayNode fieldsArrayNode) {
    var nodes = new LinkedList<JsonNode>();
    for (var node : fieldsArrayNode) {
      nodes.add(node);
    }
    return nodes;
  }

  private static JsonNode removeAndGetNodeByTag(List<JsonNode> nodes, String tag) {
    for (int i = 0; i < nodes.size(); i++) {
      var nodeTag = getTagFromNode(nodes.get(i));
      if (nodeTag.equals(tag)) {
        return nodes.remove(i);
      }
    }
    return null;
  }

  private static List<JsonNode> removeAndGetNodesByTagPrefix(List<JsonNode> nodes, String prefix) {
    var startsWithNodes = new LinkedList<JsonNode>();
    for (int i = 0; i < nodes.size(); i++) {
      var nodeTag = getTagFromNode(nodes.get(i));
      if (nodeTag.startsWith(prefix)) {
        startsWithNodes.add(nodes.get(i));
      }
    }

    nodes.removeAll(startsWithNodes);
    return startsWithNodes;
  }

  private static String getTagFromNode(JsonNode node) {
    return node.fieldNames().next();
  }

  private static List<String> getSourceFields(String source) {
    var sourceFields = new ArrayList<String>();
    var remainingFields = new ArrayList<String>();
    var has001 = false;
    try {
      var sourceJson = OBJECT_MAPPER.readTree(source);
      var fieldsNode = sourceJson.get(FIELDS);

      for (JsonNode fieldNode : fieldsNode) {
        var tag = getTagFromNode(fieldNode);
        if (tag.equals(TAG_001)) {
          sourceFields.add(0, tag);
          has001 = true;
        } else if (tag.equals(TAG_005)) {
          if (!has001) {
            sourceFields.add(0, tag);
          } else {
            sourceFields.add(1, tag);
          }
        } else {
          remainingFields.add(tag);
        }
      }
      sourceFields.addAll(remainingFields);
    } catch (Exception e) {
      LOGGER.error("An error occurred while parsing source JSON: {}", e.getMessage(), e);
    }
    return sourceFields;
  }

  public static String getControlFieldValue(Record record, String tag) {
    org.marc4j.marc.Record marcRecord = computeMarcRecord(record);
    if (marcRecord != null) {
      try {
        return marcRecord.getControlFields().stream()
          .filter(controlField -> controlField.getTag().equals(tag))
          .findFirst()
          .map(ControlField::getData)
          .orElse(null);
      } catch (Exception e) {
        LOGGER.warn("getControlFieldValue:: Error during the search a field in the record", e);
        return null;
      }
    }
    return null;
  }

  public static String getValue(Record record, String tag, char subfield) {
    org.marc4j.marc.Record marcRecord = computeMarcRecord(record);
    if (marcRecord != null) {
      try {
        for (VariableField field : marcRecord.getVariableFields(tag)) {
          if (field instanceof DataField) {
            if (CollectionUtils.isNotEmpty(((DataField) field).getSubfields(subfield))) {
              return ((DataField) field).getSubfields(subfield).get(0).getData();
            }
          } else if (field instanceof ControlField) {
            return ((ControlField) field).getData();
          }
        }
      } catch (Exception e) {
        LOGGER.warn("getValue:: Error during the search a field in the record", e);
        return null;
      }
    }
    return null;
  }

  /**
   * Generate a {@link org.marc4j.marc.Record} from {@link Record} passed in.
   * Will return null when there is no parsed content string present. Generated MARC record will be saved into cache if
   * its parsed content string is not present in the cache as a key
   */
  private static org.marc4j.marc.Record computeMarcRecord(Record record) {
    try (ByteArrayInputStream bais = new ByteArrayInputStream(record.getParsedRecord().getContent().toString()
      .getBytes(StandardCharsets.UTF_8))) {
      MarcJsonReader reader = new MarcJsonReader(bais);
      if (reader.hasNext()) {
        return reader.next();
      }
    } catch (Exception e) {
      LOGGER.warn("computeMarcRecord:: Error during the transformation to marc record", e);
    }
    return null;
  }

  private static VariableField getSingleFieldByIndicators(List<VariableField> list, char ind1, char ind2) {
    if (list == null || list.isEmpty()) {
      return null;
    }
    return list.stream()
      .filter(f -> ((DataField) f).getIndicator1() == ind1 && ((DataField) f).getIndicator2() == ind2)
      .findFirst()
      .orElse(null);
  }
}
