package org.folio.qm.validation.impl.holdings;

import static org.folio.qm.support.utils.JsonTestUtils.getMockAsObject;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_EDIT_AUTHORITY_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_EDIT_BIB_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_EDIT_HOLDINGS_PATH;
import static org.folio.qm.validation.FieldValidationRule.IS_UNIQUE_TAG_ERROR_MSG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.support.types.UnitTest;
import org.folio.qm.validation.ValidationError;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;

@UnitTest
class OnlyOne008ControlFieldValidationRuleTest {
  private static final Logger logger = LogManager.getLogger(OnlyOne008ControlFieldValidationRuleTest.class);

  @Test
  void testGeneralInformationControlFieldValidationRuleForHoldings() {
    testGeneralInformationControlFieldValidationRule("Testing Holdings General Information Validation rule",
      QM_RECORD_EDIT_HOLDINGS_PATH);
  }

  private void testGeneralInformationControlFieldValidationRule(String s, String qmRecordHoldings) {
    logger.info(s);
    var rule = new OnlyOne008ControlFieldValidationRule();
    QuickMarcEdit quickMarc = getMockAsObject(qmRecordHoldings, QuickMarcEdit.class);
    assertDoesNotThrow(() -> rule.validate(quickMarc.getFields()));
  }

  @Test
  void testGeneralInformationControlFieldValidationRuleForBib() {
    testGeneralInformationControlFieldValidationRule("Testing Bib General Information Validation rule",
      QM_RECORD_EDIT_BIB_PATH);
  }

  @Test
  void testGeneralInformationControlFieldValidationRuleForAuthority() {
    testGeneralInformationControlFieldValidationRule("Testing Authority General Information Validation rule",
      QM_RECORD_EDIT_AUTHORITY_PATH);
  }

  @Test
  void testGeneralInformationControlFieldValidationRuleInvalidAmountOfFieldForHoldings() {
    testGeneralInformationControlFieldValidationRuleInvalidAmountOfField(
      "Testing Holdings General Information Validation rule - two 008 fields", QM_RECORD_EDIT_HOLDINGS_PATH);
  }

  private void testGeneralInformationControlFieldValidationRuleInvalidAmountOfField(String log, String qmRecordPath) {
    logger.info(log);
    var rule = new OnlyOne008ControlFieldValidationRule();
    QuickMarcEdit quickMarc = getMockAsObject(qmRecordPath, QuickMarcEdit.class);
    List<FieldItem> fields = quickMarc.getFields();
    fields.add(new FieldItem().tag("008").content("dsdsds"));
    Optional<ValidationError> validationError = rule.validate(fields);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("008"));
    assertThat(validationError.get().getMessage(), Is.is(IS_UNIQUE_TAG_ERROR_MSG));
  }

  @Test
  void testGeneralInformationControlFieldValidationRuleInvalidAmountOfFieldForAuthority() {
    testGeneralInformationControlFieldValidationRuleInvalidAmountOfField(
      "Testing Authority General Information Validation rule - two 008 fields", QM_RECORD_EDIT_AUTHORITY_PATH);
  }

  @Test
  void testGeneralInformationControlFieldValidationRuleInvalidAmountOfFieldForBib() {
    testGeneralInformationControlFieldValidationRuleInvalidAmountOfField(
      "Testing Bib General Information Validation rule - two 008 fields", QM_RECORD_EDIT_BIB_PATH);
  }
}
