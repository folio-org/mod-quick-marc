package org.folio.qm.convertion.merger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.folio.qm.domain.model.HoldingsFolioRecord;
import org.folio.rest.jaxrs.model.HoldingsRecord;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class HoldingsRecordMergerTest {

  private static final String TARGET_ID = "target-id";
  private static final String SOURCE_ID = "source-id";
  private static final String TARGET_CALL_NUMBER = "target-call-number";
  private static final String SOURCE_CALL_NUMBER = "source-call-number";
  private static final String TARGET_HRID = "target-hrid";
  private static final String SOURCE_HRID = "source-hrid";
  private static final String TARGET_NOTE = "target-note";
  private static final String SOURCE_NOTE = "source-note";
  private static final String TARGET_STATISTICAL_CODE_ID = "target-statistical-code-id";
  private static final String SOURCE_STATISTICAL_CODE_ID = "source-statistical-code-id";
  private static final String TARGET_ILL_POLICY_ID = "target-ill-policy-id";
  private static final String SOURCE_ILL_POLICY_ID = "source-ill-policy-id";
  private static final String TARGET_DIGITIZATION_POLICY = "target-digitization-policy";
  private static final String SOURCE_DIGITIZATION_POLICY = "source-digitization-policy";
  private static final String TARGET_RETENTION_POLICY = "target-retention-policy";
  private static final String SOURCE_RETENTION_POLICY = "source-retention-policy";
  private static final String TARGET_ACQUISITION_FORMAT = "target-acquisition-format";
  private static final String SOURCE_ACQUISITION_FORMAT = "source-acquisition-format";
  private static final String TARGET_ACQUISITION_METHOD = "target-acquisition-method";
  private static final String SOURCE_ACQUISITION_METHOD = "source-acquisition-method";

  private final HoldingsRecordMerger merger = new HoldingsRecordMergerImpl();

  @Test
  void merge_positive_nonIgnoredNonNullFieldsAreUpdated() {
    // Arrange
    var source = createHoldings(SOURCE_ID, SOURCE_CALL_NUMBER, SOURCE_HRID, List.of(SOURCE_NOTE));
    var target = createHoldingsRecord(TARGET_ID, TARGET_CALL_NUMBER, TARGET_HRID, List.of(TARGET_NOTE));

    // Act
    merger.merge(source, target);

    // Assert
    assertThat(target.getId()).isEqualTo(SOURCE_ID);
    assertThat(target.getCallNumber()).isEqualTo(SOURCE_CALL_NUMBER);
    assertThat(target.getHrid()).isEqualTo(SOURCE_HRID);
    assertThat(target.getAdministrativeNotes()).containsExactly(TARGET_NOTE);
  }

  @Test
  void merge_positive_nullSourceFieldsDoNotOverwriteTargetValues() {
    // Arrange
    var source = createHoldings(null, null, SOURCE_HRID, List.of(SOURCE_NOTE));
    var target = createHoldingsRecord(TARGET_ID, TARGET_CALL_NUMBER, TARGET_HRID, List.of(TARGET_NOTE));

    // Act
    merger.merge(source, target);

    // Assert
    assertThat(target.getId()).isEqualTo(TARGET_ID);
    assertThat(target.getCallNumber()).isEqualTo(TARGET_CALL_NUMBER);
    assertThat(target.getHrid()).isEqualTo(SOURCE_HRID);
    assertThat(target.getAdministrativeNotes()).containsExactly(TARGET_NOTE);
  }

  @Test
  void merge_positive_ignoredFieldsRemainUnchangedWhenSourceProvidesValues() {
    // Arrange
    var source = createHoldings(SOURCE_ID, SOURCE_CALL_NUMBER, SOURCE_HRID, List.of(SOURCE_NOTE));
    setIgnoredSourceFields(source);
    var target = createHoldingsRecord(TARGET_ID, TARGET_CALL_NUMBER, TARGET_HRID, List.of(TARGET_NOTE));
    setIgnoredTargetFields(target);

    // Act
    merger.merge(source, target);

    // Assert
    assertIgnoredTargetFieldsUnchanged(target);
  }

  @Test
  void merge_positive_allNullNonIgnoredSourceFieldsLeaveTargetUnchanged() {
    // Arrange
    var source = createHoldings(null, null, null, null);
    var target = createHoldingsRecord(TARGET_ID, TARGET_CALL_NUMBER, TARGET_HRID, List.of(TARGET_NOTE));

    // Act
    merger.merge(source, target);

    // Assert
    assertThat(target.getId()).isEqualTo(TARGET_ID);
    assertThat(target.getCallNumber()).isEqualTo(TARGET_CALL_NUMBER);
    assertThat(target.getHrid()).isEqualTo(TARGET_HRID);
    assertThat(target.getAdministrativeNotes()).containsExactly(TARGET_NOTE);
  }

  @Test
  void merge_negative_targetIsNull_throwsNullPointerException() {
    // Arrange
    var source = createHoldings(SOURCE_ID, SOURCE_CALL_NUMBER, SOURCE_HRID, List.of(SOURCE_NOTE));

    // Act / Assert
    assertThatThrownBy(() -> merger.merge(source, null))
      .isInstanceOf(NullPointerException.class);
  }

  private HoldingsRecord createHoldings(String id, String callNumber, String hrid, List<String> notes) {
    var holdings = new HoldingsRecord();
    holdings.setId(id);
    holdings.setCallNumber(callNumber);
    holdings.setHrid(hrid);
    if (notes != null) {
      holdings.setAdministrativeNotes(new ArrayList<>(notes));
    }
    return holdings;
  }

  private HoldingsFolioRecord createHoldingsRecord(String id, String callNumber, String hrid, List<String> notes) {
    var holdingsRecord = new HoldingsFolioRecord();
    holdingsRecord.setId(id);
    holdingsRecord.setCallNumber(callNumber);
    holdingsRecord.setHrid(hrid);
    if (notes != null) {
      holdingsRecord.setAdministrativeNotes(new ArrayList<>(notes));
    }
    return holdingsRecord;
  }

  private static void setIgnoredSourceFields(HoldingsRecord source) {
    source.setStatisticalCodeIds(new LinkedHashSet<>(Set.of(SOURCE_STATISTICAL_CODE_ID)));
    source.setIllPolicyId(SOURCE_ILL_POLICY_ID);
    source.setDigitizationPolicy(SOURCE_DIGITIZATION_POLICY);
    source.setRetentionPolicy(SOURCE_RETENTION_POLICY);
    source.setAcquisitionFormat(SOURCE_ACQUISITION_FORMAT);
    source.setAcquisitionMethod(SOURCE_ACQUISITION_METHOD);
  }

  private static void setIgnoredTargetFields(HoldingsRecord target) {
    target.setStatisticalCodeIds(new LinkedHashSet<>(Set.of(TARGET_STATISTICAL_CODE_ID)));
    target.setIllPolicyId(TARGET_ILL_POLICY_ID);
    target.setDigitizationPolicy(TARGET_DIGITIZATION_POLICY);
    target.setRetentionPolicy(TARGET_RETENTION_POLICY);
    target.setAcquisitionFormat(TARGET_ACQUISITION_FORMAT);
    target.setAcquisitionMethod(TARGET_ACQUISITION_METHOD);
  }

  private static void assertIgnoredTargetFieldsUnchanged(HoldingsRecord target) {
    assertThat(target.getAdministrativeNotes()).containsExactly(TARGET_NOTE);
    assertThat(target.getStatisticalCodeIds()).containsExactly(TARGET_STATISTICAL_CODE_ID);
    assertThat(target.getIllPolicyId()).isEqualTo(TARGET_ILL_POLICY_ID);
    assertThat(target.getDigitizationPolicy()).isEqualTo(TARGET_DIGITIZATION_POLICY);
    assertThat(target.getRetentionPolicy()).isEqualTo(TARGET_RETENTION_POLICY);
    assertThat(target.getAcquisitionFormat()).isEqualTo(TARGET_ACQUISITION_FORMAT);
    assertThat(target.getAcquisitionMethod()).isEqualTo(TARGET_ACQUISITION_METHOD);
  }
}
