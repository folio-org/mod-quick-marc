package org.folio.qm.util;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.qm.client.model.Record;
import org.marc4j.MarcJsonReader;
import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

public final class AdditionalFieldsUtil {

  public static final String TAG_999 = "999";
  public static final String TAG_001 = "001";
  public static final String TAG_003 = "003";
  public static final char SUBFIELD_I = 'i';

  private static final char INDICATOR = 'f';
  private static final String TAG_00X_PREFIX = "00";
  private static final String TAG_005 = "005";
  private static final String TAG_035 = "035";
  private static final String FIELDS = "fields";
  private static final String OCLC = "OCoLC";
  private static final String OCLC_PREFIX = "(OCoLC)";
  private static final String OCLC_PATTERN = "\\((" + OCLC + ")\\)((ocm|ocn|on)?0*|([a-zA-Z]+)0*)(\\d+\\w*)";
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

  public static String getControlFieldValue(Object record, String tag) {
    MarcReader reader = buildMarcReader(record);
    try {
      if (reader.hasNext()) {
        org.marc4j.marc.Record marcRecord = reader.next();
        return marcRecord.getControlFields().stream()
          .filter(controlField -> controlField.getTag().equals(tag))
          .findFirst()
          .map(ControlField::getData)
          .orElse(null);
      }
    } catch (Exception e) {
      return null;
    }
    return null;
  }

