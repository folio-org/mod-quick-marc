package org.folio.qm.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.folio.qm.utils.JsonTestUtils.getMockAsObject;
import static org.folio.qm.utils.testentities.TestEntitiesUtils.QM_RECORD_HOLDINGS;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.validation.impl.holdings.OnlyOne008ControlFieldValidationRule;

class OnlyOne008ControlFieldValidationRuleTest {
  private static final Logger logger = LogManager.getLogger(OnlyOne008ControlFieldValidationRuleTest.class);


  @Test()
  void testGeneralInformationControlFieldValidationRule() {
    logger.info("Testing Holdings General Information Validation rule");
    var rule = new OnlyOne008ControlFieldValidationRule();
    QuickMarc quickMarc = getMockAsObject(QM_RECORD_HOLDINGS, QuickMarc.class);
    assertDoesNotThrow(() -> rule.validate(quickMarc.getFields()));
  }

  @Test()
  void testGeneralInformationControlFieldValidationRuleInvalidAmountOfField() {
    logger.info("Testing Holdings General Information Validation rule - two 008 fields");
    var rule = new OnlyOne008ControlFieldValidationRule();
    QuickMarc quickMarc = getMockAsObject(QM_RECORD_HOLDINGS, QuickMarc.class);
    List<FieldItem> fields = quickMarc.getFields();
    fields.add(new FieldItem().tag("008").content("dsdsds"));
    Optional<ValidationError> validationError = rule.validate(fields);
    assertTrue(validationError.isPresent());
    assertThat(validationError.get().getTag(), Is.is("008"));
    assertThat(validationError.get().getMessage(), Is.is("Is unique tag"));
  }
}
