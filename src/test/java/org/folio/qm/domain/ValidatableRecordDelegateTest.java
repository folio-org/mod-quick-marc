package org.folio.qm.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.folio.qm.domain.dto.ValidatableRecordFieldsInner;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class ValidatableRecordDelegateTest {

  @Test
  void testDelegateMethods() {
    var validatableRecord = new ValidatableRecord();
    validatableRecord.setMarcFormat(MarcFormat.BIBLIOGRAPHIC);
    validatableRecord.setLeader("leader");

    var field = new ValidatableRecordFieldsInner();
    field.setContent("content");
    field.setTag("001");
    field.setIndicators(List.of("indicator1", "indicator2"));
    validatableRecord.setFields(List.of(field));

    var delegate = new ValidatableRecordDelegate(validatableRecord);

    assertEquals(validatableRecord.getLeader(), delegate.getLeader());
    assertEquals(validatableRecord.getMarcFormat(), delegate.getMarcFormat());

    var fieldItem = delegate.getFields().getFirst();
    assertEquals(field.getTag(), fieldItem.getTag());
    assertEquals(field.getContent(), fieldItem.getContent());
    assertEquals(field.getIndicators(), fieldItem.getIndicators());
  }

  @Test
  void testEqualsAndHashCode() {
    var record1 = new ValidatableRecord();
    record1.setMarcFormat(MarcFormat.BIBLIOGRAPHIC);
    record1.setLeader("leader");
    record1.setFields(Collections.emptyList());

    var record2 = new ValidatableRecord();
    record2.setMarcFormat(MarcFormat.BIBLIOGRAPHIC);
    record2.setLeader("leader");
    record2.setFields(Collections.emptyList());

    var delegate1 = new ValidatableRecordDelegate(record1);
    var delegate2 = new ValidatableRecordDelegate(record2);
    assertEquals(delegate2, delegate1);
    assertEquals(delegate2.hashCode(), delegate1.hashCode());
  }
}