  public static boolean addControlledFieldToMarcRecord(Record recordForUpdate, String field, String value,
                                                       AddControlledFieldToMarcRecordFunction addFieldFunc) {
    boolean result = false;
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      if (recordForUpdate != null && recordForUpdate.getParsedRecord() != null
        && recordForUpdate.getParsedRecord().getContent() != null) {
        MarcWriter streamWriter = new MarcStreamWriter(new ByteArrayOutputStream());
        MarcJsonWriter jsonWriter = new MarcJsonWriter(os);

        org.marc4j.marc.Record marcRecord = computeMarcRecord(recordForUpdate);
        if (marcRecord != null) {
          addFieldFunc.apply(field, value, marcRecord);
          // use stream writer to recalculate leader
          streamWriter.write(marcRecord);
          jsonWriter.write(marcRecord);

          String parsedContentString = new JsonObject(os.toString()).encode();
          recordForUpdate.setParsedRecord(recordForUpdate.getParsedRecord().setContent(parsedContentString));
          result = true;
        }
      }
    } catch (Exception e) {
      LOGGER.warn("addControlledFieldToMarcRecord:: Failed to add additional controlled field {}", field);
    }
    return result;
  }

  public static void addControlledFieldToMarcRecord(String field, String value, org.marc4j.marc.Record marcRecord) {
    ControlField dataField = MarcFactory.newInstance().newControlField(field, value);
    marcRecord.addVariableField(dataField);
  }

  public static boolean removeField(Record record, String field) {
    return removeField(record, field, '\0', null);
  }

  public static boolean removeField(Record record, String fieldName, char subfield, String value) {
    boolean isFieldRemoveSucceed = false;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      if (record != null && record.getParsedRecord() != null && record.getParsedRecord().getContent() != null) {
        MarcWriter marcStreamWriter = new MarcStreamWriter(new ByteArrayOutputStream());
        MarcJsonWriter marcJsonWriter = new MarcJsonWriter(baos);
        org.marc4j.marc.Record marcRecord = computeMarcRecord(record);
        if (marcRecord != null) {
          LOGGER.debug("removeField:: Started removing controlled field {} with value {} from record {}", fieldName,
            value, record.getId());
          if (StringUtils.isEmpty(value)) {
            isFieldRemoveSucceed = removeFirstFoundFieldByName(marcRecord, fieldName);
          } else {
            isFieldRemoveSucceed = removeFieldByNameAndValue(marcRecord, fieldName, subfield, value);
          }

          LOGGER.debug("removeField:: Removing controlled field {} with value {} from record {} is {}", fieldName,
            value, record.getId(), isFieldRemoveSucceed);
          if (isFieldRemoveSucceed) {
            LOGGER.debug("removeField:: Writing record {} after removing controlled field {} with value {}",
              record.getId(), fieldName, value);
            // use stream writer to recalculate leader
            marcStreamWriter.write(marcRecord);

            LOGGER.debug("removeField:: Writing record {} after removing controlled field {} with value {} "
              + "by jsonWriter", record.getId(), fieldName, value);
            marcJsonWriter.write(marcRecord);

            String parsedContentString = new JsonObject(baos.toString()).encode();

            LOGGER.debug("removeField:: Prepared parsedContentString for record {}", record.getId());
            record.setParsedRecord(record.getParsedRecord().setContent(parsedContentString));
          }
        }
      } else {
        if (record != null) {
          LOGGER.warn("removeField:: Record or parsed record content is null for record {}", record.getId());
        } else {
          LOGGER.warn("removeField:: Record is null");
        }
      }
    } catch (Exception e) {
      if (record != null) {
        LOGGER.warn("removeField:: Failed to remove controlled field {} from record {}", fieldName, record.getId(), e);
      } else {
        LOGGER.warn("removeField:: Failed to remove controlled field {} from record", fieldName, e);
      }
    }
    return isFieldRemoveSucceed;
  }

  public static void normalize035(Record srcRecord) {
    org.marc4j.marc.Record marcRecord = computeMarcRecord(srcRecord);
    if (marcRecord == null) {
      return;
    }
    List<Subfield> subfields = get035SubfieldOclcValues(marcRecord, TAG_035);
    if (!subfields.isEmpty()) {
      formatOclc(subfields);
      deduplicateOclc(marcRecord, subfields, TAG_035);
      recalculateLeaderAndParsedRecord(srcRecord, marcRecord);
    }
  }

  public static List<Subfield> get035SubfieldOclcValues(org.marc4j.marc.Record srcRecord, String tag) {
    return Optional.ofNullable(srcRecord)
      .stream()
      .flatMap(marcRecord -> marcRecord.getVariableFields(tag).stream())
      .flatMap(field -> get035oclcSubfields(field).stream())
      .collect(Collectors.toList());
  }

  private static void formatOclc(List<Subfield> subfields) {
    Pattern pattern = Pattern.compile(OCLC_PATTERN);

    for (Subfield subfield : subfields) {
      String data = subfield.getData().replaceAll("[.\\s]", "");
      Matcher matcher = pattern.matcher(data);
      if (matcher.find()) {
        String oclcTag = matcher.group(1); // "OCoLC"
        String numericAndTrailing = matcher.group(5); // Numeric part and any characters that follow
        String prefix = matcher.group(2); // Entire prefix including letters and potentially leading zeros

        if (prefix != null && (prefix.startsWith("ocm") || prefix.startsWith("ocn") || prefix.startsWith("on"))) {
          // If "ocm" or "ocn", strip entirely from the prefix
          subfield.setData("(" + oclcTag + ")" + numericAndTrailing);
        } else {
          // For other cases, strip leading zeros only from the numeric part
          numericAndTrailing = numericAndTrailing.replaceFirst("^0+", "");
          if (prefix != null) {
            prefix = prefix.replaceAll("\\d+", ""); // Safely remove digits from the prefix if not null
          }
          // Add back any other prefix that might have been included like "tfe"
          subfield.setData("(" + oclcTag + ")" + (prefix != null ? prefix : "") + numericAndTrailing);
        }
      }
    }
  }

  private static void deduplicateOclc(org.marc4j.marc.Record marcRecord, List<Subfield> subfields, String tag) {
    List<Subfield> subfieldsToDelete = new ArrayList<>();

    for (Subfield subfield : new ArrayList<>(subfields)) {
      if (subfields.stream().anyMatch(s -> isOclcSubfieldDuplicated(subfield, s))) {
        subfieldsToDelete.add(subfield);
        subfields.remove(subfield);
      }
    }

    List<VariableField> variableFields = marcRecord.getVariableFields(tag);
    subfieldsToDelete.forEach(subfieldToDelete ->
      variableFields.forEach(field -> removeSubfieldIfExist(marcRecord, field, subfieldToDelete)));
  }

  private static boolean isOclcSubfieldDuplicated(Subfield s1, Subfield s2) {
    return s1 != s2 && s1.getData().equals(s2.getData()) && s1.getCode() == s2.getCode();
  }

  private static void removeSubfieldIfExist(org.marc4j.marc.Record marcRecord, VariableField field,
                                            Subfield subfieldToDelete) {
    if (field instanceof DataField dataField && dataField.getSubfields().contains(subfieldToDelete)) {
      if (dataField.getSubfields().size() > 1) {
        dataField.removeSubfield(subfieldToDelete);
      } else {
        marcRecord.removeVariableField(dataField);
      }
    }
  }

  private static void recalculateLeaderAndParsedRecord(Record recordForUpdate, org.marc4j.marc.Record marcRecord) {
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      MarcWriter streamWriter = new MarcStreamWriter(new ByteArrayOutputStream());
      MarcJsonWriter jsonWriter = new MarcJsonWriter(os);
      if (marcRecord != null) {
        // use stream writer to recalculate leader
        streamWriter.write(marcRecord);
        jsonWriter.write(marcRecord);

        String parsedContentString = new JsonObject(os.toString()).encode();
        recordForUpdate.setParsedRecord(recordForUpdate.getParsedRecord().setContent(parsedContentString));
      }
    } catch (Exception e) {
      LOGGER.warn("recalculateLeaderAndParsedRecord:: Failed to recalculate leader and parsed record for record: {}",
        recordForUpdate.getId(), e);
    }
  }

  private static List<Subfield> get035oclcSubfields(VariableField field) {
    if (field instanceof DataField dataField) {
      return dataField.getSubfields().stream()
        .filter(sf -> sf.getData().trim().startsWith(OCLC_PREFIX))
        .toList();
    }
    return Collections.emptyList();
  }

  /**
   * Read value from controlled field in marc record.
   *
   * @param srcRecord marc record
   * @param tag       tag to read
   * @return value from field
   */
  public static String getValueFromControlledField(Record srcRecord, String tag) {
    try {
      org.marc4j.marc.Record marcRecord = computeMarcRecord(srcRecord);
      if (marcRecord != null) {
        Optional<ControlField> controlField = marcRecord.getControlFields()
          .stream()
          .filter(field -> field.getTag().equals(tag))
          .findFirst();
        if (controlField.isPresent()) {
          return controlField.get().getData();
        }
      }
    } catch (Exception e) {
      LOGGER.warn("getValueFromControlledField:: Failed to read controlled field {} from record {}", tag,
        srcRecord.getId(), e);
      return null;
    }
    return null;
  }

  public static String mergeFieldsFor035(String valueFrom003, String valueFrom001) {
    if (isBlank(valueFrom003)) {
      return valueFrom001;
    }
    return "(" + valueFrom003 + ")" + valueFrom001;
  }

  /**
   * Check if data field with the same value exist.
   *
   * @param recordForUpdate record that needs to be updated
   * @param tag             tag of data field
   * @param value           value of the field to add
   * @return true if exist
   */

  public static boolean isFieldExist(Record recordForUpdate, String tag, char subfield, String value) {
    try {
      org.marc4j.marc.Record marcRecord = computeMarcRecord(recordForUpdate);
      if (marcRecord != null) {
        for (VariableField field : marcRecord.getVariableFields(tag)) {
          if (field instanceof DataField dataField) {
            for (Subfield sub : dataField.getSubfields(subfield)) {
              if (isNotEmpty(sub.getData()) && sub.getData().equals(value.trim())) {
                return true;
              }
            }
          } else if (field instanceof ControlField controlField
            && isNotEmpty(controlField.getData())
            && ((ControlField) field).getData().equals(value.trim())) {
            return true;
          }
        }
      }
    } catch (Exception e) {
      LOGGER.warn("isFieldExist:: Error during the search a field in the record", e);
      return false;
    }
    return false;
  }

  /**
   * Adds new data field to marc record.
   *
   * @param recordForUpdate record that needs to be updated
   * @param tag             tag of data field
   * @param value           value of the field to add
   * @return true if succeeded, false otherwise
   */
  public static boolean addDataFieldToMarcRecord(Record recordForUpdate, String tag, char ind1, char ind2,
                                                 char subfield, String value) {
    boolean result = false;
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      if (recordForUpdate != null && recordForUpdate.getParsedRecord() != null
        && recordForUpdate.getParsedRecord().getContent() != null) {
        MarcWriter streamWriter = new MarcStreamWriter(new ByteArrayOutputStream());
        MarcJsonWriter jsonWriter = new MarcJsonWriter(os);
        MarcFactory factory = MarcFactory.newInstance();
        org.marc4j.marc.Record marcRecord = computeMarcRecord(recordForUpdate);
        if (marcRecord != null) {
          DataField dataField = factory.newDataField(tag, ind1, ind2);
          dataField.addSubfield(factory.newSubfield(subfield, value));
          addDataFieldInNumericalOrder(dataField, marcRecord);
          // use stream writer to recalculate leader
          streamWriter.write(marcRecord);
          jsonWriter.write(marcRecord);

          String parsedContentString = new JsonObject(os.toString()).encode();
          recordForUpdate.setParsedRecord(recordForUpdate.getParsedRecord().setContent(parsedContentString));
          result = true;
        }
      }
    } catch (Exception e) {
      LOGGER.warn("addDataFieldToMarcRecord:: Failed to add additional data field {}", tag, e);
    }
    return result;
  }

  private static void addDataFieldInNumericalOrder(DataField field, org.marc4j.marc.Record marcRecord) {
    String tag = field.getTag();
    List<DataField> dataFields = marcRecord.getDataFields();
    for (int i = 0; i < dataFields.size(); i++) {
      if (dataFields.get(i).getTag().compareTo(tag) > 0) {
        marcRecord.getDataFields().add(i, field);
        return;
      }
    }
    marcRecord.addVariableField(field);
  }

  private static boolean removeFirstFoundFieldByName(org.marc4j.marc.Record marcRecord, String fieldName) {
    boolean isFieldFound = false;
    VariableField variableField = marcRecord.getVariableField(fieldName);
    if (variableField != null) {
      marcRecord.removeVariableField(variableField);
      isFieldFound = true;
    }
    return isFieldFound;
  }

  private static boolean removeFieldByNameAndValue(org.marc4j.marc.Record marcRecord, String fieldName, char subfield,
                                                   String value) {
    boolean isFieldFound = false;
    List<VariableField> variableFields = marcRecord.getVariableFields(fieldName);
    for (VariableField variableField : variableFields) {
      if (isFieldContainsValue(variableField, subfield, value)) {
        marcRecord.removeVariableField(variableField);
        isFieldFound = true;
        break;
      }
    }
    return isFieldFound;
  }

  /**
   * Checks if the field contains a certain value in the selected subfield.
   *
   * @param field    from MARC BIB record
   * @param subfield subfield of the field
   * @param value    value of the field
   * @return true if contains, false otherwise
   */
  private static boolean isFieldContainsValue(VariableField field, char subfield, String value) {
    boolean isContains = false;
    if (field instanceof DataField) {
      for (Subfield sub : ((DataField) field).getSubfields(subfield)) {
        if (isNotEmpty(sub.getData()) && sub.getData().contains(value.trim())) {
          isContains = true;
          break;
        }
      }
    }
    return isContains;
  }

  private static MarcReader buildMarcReader(Object record) {
    return new MarcJsonReader(new ByteArrayInputStream(record.toString().getBytes(StandardCharsets.UTF_8)));
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

  @FunctionalInterface
  public interface AddControlledFieldToMarcRecordFunction {
    void apply(String field, String value, org.marc4j.marc.Record marcRecord);
  }
}
