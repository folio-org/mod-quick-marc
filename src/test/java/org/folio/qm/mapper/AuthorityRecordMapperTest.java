package org.folio.qm.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import org.folio.Authority;
import org.folio.Note_;
import org.folio.qm.converter.AuthorityRecordMapper;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@UnitTest
class AuthorityRecordMapperTest {

  private static final String ID = "id";
  private static final String SOURCE_NAME = "sourceName";
  private static final String TARGET_NAME = "targetName";
  private static final String SOURCE_FILE_ID = "sourceFileId";
  private static final String TARGET_FILE_ID = "targetFileId";
  private static final String NOTE_TEXT = "Existing note";

  private final AuthorityRecordMapper mapper = Mappers.getMapper(AuthorityRecordMapper.class);

  @Test
  void shouldUpdateNonNullFields() {
    Authority source = createAuthority(ID, SOURCE_NAME, SOURCE_FILE_ID, null);
    Authority target = createAuthority(ID, TARGET_NAME, TARGET_FILE_ID, null);

    mapper.merge(source, target);

    assertEquals(ID, target.getId());
    assertEquals(SOURCE_NAME, target.getPersonalName());
    assertEquals(SOURCE_FILE_ID, target.getSourceFileId());
  }

  @Test
  void shouldNotOverwriteWithNullFields() {
    Authority source = createAuthority(null, null, SOURCE_FILE_ID, Collections.emptyList());
    java.util.List<Note_> notes = new ArrayList<>(Collections.singletonList(new Note_().withNote(NOTE_TEXT)));
    Authority target = createAuthority(ID, TARGET_NAME, TARGET_FILE_ID, notes);

    mapper.merge(source, target);

    assertEquals(ID, target.getId());
    assertTrue(target.getNotes().isEmpty());
    assertEquals(TARGET_NAME, target.getPersonalName());
    assertEquals(SOURCE_FILE_ID, target.getSourceFileId());
  }

  @Test
  void shouldDoNothingIfSourceIsAllNull() {
    Authority source = createAuthority(null, null, null, null);
    Authority target = createAuthority(ID, TARGET_NAME, TARGET_FILE_ID, null);

    mapper.merge(source, target);

    assertEquals(ID, target.getId());
    assertEquals(TARGET_NAME, target.getPersonalName());
    assertEquals(TARGET_FILE_ID, target.getSourceFileId());
  }

  @Test
  void shouldThrowIfTargetIsNull() {
    Authority source = createAuthority(ID, null, null, null);
    assertThrows(NullPointerException.class, () -> mapper.merge(source, null));
  }

  private Authority createAuthority(String id, String personalName, String sourceFileId, java.util.List<Note_> notes) {
    Authority authority = new Authority();
    authority.setId(id);
    authority.setPersonalName(personalName);
    authority.setSourceFileId(sourceFileId);
    if (notes != null) {
      authority.setNotes(notes);
    }
    return authority;
  }
}
