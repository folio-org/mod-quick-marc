package org.folio.qm.util;

import io.vertx.core.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.log4j.Log4j2;
import org.marc4j.MarcJsonReader;
import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcStreamWriter;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

/**
 * Utility class for adding additional fields to MARC records during CREATE operations.
 * Based on AdditionalFieldsUtil from mod-source-record-manager.
 */
@Log4j2
public final class MarcRecordModifier {

  public static final String TAG_999 = "999";
  public static final String TAG_001 = "001";
  public static final String TAG_003 = "003";
  public static final String TAG_004 = "004";
  public static final String TAG_035 = "035";
  public static final char INDICATOR_F = 'f';
  public static final char SUBFIELD_I = 'i';
  private static final String DOT_OR_WHITESPACE_REGEX = "[.\\s]";
  private static final String DIGITS_REGEX = "\\d+";
  private static final String PREFIX_ZEROS_REGEX = "^0+";
  private static final String OCLC_PREFIX = "(OCoLC)";
  private static final String OCLC = "OCoLC";
  private static final String OCLC_PATTERN = "\\((" + OCLC + ")\\)((ocm|ocn|on)?0*|([a-zA-Z]+)0*)(\\d+\\w*)";
  private static final Pattern OCLC_COMPILED = Pattern.compile(OCLC_PATTERN);

  private static final MarcFactory FACTORY = MarcFactory.newInstance();

  private MarcRecordModifier() {
  }

  /**
   * Adds 999 field with external ID in subfield $i to the MARC record.
   * The 999 field has indicators 'f' 'f'.
   *
   * @param marcRecord the marc4j Record to modify
   * @param externalId the external ID (Instance/Holdings/Authority ID)
   * @return true if successful, false otherwise
   */
  public static boolean add999Field(Record marcRecord, String externalId) {
    try {
      // Remove existing 999 field with indicators 'f' 'f' if exists
      VariableField existingField = getSingleFieldByIndicators(marcRecord, TAG_999);
      if (existingField != null) {
        marcRecord.removeVariableField(existingField);
      }

      // Create new 999 field with $i subfield
      DataField field999 = FACTORY.newDataField(TAG_999, INDICATOR_F, INDICATOR_F);
      field999.addSubfield(FACTORY.newSubfield(SUBFIELD_I, externalId));

      marcRecord.addVariableField(field999);
      log.debug("add999Field:: Added 999 field with externalId: {}", externalId);
      return true;
    } catch (Exception e) {
      log.error("add999Field:: Failed to add 999 field", e);
      return false;
    }
  }

  /**
   * Adds or updates 001 field with HRID value.
   *
   * @param marcRecord the marc4j Record to modify
   * @param hrid       the HRID value to set
   * @return true if successful, false otherwise
   */
  public static boolean add001Field(Record marcRecord, String hrid) {
    try {
      // Remove existing 001 field if exists
      VariableField existing001 = marcRecord.getVariableField(TAG_001);
      if (existing001 != null) {
        marcRecord.removeVariableField(existing001);
      }

      // Add new 001 control field with HRID
      marcRecord.addVariableField(FACTORY.newControlField(TAG_001, hrid));
      log.debug("add001Field:: Added 001 field with HRID: {}", hrid);
      return true;
    } catch (Exception e) {
      log.error("add001Field:: Failed to add 001 field", e);
      return false;
    }
  }

  /**
   * Removes all 003 fields from the MARC record.
   *
   * @param marcRecord the marc4j Record to modify
   */
  public static void remove003Field(Record marcRecord) {
    marcRecord.getVariableFields(TAG_003).forEach(marcRecord::removeVariableField);
  }

  /**
   * Normalizes 035 fields containing OCLC numbers in the MARC record.
   * Formats OCLC numbers and removes duplicates.
   *
   * @param marcRecord the marc4j Record to modify
   */
  public static void normalize035Field(Record marcRecord) {
    if (marcRecord == null) {
      return;
    }
    var subfields = get035SubfieldOclcValues(marcRecord);
    if (!subfields.isEmpty()) {
      formatOclc(subfields);
      deduplicateOclc(marcRecord, subfields);
    }
  }

  /**
   * Retrieves the data from control field 004.
   *
   * @param marcRecord the marc4j Record
   * @return the data of control field 004, or null if not present
   */
  public static String get004ControlFieldData(Record marcRecord) {
    return marcRecord.getControlFields()
      .stream()
      .filter(controlField -> controlField.getTag().equals(TAG_004) && controlField.getData() != null)
      .findFirst()
      .map(controlField -> controlField.getData().trim())
      .orElse(null);
  }

