package org.folio.qm.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.marc4j.marc.impl.DataFieldImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MarcQmToMarcRecordConverterTest {

  @Mock
  private MarcFieldsConverter fieldsConverter;

  @InjectMocks
  private MarcQmToMarcRecordConverter converter;

  @Test
  void convert_emptyFieldContent() {
    var source = new BaseMarcRecord().leader("test");
    var tag = "245";

    when(fieldsConverter.convertQmFields(any(), any())).thenReturn(List.of(new DataFieldImpl(tag, '/', '/')));

    var actual = converter.convert(source);

    assertThat(actual).isNotNull();
    assertThat(actual.dataFields()).hasSize(1);
    assertThat(actual.dataFields().get(0))
      .matches(field -> tag.equals(field.tag()) && field.subfields().isEmpty());
  }

  @Test
  void convert_emptyFieldIndicatorContent() {
    var source = new BaseMarcRecord().leader("test");
    var tag = "245";

    when(fieldsConverter.convertQmFields(any(), any())).thenReturn(List.of(new DataFieldImpl(tag, ' ', ' ')));

    var actual = converter.convert(source);

    assertThat(actual).isNotNull();
    assertThat(actual.dataFields()).hasSize(1);
    assertThat(actual.dataFields().get(0).indicators()).isNotNull();
    assertThat(actual.dataFields().get(0).indicators()).hasSize(2);
    actual.dataFields().get(0).indicators().forEach(ind -> assertEquals('\\', ind.value()));
  }
}
