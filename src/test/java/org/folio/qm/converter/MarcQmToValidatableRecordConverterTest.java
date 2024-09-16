package org.folio.qm.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
public class MarcQmToValidatableRecordConverterTest {

  @InjectMocks
  private MarcQmToValidatableRecordConverter converter;

  @Test
  void convertToValidatableRecord() {
    var fields = new FieldItem().tag("246").indicators(List.of("0", "1")).content("$a test");
    var marcRecord = new BaseMarcRecord().leader("test").marcFormat(MarcFormat.BIBLIOGRAPHIC).fields(List.of(fields));

    var actual = converter.convert(marcRecord);

    assertThat(actual).isNotNull();
    var validatableRecordFieldsInner = actual.getFields().get(0);
    assertThat(actual.getLeader()).isEqualTo(marcRecord.getLeader());
    assertThat(actual.getMarcFormat()).isEqualTo(marcRecord.getMarcFormat());
    assertThat(validatableRecordFieldsInner.getTag()).isEqualTo(fields.getTag());
    assertThat(validatableRecordFieldsInner.getContent()).isEqualTo(fields.getContent());
    assertThat(validatableRecordFieldsInner.getIndicators()).isEqualTo(fields.getIndicators());
  }

  @Test
  void convertToValidatableRecordWithoutFieldItemss() {
    var marcRecord = new BaseMarcRecord().leader("test").marcFormat(MarcFormat.BIBLIOGRAPHIC);

    var actual = converter.convert(marcRecord);

    assertThat(actual).isNotNull();
    assertThat(actual.getLeader()).isEqualTo(marcRecord.getLeader());
    assertThat(actual.getMarcFormat()).isEqualTo(marcRecord.getMarcFormat());
  }

  @Test
  void convertToEmptyValidatableRecord() {
    var actual = converter.convert(new BaseMarcRecord());

    assertThat(actual).isNotNull();
    assertThat(actual.getLeader()).isNull();
    assertThat(actual.getMarcFormat()).isNull();
    assertThat(actual.getFields()).isEmpty();
  }
}
