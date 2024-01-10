package org.folio.qm.controller;

import static org.folio.qm.support.utils.ApiTestUtils.marcSpecificationsByrecordTypeAndFieldTag;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.log4j.Log4j2;
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

    getResultActions(marcSpecificationsByrecordTypeAndFieldTag(RecordType.MARC_BIBLIOGRAPHIC.getValue(), "009"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));

    getResultActions(marcSpecificationsByrecordTypeAndFieldTag(RecordType.MARC_HOLDINGS.getValue(), "001"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));

    getResultActions(marcSpecificationsByrecordTypeAndFieldTag(RecordType.MARC_AUTHORITY.getValue(), "003"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));
  }

  @Test
  void testGetMarcSpecificationsSuccess() throws Exception {
    log.info("===== Verify GET MARC Specifications: Successful =====");

    getResultActions(marcSpecificationsByrecordTypeAndFieldTag(RecordType.MARC_BIBLIOGRAPHIC.getValue(), "008"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.format").value(MarcFormat.BIBLIOGRAPHIC.getValue()));

    getResultActions(marcSpecificationsByrecordTypeAndFieldTag(RecordType.MARC_AUTHORITY.getValue(), "008"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.format").value(MarcFormat.AUTHORITY.getValue()));

    getResultActions(marcSpecificationsByrecordTypeAndFieldTag(RecordType.MARC_HOLDINGS.getValue(), "008"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.format").value(MarcFormat.HOLDINGS.getValue()));
  }

  @Test
  void testGetMarcSpecificationsBadRequest() throws Exception {
    log.info("===== Verify GET MARC Specifications: Bad request =====");

    getResultActions(marcSpecificationsByrecordTypeAndFieldTag("WRONG_RECORD_TYPE", "008"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));

    getResultActions(marcSpecificationsByrecordTypeAndFieldTag(RecordType.MARC_BIBLIOGRAPHIC.getValue(), "08"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));
  }
}
