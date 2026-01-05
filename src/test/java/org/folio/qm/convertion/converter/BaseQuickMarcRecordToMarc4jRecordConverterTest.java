package org.folio.qm.convertion.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.folio.qm.convertion.field.MarcFieldsConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.spring.testing.type.UnitTest;
import org.folio.support.StubQuickMarcRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.ControlFieldImpl;
import org.marc4j.marc.impl.DataFieldImpl;
import org.marc4j.marc.impl.LeaderImpl;
import org.marc4j.marc.impl.MarcFactoryImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class BaseQuickMarcRecordToMarc4jRecordConverterTest {

  @Mock
  private MarcFactory factory;

  @Mock
  private MarcFieldsConverter fieldsConverter;

  @InjectMocks
  private BaseQuickMarcRecordToMarc4jRecordConverter converter;

  @Test
  void convert_shouldConvertBaseMarcRecordWithLeaderAndFields() {
    // given
    var leaderString = "00000nam\\\\2200000\\u\\4500";
    var fields = List.of(new FieldItem().tag("001"), new FieldItem().tag("245"));
    var source = new StubQuickMarcRecord(MarcFormat.BIBLIOGRAPHIC, leaderString, fields);

    var marcRecord = new MarcFactoryImpl().newRecord();
    var leader = new LeaderImpl("00000nam  2200000 u 4500");
    var controlField = new ControlFieldImpl("001", "test001");
    var dataField = new DataFieldImpl("245", '0', '0');

    when(factory.newRecord()).thenReturn(marcRecord);
    when(factory.newLeader("00000nam  2200000 u 4500")).thenReturn(leader);
    when(fieldsConverter.convertQmFields(fields, MarcFormat.BIBLIOGRAPHIC))
      .thenReturn(List.of(controlField, dataField));

    // when
    var result = converter.convert(source);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getLeader()).isEqualTo(leader);
    verify(factory).newRecord();
    verify(factory).newLeader("00000nam  2200000 u 4500");
    verify(fieldsConverter).convertQmFields(fields, MarcFormat.BIBLIOGRAPHIC);
  }

  @ValueSource(strings = "   ")
  @NullAndEmptySource
  @ParameterizedTest
  void convert_shouldConvertBaseMarcRecordBlankLeader(String leaderString) {
    // given
    var fields = List.of(new FieldItem().tag("245"));
    var source = new StubQuickMarcRecord(MarcFormat.BIBLIOGRAPHIC, leaderString, fields);

    var marcRecord = new MarcFactoryImpl().newRecord();
    var dataField = new DataFieldImpl("245", '0', '0');

    when(factory.newRecord()).thenReturn(marcRecord);
    when(fieldsConverter.convertQmFields(fields, MarcFormat.BIBLIOGRAPHIC))
      .thenReturn(List.of(dataField));

    // when
    var result = converter.convert(source);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getLeader().marshal()).isEqualTo("00000nam a2200000 a 4500");
    verify(factory).newRecord();
    verify(fieldsConverter).convertQmFields(fields, MarcFormat.BIBLIOGRAPHIC);
  }

  @Test
  void convert_shouldConvertBaseMarcRecordWithEmptyFields() {
    // given
    var leaderString = "00000nam\\\\2200000\\u\\4500";
    var source = new StubQuickMarcRecord(MarcFormat.BIBLIOGRAPHIC, leaderString, Collections.emptyList());

    var marcRecord = new MarcFactoryImpl().newRecord();
    var leader = new LeaderImpl("00000nam  2200000 u 4500");

    when(factory.newRecord()).thenReturn(marcRecord);
    when(factory.newLeader("00000nam  2200000 u 4500")).thenReturn(leader);
    when(fieldsConverter.convertQmFields(Collections.emptyList(), MarcFormat.BIBLIOGRAPHIC))
      .thenReturn(Collections.emptyList());

    // when
    var result = converter.convert(source);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getLeader()).isEqualTo(leader);
    verify(factory).newRecord();
    verify(factory).newLeader("00000nam  2200000 u 4500");
    verify(fieldsConverter).convertQmFields(Collections.emptyList(), MarcFormat.BIBLIOGRAPHIC);
  }

  @Test
  void convert_shouldRestoreBlanksInLeader() {
    // given
    var leaderString = "00000nam\\\\2200000\\u\\4500";
    var fields = List.of(new FieldItem().tag("001"));
    var source = new StubQuickMarcRecord(MarcFormat.BIBLIOGRAPHIC, leaderString, fields);

    var marcRecord = new MarcFactoryImpl().newRecord();
    var leader = new LeaderImpl("00000nam  2200000 u 4500");
    var controlField = new ControlFieldImpl("001", "test001");

    when(factory.newRecord()).thenReturn(marcRecord);
    when(factory.newLeader("00000nam  2200000 u 4500")).thenReturn(leader);
    when(fieldsConverter.convertQmFields(fields, MarcFormat.BIBLIOGRAPHIC))
      .thenReturn(List.of(controlField));

    // when
    var result = converter.convert(source);

    // then
    assertThat(result).isNotNull();
    verify(factory).newLeader("00000nam  2200000 u 4500");
  }

  @Test
  void convert_shouldAddAllVariableFieldsToRecord() {
    // given
    var fields = List.of(new FieldItem().tag("001"),
      new FieldItem().tag("008"),
      new FieldItem().tag("245"),
      new FieldItem().tag("650"));
    var leaderString = "00000nam\\\\2200000\\u\\4500";
    var source = new StubQuickMarcRecord(MarcFormat.BIBLIOGRAPHIC, leaderString, fields);

    var marcRecord = new MarcFactoryImpl().newRecord();
    var leader = new LeaderImpl("00000nam  2200000 u 4500");
    List<VariableField> variableFields = List.of(
      new ControlFieldImpl("001", "test001"),
      new ControlFieldImpl("008", "test008"),
      new DataFieldImpl("245", '0', '0'),
      new DataFieldImpl("650", ' ', '0')
    );

    when(factory.newRecord()).thenReturn(marcRecord);
    when(factory.newLeader(any())).thenReturn(leader);
    when(fieldsConverter.convertQmFields(fields, MarcFormat.BIBLIOGRAPHIC))
      .thenReturn(variableFields);

    // when
    var result = converter.convert(source);

    // then
    assertThat(result).isNotNull();
    verify(fieldsConverter).convertQmFields(fields, MarcFormat.BIBLIOGRAPHIC);
  }

  @Test
  void convert_shouldHandleAuthorityMarcFormat() {
    // given
    var fields = List.of(new FieldItem().tag("100"));
    var leaderString = "00000nz\\\\\\2200000\\u\\4500";
    var source = new StubQuickMarcRecord(MarcFormat.AUTHORITY, leaderString, fields);

    var marcRecord = new MarcFactoryImpl().newRecord();
    var leader = new LeaderImpl("00000nz   2200000 u 4500");
    var dataField = new DataFieldImpl("100", '1', ' ');

    when(factory.newRecord()).thenReturn(marcRecord);
    when(factory.newLeader("00000nz   2200000 u 4500")).thenReturn(leader);
    when(fieldsConverter.convertQmFields(fields, MarcFormat.AUTHORITY))
      .thenReturn(List.of(dataField));

    // when
    var result = converter.convert(source);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getLeader()).isEqualTo(leader);
    verify(fieldsConverter).convertQmFields(fields, MarcFormat.AUTHORITY);
  }

  @Test
  void convert_shouldHandleHoldingsMarcFormat() {
    // given
    var fields = List.of(new FieldItem().tag("852"));
    var leaderString = "00000nx\\\\\\2200000\\u\\4500";
    var source = new StubQuickMarcRecord(MarcFormat.HOLDINGS, leaderString, fields);

    var marcRecord = new MarcFactoryImpl().newRecord();
    var leader = new LeaderImpl("00000nx   2200000 u 4500");
    var dataField = new DataFieldImpl("852", ' ', ' ');

    when(factory.newRecord()).thenReturn(marcRecord);
    when(factory.newLeader("00000nx   2200000 u 4500")).thenReturn(leader);
    when(fieldsConverter.convertQmFields(fields, MarcFormat.HOLDINGS))
      .thenReturn(List.of(dataField));

    // when
    var result = converter.convert(source);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getLeader()).isEqualTo(leader);
    verify(fieldsConverter).convertQmFields(fields, MarcFormat.HOLDINGS);
  }
}
