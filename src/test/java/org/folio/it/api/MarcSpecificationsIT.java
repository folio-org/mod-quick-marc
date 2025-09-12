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
