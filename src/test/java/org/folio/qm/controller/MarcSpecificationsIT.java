package org.folio.qm.controller;

import static org.folio.qm.support.utils.ApiTestUtils.marcSpecificationsByrecordTypeAndFieldTag;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.log4j.Log4j2;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.support.types.IntegrationTest;
import org.folio.qm.util.ErrorUtils;
import org.junit.jupiter.api.Test;

@Log4j2
@IntegrationTest
class MarcSpecificationsIT extends BaseIT {

  @Test
  void testGetMarcSpecificationsNotFound() throws Exception {
    log.info("===== Verify GET MARC Specifications: Record Not Found =====");

    getResultActions(marcSpecificationsByrecordTypeAndFieldTag(MarcFormat.BIBLIOGRAPHIC.getValue(), "009"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));

    getResultActions(marcSpecificationsByrecordTypeAndFieldTag(MarcFormat.HOLDINGS.getValue(), "008"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));
  }

  @Test
  void testGetMarcSpecificationsSuccess() throws Exception {
    log.info("===== Verify GET MARC Specifications: Successful =====");

    getResultActions(marcSpecificationsByrecordTypeAndFieldTag(MarcFormat.BIBLIOGRAPHIC.getValue(), "008"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.format").value(MarcFormat.BIBLIOGRAPHIC.getValue()));
  }

  @Test
  void testGetMarcSpecificationsBadRequest() throws Exception {
    log.info("===== Verify GET MARC Specifications: Bad request =====");

    getResultActions(marcSpecificationsByrecordTypeAndFieldTag("WRONG_RECORD_TYPE", "008"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));

    getResultActions(marcSpecificationsByrecordTypeAndFieldTag(MarcFormat.BIBLIOGRAPHIC.getValue(), "08"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.type").value(ErrorUtils.ErrorType.INTERNAL.getTypeCode()));
  }
}
