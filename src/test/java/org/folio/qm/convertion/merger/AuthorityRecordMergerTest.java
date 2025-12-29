package org.folio.qm.convertion.merger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.folio.Note_;
import org.folio.qm.domain.model.AuthorityRecord;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@UnitTest
class AuthorityRecordMergerTest {

  private static final String ID = "id";
  private static final String SOURCE_NAME = "sourceName";
  private static final String TARGET_NAME = "targetName";
  private static final String SOURCE_FILE_ID = "sourceFileId";
  private static final String TARGET_FILE_ID = "targetFileId";
  private static final String NOTE_TEXT = "Existing note";

  private final AuthorityRecordMerger merger = Mappers.getMapper(AuthorityRecordMerger.class);

  @Test
  void shouldUpdateNonNullFields() {
    var source = createAuthority(ID, SOURCE_NAME, SOURCE_FILE_ID, null);
    var target = createAuthority(ID, TARGET_NAME, TARGET_FILE_ID, null);

    merger.merge(source, target);

    assertEquals(ID, target.getId());
    assertEquals(SOURCE_NAME, target.getPersonalName());
    assertEquals(SOURCE_FILE_ID, target.getSourceFileId());
  }

  @Test
  void shouldNotOverwriteWithNullFields() {
    var source = createAuthority(null, null, SOURCE_FILE_ID, Collections.emptyList());
    var notes = new ArrayList<>(Collections.singletonList(new Note_().withNote(NOTE_TEXT)));
    var target = createAuthority(ID, TARGET_NAME, TARGET_FILE_ID, notes);

    merger.merge(source, target);

    assertEquals(ID, target.getId());
    assertTrue(target.getNotes().isEmpty());
    assertEquals(TARGET_NAME, target.getPersonalName());
    assertEquals(SOURCE_FILE_ID, target.getSourceFileId());
  }

  @Test
  void shouldDoNothingIfSourceIsAllNull() {
    var source = createAuthority(null, null, null, null);
    var target = createAuthority(ID, TARGET_NAME, TARGET_FILE_ID, null);

    merger.merge(source, target);

    assertEquals(ID, target.getId());
    assertEquals(TARGET_NAME, target.getPersonalName());
    assertEquals(TARGET_FILE_ID, target.getSourceFileId());
  }

  @Test
  void shouldThrowIfTargetIsNull() {
    var source = createAuthority(ID, null, null, null);
    assertThrows(NullPointerException.class, () -> merger.merge(source, null));
  }

  private AuthorityRecord createAuthority(String id, String personalName, String sourceFileId, List<Note_> notes) {
    var authority = new AuthorityRecord();
    authority.setId(id);
    authority.setPersonalName(personalName);
    authority.setSourceFileId(sourceFileId);
    if (notes != null) {
      authority.setNotes(notes);
    }
    return authority;
  }
}