  /**
   * Converts MARC record to JSON content after modifications.
   * Recalculates leader using stream writer.
   *
   * @param marcRecord the modified marc4j Record
   * @return JSON string representation
   */
  public static String toJsonContent(Record marcRecord) {
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      // Use stream writer to recalculate leader
      ByteArrayOutputStream tempOs = new ByteArrayOutputStream();
      MarcStreamWriter streamWriter = new MarcStreamWriter(tempOs);
      streamWriter.write(marcRecord);
      streamWriter.close();

      // Write to JSON
      MarcJsonWriter jsonWriter = new MarcJsonWriter(os);
      jsonWriter.write(marcRecord);
      jsonWriter.close();

      return new JsonObject(os.toString()).encode();
    } catch (Exception e) {
      log.error("toJsonContent:: Failed to convert MARC record to JSON", e);
      throw new IllegalArgumentException("Failed to convert MARC record to JSON", e);
    }
  }

  /**
   * Parses JSON content to marc4j Record.
   *
   * @param jsonContent JSON string representation of MARC record
   * @return marc4j Record
   */
  public static Record fromJsonContent(String jsonContent) {
    try (ByteArrayInputStream bis = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8))) {
      MarcJsonReader reader = new MarcJsonReader(bis);
      if (reader.hasNext()) {
        return reader.next();
      }
      throw new IllegalArgumentException("No MARC record found in JSON content");
    } catch (Exception e) {
      log.error("fromJsonContent:: Failed to parse JSON content", e);
      throw new IllegalArgumentException("Failed to parse JSON content", e);
    }
  }

  /**
   * Gets a single field by tag and indicators 'f' 'f'.
   *
   * @param marcRecord the marc4j Record
   * @param tag        the field tag
   * @return the field if found, null otherwise
   */
  private static VariableField getSingleFieldByIndicators(Record marcRecord, String tag) {
    for (VariableField field : marcRecord.getVariableFields(tag)) {
      if (field instanceof DataField dataField
        && dataField.getIndicator1() == INDICATOR_F
        && dataField.getIndicator2() == INDICATOR_F) {
        return field;
      }
    }
    return null;
  }

  private static List<Subfield> get035SubfieldOclcValues(Record marcRecord) {
    List<Subfield> subfields = new ArrayList<>();
    for (VariableField field : marcRecord.getVariableFields(TAG_035)) {
      if (field instanceof DataField dataField) {
        for (Subfield sf : dataField.getSubfields()) {
          if (sf.getData() != null && sf.getData().trim().startsWith(OCLC_PREFIX)) {
            subfields.add(sf);
          }
        }
      }
    }
    return subfields;
  }

  private static void formatOclc(List<Subfield> subfields) {
    for (Subfield subfield : subfields) {
      var data = subfield.getData().replaceAll(DOT_OR_WHITESPACE_REGEX, "");
      var matcher = OCLC_COMPILED.matcher(data);
      if (matcher.find()) {
        var oclcTag = matcher.group(1); // "OCoLC"
        var numericAndTrailing = matcher.group(5); // Numeric part and any characters that follow
        var prefix = matcher.group(2); // Entire prefix including letters and potentially leading zeros

        if (prefix != null && (prefix.startsWith("ocm") || prefix.startsWith("ocn") || prefix.startsWith("on"))) {
          // If "ocm" or "ocn", strip entirely from the prefix
          subfield.setData("(" + oclcTag + ")" + numericAndTrailing);
        } else {
          // For other cases, strip leading zeros only from the numeric part
          numericAndTrailing = numericAndTrailing.replaceFirst(PREFIX_ZEROS_REGEX, "");
          if (prefix != null) {
            prefix = prefix.replaceAll(DIGITS_REGEX, ""); // Safely remove digits from the prefix if not null
          }
          // Add back any other prefix that might have been included like "tfe"
          subfield.setData("(" + oclcTag + ")" + (prefix != null ? prefix : "") + numericAndTrailing);
        }
      }
    }
  }

  private static void deduplicateOclc(Record marcRecord, List<Subfield> subfields) {
    List<Subfield> subfieldsToDelete = new ArrayList<>();

    for (Subfield subfield : new ArrayList<>(subfields)) {
      if (subfields.stream().anyMatch(s -> isOclcSubfieldDuplicated(subfield, s))) {
        subfieldsToDelete.add(subfield);
        subfields.remove(subfield);
      }
    }
    var variableFields = marcRecord.getVariableFields(TAG_035);
    subfieldsToDelete.forEach(subfieldToDelete ->
      variableFields.forEach(field -> removeSubfieldIfExist(marcRecord, field, subfieldToDelete)));
  }

  private static boolean isOclcSubfieldDuplicated(Subfield s1, Subfield s2) {
    return !s1.equals(s2)
      && s1.getData().equals(s2.getData())
      && s1.getCode() == s2.getCode();
  }

  private static void removeSubfieldIfExist(Record marcRecord, VariableField field,
                                            Subfield subfieldToDelete) {
    if (field instanceof DataField dataField && dataField.getSubfields().contains(subfieldToDelete)) {
      if (dataField.getSubfields().size() > 1) {
        dataField.removeSubfield(subfieldToDelete);
      } else {
        marcRecord.removeVariableField(dataField);
      }
    }
  }
}
