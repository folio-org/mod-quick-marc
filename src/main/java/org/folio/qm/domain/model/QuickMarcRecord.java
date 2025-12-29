package org.folio.qm.domain.model;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.exception.ConverterException;
import org.folio.qm.util.QmMarcJsonWriter;
import org.marc4j.marc.Record;

/**
 * Domain object that holds all related context and records for QuickMARC operations.
 * This class serves as a central holder for MARC record data along with its associated
 * Folio record, metadata, and versioning information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickMarcRecord {

  private BaseMarcRecord source;

  /**
   * Marc4j Record representation of the MARC record.
   * Converted once from QuickMarcEdit/Create DTO.
   */
  private Record marcRecord;

  /**
   * Parsed content (JSON) derived from marc4j Record.
   * This is the central artifact used for:
   * - Creating/updating SRS records
   * - Mapping to Folio records (Instance/Authority/Holdings)
   * Computed once and reused everywhere.
   */
  private JsonObject parsedContent;

  /**
   * Associated Folio record (Instance, Holdings, or Authority).
   */
  private FolioRecord folioRecord;

  /**
   * Format type of the MARC record (BIBLIOGRAPHIC, HOLDINGS, AUTHORITY).
   */
  private MarcFormat marcFormat;

  /**
   * Mapping record type.
   */
  private MappingRecordType mappingRecordType;

  /**
   * Version number for optimistic locking.
   */
  private Integer sourceVersion;

  /**
   * Indicates whether the record should be suppressed from discovery.
   */
  private boolean suppressDiscovery;

  /**
   * External ID linking to the corresponding Folio record (Instance/Holdings/Authority ID).
   */
  private UUID externalId;

  /**
   * Human-readable identifier from the external Folio record.
   */
  private String externalHrid;

  /**
   * Parsed record ID from Source Record Storage.
   */
  private UUID parsedRecordId;

  /**
   * DTO ID of the parsed record from Source Record Storage.
   */
  private UUID parsedRecordDtoId;

  /**
   * Source record type.
   */
  private org.folio.Record.RecordType sourceRecordType;

  /**
   * Gets the ID from the associated Folio record.
   *
   * @return the Folio record ID, or null if folioRecord is null
   */
  public String getFolioRecordId() {
    return folioRecord != null ? folioRecord.getId() : null;
  }

  /**
   * Gets the HRID from the associated Folio record.
   *
   * @return the Folio record HRID, or null if folioRecord is null
   */
  public String getFolioRecordHrid() {
    return folioRecord != null ? folioRecord.getHrid() : null;
  }

  public JsonObject getParsedContent() {
    if (parsedContent == null) {
      buildParsedContent();
    }
    return parsedContent;
  }

  public void buildParsedContent() {
    try (ByteArrayOutputStream os = new ByteArrayOutputStream();
         QmMarcJsonWriter writer = new QmMarcJsonWriter(os)) {
      writer.write(marcRecord);

      var convertedContent = new JsonObject(Buffer.buffer(os.toByteArray()));
      reorderContentTagsBasedOnSource(convertedContent, source.getFields());
      this.parsedContent = convertedContent;
    } catch (Exception e) {
      throw new ConverterException(e);
    }
  }

  private void reorderContentTagsBasedOnSource(JsonObject convertedContent, List<FieldItem> sourceFields) {
    var fieldsArrayNode = convertedContent.getJsonArray("fields");

    Map<String, Queue<JsonObject>> jsonNodesByTag = new HashMap<>();
    for (int i = 0; i < fieldsArrayNode.size(); i++) {
      var node = fieldsArrayNode.getJsonObject(i);
      var tag = node.fieldNames().iterator().next();
      jsonNodesByTag.computeIfAbsent(tag, k -> new LinkedList<>()).add(node);
    }

    var rearrangedArray = new JsonArray();
    for (FieldItem fieldItem : sourceFields) {
      Queue<JsonObject> nodes = jsonNodesByTag.get(fieldItem.getTag());
      if (nodes != null && !nodes.isEmpty()) {
        rearrangedArray.add(nodes.poll());
      }
    }

    fieldsArrayNode.clear();
    fieldsArrayNode.addAll(rearrangedArray);
  }
}
