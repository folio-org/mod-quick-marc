package org.folio.qm.validation.impl.holdings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.folio.qm.utils.JsonTestUtils.getMockAsObject;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_RECORD_AUTHORITY_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_RECORD_BIB_PATH;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_RECORD_HOLDINGS;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.validation.ValidationError;

class OnlyOne008ControlFieldValidationRuleTest {
  private static final Logger logger = LogManager.getLogger(OnlyOne008ControlFieldValidationRuleTest.class);

  @Test
  void testGeneralInformationControlFieldValidationRuleForHoldings() {
    testGeneralInformationControlFieldValidationRule("Testing Holdings General Information Validation rule", QM_RECORD_HOLDINGS);
  }

  @Test
  void testGeneralInformationControlFieldValidationRuleForBib() {
    testGeneralInformationControlFieldValidationRule("Testing Bib General Information Validation rule", QM_RECORD_BIB_PATH);
  }

  @Test
  void testGeneralInformationControlFieldValidationRuleForAuthority() {
    testGeneralInformationControlFieldValidationRule("Testing Authority General Information Validation rule", QM_RECORD_AUTHORITY_PATH);
  }

  @Test
  void testGeneralInformationControlFieldValidationRuleInvalidAmountOfFieldForHoldings() {
    testGeneralInformationControlFieldValidationRuleInvalidAmountOfField("Testing Holdings General Information Validation rule - two 008 fields", QM_RECORD_HOLDINGS);
  }

  @Test
  void testGeneralInformationControlFieldValidationRuleInvalidAmountOfFieldForAuthority() {
    testGeneralInformationControlFieldValidationRuleInvalidAmountOfField("Testing Authority General Information Validation rule - two 008 fields", QM_RECORD_AUTHORITY_PATH);
  }

  @Test
  void testGeneralInformationControlFieldValidationRuleInvalidAmountOfFieldForBib() {
    testGeneralInformationControlFieldValidationRuleInvalidAmountOfField("Testing Bib General Information Validation rule - two 008 fields", QM_RECORD_BIB_PATH);
  }

  private void testGeneralInformationControlFieldValidationRule(String s, String qmRecordHoldings) {
    logger.info(s);
    var rule = new OnlyOne008ControlFieldValidationRule();
    QuickMarc quickMarc = getMockAsObject(qmRecordHoldings, QuickMarc.class);
    assertDoesNotThrow(() -> rule.validate(quickMarc.getFields()));
  }

  private void testGeneralInformationControlFieldValidationRuleInvalidAmountOfField(String log, String qmRecordPath) {
    logger.info(log);
    var rule = new OnlyOne008ControlFieldValidationRule();
    QuickMarc quickMarc = getMockAsObject(qmRecordPath, QuickMarc.class);
    List<FieldItem> fields = quickMarc.getFields();
    fields.add(new FieldItem().tag("008").content("dsdsds"));
    Optional<ValidationError> validationError = rule.validate(fields);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("008"));
    assertThat(validationError.get().getMessage(), Is.is("Is unique tag"));
  }
}
