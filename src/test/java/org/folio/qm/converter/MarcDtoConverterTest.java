package org.folio.qm.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.qm.support.utils.JsonTestUtils.getMockAsObject;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_AUTHORITY_DTO_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_BIB_DTO_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORD_HOLDINGS_DTO_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_VIEW_AUTHORITY_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_VIEW_BIB_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_RECORD_VIEW_HOLDINGS_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.mapper.MarcTypeMapperImpl;
import org.folio.qm.support.types.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MarcDtoConverterTest {

  private MarcDtoConverter converter;
  private MarcFieldsConverter fieldsConverter;

  @BeforeEach
  void setUp() {
    fieldsConverter = mock(MarcFieldsConverter.class);
    converter = new MarcDtoConverter(new ObjectMapper(), new MarcTypeMapperImpl(), fieldsConverter);
  }

  @SneakyThrows
  @ParameterizedTest
  @CsvSource(value = {
    PARSED_RECORD_AUTHORITY_DTO_PATH + "," + QM_RECORD_VIEW_AUTHORITY_PATH + "," + "06059cz\\\\a2201201n\\\\4500",
    PARSED_RECORD_HOLDINGS_DTO_PATH + "," + QM_RECORD_VIEW_HOLDINGS_PATH + "," + "00241cx\\\\a2200109zn\\4500",
    PARSED_RECORD_BIB_DTO_PATH + "," + QM_RECORD_VIEW_BIB_PATH + "," + "01706ccm\\a2200361\\\\\\4500"
  })
  void testConvertDtoRecord(String parsedRecordDtoPath, String quickMarcJsonPath, String expectedLeader) {
    var parsedRecordDto = getMockAsObject(parsedRecordDtoPath, ParsedRecordDto.class);
    var expected = getMockAsObject(quickMarcJsonPath, QuickMarcView.class);
    when(fieldsConverter.convertDtoFields(any(), any(), any())).thenReturn(expected.getFields());

    QuickMarcView actual = converter.convert(parsedRecordDto);

    assertThat(actual)
      .isNotNull()
      .hasFieldOrPropertyWithValue("leader", expectedLeader)
      .hasFieldOrPropertyWithValue("marcFormat", expected.getMarcFormat())
      .hasFieldOrPropertyWithValue("parsedRecordId", expected.getParsedRecordId())
      .hasFieldOrPropertyWithValue("parsedRecordDtoId", expected.getParsedRecordDtoId())
      .hasFieldOrPropertyWithValue("suppressDiscovery", expected.getSuppressDiscovery())
      .hasFieldOrPropertyWithValue("updateInfo.recordState", expected.getUpdateInfo().getRecordState())
      .hasFieldOrPropertyWithValue("updateInfo.updateDate", expected.getUpdateInfo().getUpdateDate())
      .extracting(QuickMarcView::getFields).asList()
      .hasSize(expected.getFields().size());
  }

}
