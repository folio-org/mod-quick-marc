package org.folio.qm.convertion.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.support.utils.JsonTestUtils.getMockAsObject;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_VIEW_AUTHORITY_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_VIEW_BIB_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_VIEW_HOLDINGS_PATH;
import static org.folio.support.utils.TestEntitiesUtils.SOURCE_RECORD_AUTHORITY_PATH;
import static org.folio.support.utils.TestEntitiesUtils.SOURCE_RECORD_BIB_PATH;
import static org.folio.support.utils.TestEntitiesUtils.SOURCE_RECORD_HOLDINGS_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import lombok.SneakyThrows;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.folio.Record;
import org.folio.qm.convertion.field.MarcFieldsConverter;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SourceRecordToQuickMarcViewConverterTest {

  private @Mock MarcFieldsConverter fieldsConverter;
  private @Spy RecordTypeToMarcFormatConverter recordTypeConverter;
  private SourceRecordToQuickMarcViewConverter converter;

  @BeforeEach
  void setUp() {
    converter = new SourceRecordToQuickMarcViewConverter(new ObjectMapper(), recordTypeConverter, fieldsConverter);
  }

  @SneakyThrows
  @ParameterizedTest
  @CsvSource(value = {
    SOURCE_RECORD_AUTHORITY_PATH + "," + QM_RECORD_VIEW_AUTHORITY_PATH + "," + "01725cz\\\\a2200433n\\\\4500",
    SOURCE_RECORD_HOLDINGS_PATH + "," + QM_RECORD_VIEW_HOLDINGS_PATH + "," + "01717cx\\\\a2200433zn\\4500",
    SOURCE_RECORD_BIB_PATH + "," + QM_RECORD_VIEW_BIB_PATH + "," + "01750ccm\\a2200421\\\\\\4500"
  })
  void testConvertDtoRecord(String sourceRecordPath, String quickMarcRecordPath, String expectedLeader) {
    var expected = getMockAsObject(quickMarcRecordPath, QuickMarcView.class);
    assertNotNull(expected.getUpdateInfo());
    expected.getUpdateInfo().setUpdatedBy(null);
    when(fieldsConverter.convertDtoFields(any(), any(), any())).thenReturn(expected.getFields());
    when(fieldsConverter.reorderFieldsBasedOnParsedRecordOrder(any(), any())).thenReturn(expected.getFields());

    var sourceRecord = getMockAsObject(sourceRecordPath, Record.class);
    var actual = converter.convert(sourceRecord);

    assertEquals(expected, actual);
    assertThat(actual)
      .isNotNull()
      .hasFieldOrPropertyWithValue("leader", expectedLeader)
      .hasFieldOrPropertyWithValue("marcFormat", expected.getMarcFormat())
      .hasFieldOrPropertyWithValue("parsedRecordId", expected.getParsedRecordId())
      .hasFieldOrPropertyWithValue("parsedRecordDtoId", expected.getParsedRecordDtoId())
      .hasFieldOrPropertyWithValue("suppressDiscovery", expected.getSuppressDiscovery())
      .hasFieldOrPropertyWithValue("updateInfo.recordState", expected.getUpdateInfo().getRecordState())
      .hasFieldOrPropertyWithValue("updateInfo.updateDate", expected.getUpdateInfo().getUpdateDate())
      .extracting(QuickMarcView::getFields).asInstanceOf(InstanceOfAssertFactories.LIST)
      .hasSize(expected.getFields().size());
  }
}
