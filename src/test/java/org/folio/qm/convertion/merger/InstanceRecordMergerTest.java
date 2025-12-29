package org.folio.qm.convertion.merger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.folio.Instance;
import org.folio.qm.domain.model.InstanceRecord;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@UnitTest
class InstanceRecordMergerTest {

  private static final String ID = "id";
  private static final String SOURCE_TITLE = "sourceTitle";
  private static final String TARGET_TITLE = "targetTitle";
  private static final String SOURCE_HRID = "sourceHrid";
  private static final String TARGET_HRID = "targetHrid";
  private static final String SOURCE_NOTE = "sourceNote";
  private static final String TARGET_NOTE = "targetNote";

  private final InstanceRecordMerger mapper = Mappers.getMapper(InstanceRecordMerger.class);

  @Test
  void shouldUpdateNonNullFields() {
    var source = createFolioInstance(ID, SOURCE_TITLE, SOURCE_HRID, new ArrayList<>(List.of(SOURCE_NOTE)));
    var target = createInstance(TARGET_HRID, new ArrayList<>(List.of(TARGET_NOTE)));

    mapper.merge(source, target);

    assertEquals(ID, target.getId());
    assertEquals(SOURCE_TITLE, target.getTitle());
    assertEquals(SOURCE_HRID, target.getHrid());
    assertEquals(List.of(SOURCE_NOTE), target.getAdministrativeNotes());
  }

  @Test
  void shouldNotOverwriteWithNullFields() {
    var source = createFolioInstance(null, null, SOURCE_HRID, Collections.emptyList());
    var target = createInstance(null, new ArrayList<>(List.of(TARGET_NOTE)));

    mapper.merge(source, target);

    assertEquals(ID, target.getId());
    assertEquals(TARGET_TITLE, target.getTitle());
    assertEquals(SOURCE_HRID, target.getHrid());
    assertTrue(target.getAdministrativeNotes().isEmpty());
  }

  @Test
  void shouldDoNothingIfSourceIsAllNull() {
    var source = createFolioInstance(null, null, null, null);
    var target = createInstance(TARGET_HRID, new ArrayList<>(List.of(TARGET_NOTE)));

    mapper.merge(source, target);

    assertEquals(ID, target.getId());
    assertEquals(TARGET_TITLE, target.getTitle());
    assertEquals(TARGET_HRID, target.getHrid());
    assertTrue(target.getAdministrativeNotes().isEmpty());
  }

  @Test
  void shouldThrowIfTargetIsNull() {
    var source = createFolioInstance(ID, null, null, null);

    assertThrows(NullPointerException.class, () -> mapper.merge(source, null));
  }

  private Instance createFolioInstance(String id, String title, String hrid, List<String> notes) {
    var instance = new Instance();
    instance.setId(id);
    instance.setTitle(title);
    instance.setHrid(hrid);
    if (notes != null) {
      instance.setAdministrativeNotes(notes);
    }
    return instance;
  }

  private InstanceRecord createInstance(String hrid, List<String> notes) {
    var instance = new InstanceRecord();
    instance.setId(ID);
    instance.setTitle(TARGET_TITLE);
    instance.setHrid(hrid);
    if (notes != null) {
      instance.setAdministrativeNotes(notes);
    }
    return instance;
  }
}
