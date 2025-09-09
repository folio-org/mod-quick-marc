package org.folio.qm.converter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.support.utils.JsonTestUtils.getMockAsObject;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_EDIT_AUTHORITY_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_EDIT_BIB_PATH;
import static org.folio.support.utils.TestEntitiesUtils.QM_RECORD_EDIT_HOLDINGS_PATH;
import static org.folio.support.utils.TestEntitiesUtils.SOURCE_RECORD_AUTHORITY_PATH;
import static org.folio.support.utils.TestEntitiesUtils.SOURCE_RECORD_BIB_PATH;
import static org.folio.support.utils.TestEntitiesUtils.SOURCE_RECORD_HOLDINGS_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.folio.qm.domain.dto.ParsedRecord;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.SourceRecord;
import org.folio.qm.domain.dto.State;
import org.folio.qm.exception.ConverterException;
import org.folio.qm.mapper.MarcTypeMapperImpl;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.marc4j.MarcJsonReader;
import org.marc4j.marc.Record;
import org.marc4j.marc.impl.MarcFactoryImpl;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MarcQmConverterTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private MarcQmEditConverter converter;
  private MarcFieldsConverter fieldsConverter;

  @BeforeEach
  void setUp() {
    fieldsConverter = mock(MarcFieldsConverter.class);
    var recordConverter = new MarcQmToMarc4jConverter(new MarcFactoryImpl(), fieldsConverter);
    converter = new MarcQmEditConverter(new ObjectMapper(), new MarcTypeMapperImpl(), recordConverter);
  }

  @SneakyThrows
  @ParameterizedTest
  @CsvSource(value = {
    SOURCE_RECORD_AUTHORITY_PATH + "," + QM_RECORD_EDIT_AUTHORITY_PATH + "," + "01725cz  a2200433n  4500",
    SOURCE_RECORD_HOLDINGS_PATH + "," + QM_RECORD_EDIT_HOLDINGS_PATH + "," + "01717cx  a2200433zn 4500",
    SOURCE_RECORD_BIB_PATH + "," + QM_RECORD_EDIT_BIB_PATH + "," + "01750ccm a2200421   4500"
  })
  void testConvertDtoRecord(String parsedRecordDtoPath, String quickMarcJsonPath, String expectedLeader) {
    var expected = toDto(getMockAsObject(parsedRecordDtoPath, SourceRecord.class));
    var qmRecord = getMockAsObject(quickMarcJsonPath, QuickMarcEdit.class);
    when(fieldsConverter.convertQmFields(any(), any())).thenReturn(extractMarcRecord(expected.getParsedRecord())
      .getVariableFields());

    var actual = converter.convert(qmRecord);

    assertThat(actual)
      .isNotNull()
      .hasFieldOrPropertyWithValue("id", expected.getId())
      .hasFieldOrPropertyWithValue("recordType", expected.getRecordType())
      .hasFieldOrPropertyWithValue("parsedRecord.id", expected.getParsedRecord().getId())
      .hasFieldOrPropertyWithValue("additionalInfo", expected.getAdditionalInfo())
      .extracting(parsedRecordDto -> parsedRecordDto.getParsedRecord().getContent().toString())
      .matches(content -> content.contains(expectedLeader), "contains valid leader");

    var expectedJson = objectMapper.writeValueAsString(expected.getParsedRecord().getContent());
    var actualJson = objectMapper.writeValueAsString(actual.getParsedRecord().getContent());

    JSONAssert.assertEquals(expectedJson, actualJson, true);
  }

  private Record extractMarcRecord(ParsedRecord parsedRecord) {
    try (var input = IOUtils.toInputStream(new ObjectMapper().writeValueAsString(parsedRecord), UTF_8)) {
      return new MarcJsonReader(input).next();
    } catch (Exception e) {
      throw new ConverterException(e);
    }
  }

  private ParsedRecordDto toDto(SourceRecord sourceRecord) {
    return new ParsedRecordDto()
      .recordType(ParsedRecordDto.RecordTypeEnum.fromValue(sourceRecord.getRecordType().getValue()))
      .parsedRecord(sourceRecord.getParsedRecord())
      .additionalInfo(sourceRecord.getAdditionalInfo())
      .id(sourceRecord.getRecordId())
      .externalIdsHolder(sourceRecord.getExternalIdsHolder())
      .metadata(sourceRecord.getMetadata())
      .recordState(State.ACTUAL);
  }
}
