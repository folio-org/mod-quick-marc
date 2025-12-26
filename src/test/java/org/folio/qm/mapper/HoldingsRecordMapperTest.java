package org.folio.qm.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.folio.Holdings;
import org.folio.qm.converter.HoldingsRecordMapper;
import org.folio.rest.jaxrs.model.HoldingsRecord;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@UnitTest
class HoldingsRecordMapperTest {

  private static final String ID = "id";
  private static final String SOURCE_CALL_NUMBER = "sourceCallNumber";
  private static final String TARGET_CALL_NUMBER = "targetCallNumber";
  private static final String SOURCE_HRID = "sourceHrid";
  private static final String TARGET_HRID = "targetHrid";
  private static final String SOURCE_NOTE_1 = "sourceNote1";
  private static final String SOURCE_NOTE_2 = "sourceNote2";
  private static final String TARGET_NOTE = "targetNote";

  private final HoldingsRecordMapper mapper = Mappers.getMapper(HoldingsRecordMapper.class);

  @Test
  void shouldUpdateNonNullFields() {
    var source = createHoldings(ID, SOURCE_CALL_NUMBER, SOURCE_HRID,
      new ArrayList<>(List.of(SOURCE_NOTE_1, SOURCE_NOTE_2)));
    var target = createHoldingsRecord(null, null, null, new ArrayList<>(List.of(TARGET_NOTE)));

    mapper.merge(source, target);

    assertEquals(ID, target.getId());
    assertEquals(SOURCE_CALL_NUMBER, target.getCallNumber());
    assertEquals(SOURCE_HRID, target.getHrid());
    assertEquals(List.of(SOURCE_NOTE_1, SOURCE_NOTE_2), target.getAdministrativeNotes());
  }

  @Test
  void shouldNotOverwriteWithNullFields() {
    var source = createHoldings(null, null, SOURCE_HRID, Collections.emptyList());
    var target = createHoldingsRecord(ID, TARGET_CALL_NUMBER, TARGET_HRID, new ArrayList<>(List.of(TARGET_NOTE)));

    mapper.merge(source, target);

    assertEquals(ID, target.getId());
    assertEquals(TARGET_CALL_NUMBER, target.getCallNumber());
    assertEquals(SOURCE_HRID, target.getHrid());
    assertTrue(target.getAdministrativeNotes().isEmpty());
  }

  @Test
  void shouldDoNothingIfSourceIsAllNull() {
    var source = createHoldings(null, null, null, null);
    var target = createHoldingsRecord(ID, TARGET_CALL_NUMBER, TARGET_HRID, new ArrayList<>(List.of(TARGET_NOTE)));

    mapper.merge(source, target);

    assertEquals(ID, target.getId());
    assertEquals(TARGET_CALL_NUMBER, target.getCallNumber());
    assertEquals(TARGET_HRID, target.getHrid());
    assertEquals(List.of(TARGET_NOTE), target.getAdministrativeNotes());
  }

  @Test
  void shouldThrowIfTargetIsNull() {
    var source = createHoldings(ID, null, null, null);

    assertThrows(NullPointerException.class, () -> mapper.merge(source, null));
  }

  private Holdings createHoldings(String id, String callNumber, String hrid, List<String> notes) {
    var holdings = new Holdings();
    holdings.setId(id);
    holdings.setCallNumber(callNumber);
    holdings.setHrid(hrid);
    holdings.setAdministrativeNotes(notes);
    return holdings;
  }

  private HoldingsRecord createHoldingsRecord(String id, String callNumber, String hrid, List<String> notes) {
    var holdingsRecord = new HoldingsRecord();
    holdingsRecord.setId(id);
    holdingsRecord.setCallNumber(callNumber);
    holdingsRecord.setHrid(hrid);
    if (notes != null) {
      holdingsRecord.setAdministrativeNotes(notes);
    }
    return holdingsRecord;
  }
}
