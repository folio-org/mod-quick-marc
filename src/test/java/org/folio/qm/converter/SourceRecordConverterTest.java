package org.folio.qm.converter;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.folio.qm.client.model.SourceRecord;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.mapper.MarcTypeMapperImpl;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SourceRecordConverterTest {

  private SourceRecordConverter converter;
  private MarcFieldsConverter fieldsConverter;

  @BeforeEach
  void setUp() {
    fieldsConverter = mock(MarcFieldsConverter.class);
    converter = new SourceRecordConverter(new ObjectMapper(), new MarcTypeMapperImpl(), fieldsConverter);
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

    var sourceRecord = getMockAsObject(sourceRecordPath, SourceRecord.class);
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
