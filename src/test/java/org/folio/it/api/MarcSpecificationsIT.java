package org.folio.it.api;

import static org.folio.support.utils.ApiTestUtils.marcSpecificationsByRecordTypeAndFieldTag;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.log4j.Log4j2;
import org.folio.it.BaseIT;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.entity.RecordType;
import org.folio.qm.util.ErrorUtils;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Test;

@Log4j2
@IntegrationTest
class MarcSpecificationsIT extends BaseIT {

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
  void testGetMarcSpecificationsSuccess_newAllowedValuesIn21PositionExist() throws Exception {
    log.info("===== Verify GET MARC Specifications allowedValues in 21 position: Successful =====");

    doGet(marcSpecificationsByRecordTypeAndFieldTag(RecordType.MARC_BIBLIOGRAPHIC.getValue(), "008"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.spec.types[3].items[8].allowedValues[2].code").value("a"))
      .andExpect(jsonPath("$.spec.types[3].items[8].allowedValues[2].name").value("Activity report"))
      .andExpect(jsonPath("$.spec.types[3].items[8].allowedValues[6].code").value("i"))
      .andExpect(jsonPath("$.spec.types[3].items[8].allowedValues[6].name").value("Serial zine"))
      .andExpect(jsonPath("$.spec.types[3].items[8].allowedValues[12].code").value("q"))
      .andExpect(jsonPath("$.spec.types[3].items[8].allowedValues[12].name").value("Serial podcast"));
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
}
