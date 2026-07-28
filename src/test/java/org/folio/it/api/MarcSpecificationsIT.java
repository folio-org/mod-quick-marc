package org.folio.it.api;

import static org.folio.support.utils.ApiTestUtils.marcSpecificationsByRecordTypeAndFieldTag;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.folio.it.BaseIT;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.MarcSpec;
import org.folio.qm.domain.dto.MarcSpecSpec;
import org.folio.qm.domain.dto.MarcSpecificationCondition;
import org.folio.qm.domain.dto.MarcSpecificationItem;
import org.folio.qm.domain.dto.MarcSpecificationItemValue;
import org.folio.qm.domain.dto.MarcSpecificationType;
import org.folio.qm.domain.dto.MarcSpecificationTypeIdentifiedBy;
import org.folio.qm.domain.entity.MarcSpecification;
import org.folio.qm.domain.entity.RecordType;
import org.folio.qm.domain.repository.MarcSpecificationRepository;
import org.folio.qm.util.ErrorUtils;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Log4j2
@IntegrationTest
class MarcSpecificationsIT extends BaseIT {

  private static final String CUSTOM_FIELD_TAG = "999";

  @Autowired
  private MarcSpecificationRepository marcSpecificationRepository;

  @Test
  void testGetMarcSpecificationsNotFound() throws Exception {
    log.info("===== Verify GET MARC Specifications: Record Not Found =====");

    doGet(marcSpecificationsByRecordTypeAndFieldTag(RecordType.MARC_BIBLIOGRAPHIC.getValue(), "009"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));

    doGet(marcSpecificationsByRecordTypeAndFieldTag(RecordType.MARC_HOLDINGS.getValue(), "001"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));

    doGet(marcSpecificationsByRecordTypeAndFieldTag(RecordType.MARC_AUTHORITY.getValue(), "003"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));
  }

