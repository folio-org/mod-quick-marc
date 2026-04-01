package org.folio.qm.convertion.merger;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.folio.Instance;
import org.folio.qm.domain.model.InstanceFolioRecord;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class InstanceRecordMergerTest {

  private static final String TARGET_ID = "target-id";
  private static final String SOURCE_ID = "source-id";
  private static final String TARGET_TITLE = "target-title";
  private static final String SOURCE_TITLE = "source-title";
  private static final String TARGET_HRID = "target-hrid";
  private static final String SOURCE_HRID = "source-hrid";
  private static final String TARGET_NOTE = "target-note";
  private static final String SOURCE_NOTE = "source-note";
  private static final String TARGET_STATUS_ID = "target-status-id";
  private static final String SOURCE_STATUS_ID = "source-status-id";
  private static final String TARGET_STATISTICAL_CODE_ID = "target-statistical-code-id";
  private static final String SOURCE_STATISTICAL_CODE_ID = "source-statistical-code-id";
  private static final String TARGET_NATURE_OF_CONTENT_ID = "target-nature-of-content-id";
  private static final String SOURCE_NATURE_OF_CONTENT_ID = "source-nature-of-content-id";

  private final InstanceRecordMerger merger = new InstanceRecordMergerImpl();

  @Test
  void merge_positive_nonIgnoredNonNullFieldsAreUpdated() {
    // Arrange
    var source = createFolioInstance(SOURCE_ID, SOURCE_TITLE, SOURCE_HRID);
    var target = createInstanceRecord();

    // Act
    merger.merge(source, target);

    // Assert
    assertThat(target.getId()).isEqualTo(TARGET_ID);
    assertThat(target.getTitle()).isEqualTo(SOURCE_TITLE);
    assertThat(target.getHrid()).isEqualTo(SOURCE_HRID);
  }

  @Test
  @SuppressWarnings("checkstyle:MethodLength")
  void merge_positive_ignoredFieldsRemainUnchangedWhenSourceProvidesValues() {
    // Arrange
    var source = createFolioInstance(SOURCE_ID, SOURCE_TITLE, SOURCE_HRID);
    source.setStaffSuppress(false);
    source.setDiscoverySuppress(true);
    source.setStatisticalCodeIds(List.of(SOURCE_STATISTICAL_CODE_ID));
    source.setNatureOfContentTermIds(List.of(SOURCE_NATURE_OF_CONTENT_ID));
    source.setPreviouslyHeld(false);
    source.setStatusId(SOURCE_STATUS_ID);
    source.setAdministrativeNotes(List.of(SOURCE_NOTE));
    var target = createInstanceRecord();
    target.setStaffSuppress(true);
    target.setDiscoverySuppress(false);
    target.setStatisticalCodeIds(new LinkedHashSet<>(Set.of(TARGET_STATISTICAL_CODE_ID)));
    target.setNatureOfContentTermIds(new LinkedHashSet<>(Set.of(TARGET_NATURE_OF_CONTENT_ID)));
    target.setPreviouslyHeld(true);
    target.setStatusId(TARGET_STATUS_ID);
    target.setAdministrativeNotes(List.of(TARGET_NOTE));

    // Act
    merger.merge(source, target);

    // Assert
    assertThat(target.getAdministrativeNotes()).containsExactly(TARGET_NOTE);
    assertThat(target.getStaffSuppress()).isTrue();
    assertThat(target.getDiscoverySuppress()).isFalse();
    assertThat(target.getStatisticalCodeIds()).containsExactly(TARGET_STATISTICAL_CODE_ID);
    assertThat(target.getNatureOfContentTermIds()).containsExactly(TARGET_NATURE_OF_CONTENT_ID);
    assertThat(target.getPreviouslyHeld()).isTrue();
    assertThat(target.getStatusId()).isEqualTo(TARGET_STATUS_ID);
  }

  @Test
  void merge_positive_allNullNonIgnoredSourceFieldsSetTargetFieldsToNull() {
    // Arrange
    var source = createFolioInstance(null, null, null);
    var target = createInstanceRecord();

    // Act
    merger.merge(source, target);

    // Assert
    assertThat(target.getId()).isEqualTo(TARGET_ID);
    assertThat(target.getTitle()).isNull();
    assertThat(target.getHrid()).isNull();
  }

  private static Instance createFolioInstance(String id, String title, String hrid) {
    var instance = new Instance();
    instance.setId(id);
    instance.setTitle(title);
    instance.setHrid(hrid);
    return instance;
  }

  private InstanceFolioRecord createInstanceRecord() {
    var instance = new InstanceFolioRecord();
    instance.setId(TARGET_ID);
    instance.setVersion(1L);
    instance.setTitle(TARGET_TITLE);
    instance.setHrid(TARGET_HRID);
    return instance;
  }
}
