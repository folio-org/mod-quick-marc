package org.folio.qm.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;
import org.folio.qm.converter.field.FieldItemConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.marc4j.marc.impl.DataFieldImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MarcFieldsSoftConverterTest {

  @Mock
  private FieldItemConverter fieldItemConverter;

  @Spy
  private final List<FieldItemConverter> fieldItemConverters = new LinkedList<>();

  @InjectMocks
  private MarcFieldsSoftConverter converter;

  @BeforeEach
  void init() {
    fieldItemConverters.add(fieldItemConverter);
  }

  @Test
  void convertQmFieldsSoft() {
    var fieldItem = new FieldItem();
    var marcFormat = MarcFormat.BIBLIOGRAPHIC;
    var expected = new DataFieldImpl("245", '/', '/');

    when(fieldItemConverter.canProcess(fieldItem, marcFormat)).thenReturn(true);
    when(fieldItemConverter.convert(fieldItem, true)).thenReturn(expected);

    var actual = converter.convertQmFields(List.of(fieldItem), marcFormat);

    assertThat(actual).containsOnly(expected);
    verify(fieldItemConverter).convert(fieldItem, true);
  }
}