  @Test
  void testGetMarcSpecificationsSuccess() throws Exception {
    log.info("===== Verify GET MARC Specifications: Successful =====");

    doGet(marcSpecificationsByRecordTypeAndFieldTag(RecordType.MARC_BIBLIOGRAPHIC.getValue(), "008"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.format").value(MarcFormat.BIBLIOGRAPHIC.getValue()));

    doGet(marcSpecificationsByRecordTypeAndFieldTag(RecordType.MARC_AUTHORITY.getValue(), "008"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.format").value(MarcFormat.AUTHORITY.getValue()));

    doGet(marcSpecificationsByRecordTypeAndFieldTag(RecordType.MARC_HOLDINGS.getValue(), "008"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.format").value(MarcFormat.HOLDINGS.getValue()));

    doGet(marcSpecificationsByRecordTypeAndFieldTag(RecordType.MARC_BIBLIOGRAPHIC.getValue(), "008"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.spec.types[0].items[1].allowedValues[0].name").value("No attempt to code"));
  }

  @Test
  void testGetMarcSpecificationsSuccess_allowedValuesExists() throws Exception {
    log.info("===== Verify GET MARC Specifications allowedValues: Successful =====");

    doGet(marcSpecificationsByRecordTypeAndFieldTag(RecordType.MARC_BIBLIOGRAPHIC.getValue(), "008"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.spec.types[0].items[1].allowedValues[0].name").value("No attempt to code"));
  }

  @Test
  void testGetMarcSpecificationsSuccess_allowedValuesIn25PositionExists() throws Exception {
    log.info("===== Verify GET MARC Specifications allowedValues in 25 position: Successful =====");

    doGet(marcSpecificationsByRecordTypeAndFieldTag(RecordType.MARC_BIBLIOGRAPHIC.getValue(), "008"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.spec.types[6].items[8].allowedValues[8].name").value("Remote sensing image"))
      .andExpect(jsonPath("$.spec.types[6].items[8].allowedValues[8].code").value("r"));
  }

  @Test
  void testGetMarcSpecificationsBadRequest() throws Exception {
    log.info("===== Verify GET MARC Specifications: Bad request =====");

    doGet(marcSpecificationsByRecordTypeAndFieldTag("WRONG_RECORD_TYPE", "008"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));

    doGet(marcSpecificationsByRecordTypeAndFieldTag(RecordType.MARC_BIBLIOGRAPHIC.getValue(), "08"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));
  }

  @Test
  void testGetMarcSpecificationsSuccess_responseJsonFormat() throws Exception {
    log.info("===== Verify GET MARC Specifications: Response JSON format (included/not-included fields) =====");

    runInTenantContext(() -> marcSpecificationRepository.save(customBibliographicSpecification()));

    doGet(marcSpecificationsByRecordTypeAndFieldTag(RecordType.MARC_BIBLIOGRAPHIC.getValue(), CUSTOM_FIELD_TAG))
      .andExpect(status().isOk())
      // required, non-null fields are present
      .andExpect(jsonPath("$.tag").value(CUSTOM_FIELD_TAG))
      .andExpect(jsonPath("$.format").value(MarcFormat.BIBLIOGRAPHIC.getValue()))
      .andExpect(jsonPath("$.label").value("Custom field"))
      .andExpect(jsonPath("$.required").value(true))
      // `false` booleans are not treated as "empty" and must still be serialized
      .andExpect(jsonPath("$.repeatable").value(false))
      // `url` is null -> excluded from the response (NON_EMPTY global inclusion)
      .andExpect(jsonPath("$.url").doesNotExist())
      // item without allowedValues set -> field is absent (JsonNullable NON_ABSENT)
      .andExpect(jsonPath("$.spec.types[0].items[0].code").value("noAllowedValues"))
      .andExpect(jsonPath("$.spec.types[0].items[0].allowedValues").doesNotExist())
      // item with allowedValues explicitly set -> field is present
      .andExpect(jsonPath("$.spec.types[0].items[1].code").value("withAllowedValues"))
      .andExpect(jsonPath("$.spec.types[0].items[1].allowedValues").isArray())
      .andExpect(jsonPath("$.spec.types[0].items[1].allowedValues[0].code").value("a"))
      // allowed value with null `name` -> excluded (NON_NULL on MarcSpecificationItemValue)
      .andExpect(jsonPath("$.spec.types[0].items[1].allowedValues[0].name").doesNotExist());

    // cleanup of custom spec
    runInTenantContext(() -> marcSpecificationRepository
      .findByRecordTypeAndFieldTag(RecordType.MARC_BIBLIOGRAPHIC, CUSTOM_FIELD_TAG)
      .ifPresent(marcSpecificationRepository::delete));
  }

  private MarcSpecification customBibliographicSpecification() {
    var type = new MarcSpecificationType()
      .code("book")
      .identifiedBy(new MarcSpecificationTypeIdentifiedBy()
        .or(List.of(new MarcSpecificationCondition().tag("LDR").positions(Map.of()))))
      .items(List.of(itemWithoutAllowedValues(), itemWithAllowedValues()));

    var marcSpec = new MarcSpec()
      .tag(CUSTOM_FIELD_TAG)
      .format(MarcFormat.BIBLIOGRAPHIC)
      .label("Custom field")
      .url(null)
      .repeatable(false)
      .required(true)
      .spec(new MarcSpecSpec().types(List.of(type)));

    var entity = new MarcSpecification();
    entity.setRecordType(RecordType.MARC_BIBLIOGRAPHIC);
    entity.setFieldTag(CUSTOM_FIELD_TAG);
    entity.setMarcSpec(marcSpec);
    entity.setCreatedAt(Timestamp.from(Instant.parse("2026-05-07T10:00:00Z")));
    return entity;
  }

  private MarcSpecificationItem itemWithoutAllowedValues() {
    return new MarcSpecificationItem()
      .code("noAllowedValues")
      .name("Item without allowed values")
      .order(0)
      .position(0)
      .length(1)
      .isArray(false)
      .readOnly(true);
  }

  private MarcSpecificationItem itemWithAllowedValues() {
    return new MarcSpecificationItem()
      .code("withAllowedValues")
      .name("Item with allowed values")
      .order(1)
      .position(1)
      .length(1)
      .isArray(false)
      .readOnly(true)
      .allowedValues(List.of(new MarcSpecificationItemValue().code("a").name(null)));
  }
}
