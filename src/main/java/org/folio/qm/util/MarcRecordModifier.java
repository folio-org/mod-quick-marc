package org.folio.qm.util;

import io.vertx.core.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.log4j.Log4j2;
import org.marc4j.MarcJsonReader;
import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcStreamWriter;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;

/**
 * Utility class for adding additional fields to MARC records during CREATE operations.
 * Based on AdditionalFieldsUtil from mod-source-record-manager.
 */
@Log4j2
public final class MarcRecordModifier {

  public static final String TAG_999 = "999";
  public static final String TAG_001 = "001";
  public static final char INDICATOR_F = 'f';
  public static final char SUBFIELD_I = 'i';
  public static final char SUBFIELD_S = 's';

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
    try {
      MarcJsonReader reader = new MarcJsonReader(
        new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8)));
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
}
