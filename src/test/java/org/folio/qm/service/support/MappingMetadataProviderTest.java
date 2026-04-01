package org.folio.qm.service.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.qm.domain.model.MappingRecordType.MARC_AUTHORITY;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.folio.processing.mapping.defaultmapper.processor.parameters.MappingParameters;
import org.folio.qm.client.MappingMetadataClient;
import org.folio.rest.jaxrs.model.IdentifierType;
import org.folio.spring.exception.NotFoundException;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MappingMetadataProviderTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private @Mock MappingMetadataClient client;
  private @InjectMocks MappingMetadataProvider provider;

  @Test
  @SneakyThrows
  void getMappingData_positive() {
    var mappingRules = new JsonObject("{\"test\": \"test\"}");
    var mappingParams = new MappingParameters().withIdentifierTypes(List.of(new IdentifierType().withId("test")));
    when(client.getMappingMetadata(MARC_AUTHORITY.value())).thenReturn(
      new MappingMetadataClient.MappingMetadata(mappingRules.encode(), MAPPER.writeValueAsString(mappingParams)));

    var metadata = provider.getMappingData(MARC_AUTHORITY);

    assertThat(metadata.mappingRules()).isEqualTo(mappingRules);
    assertThat(metadata.mappingParameters().getIdentifierTypes()).isEqualTo(mappingParams.getIdentifierTypes());
  }

  @Test
  void getMappingData_negative_serverError() {
    when(client.getMappingMetadata(MARC_AUTHORITY.value())).thenThrow(NotFoundException.class);
    var metadata = provider.getMappingData(MARC_AUTHORITY);
    assertThat(metadata).isNull();
  }

  @Test
  void getMappingData_negative_nullServerResponse() {
    when(client.getMappingMetadata(MARC_AUTHORITY.value())).thenReturn(null);
    var metadata = provider.getMappingData(MARC_AUTHORITY);
    assertThat(metadata).isNull();
  }

  @ParameterizedTest
  @MethodSource("emptyMappingMetadataArguments")
  void getMappingData_negative_emptyServerResponse(String mappingRules, String mappingParams) {
    var mappingMetadata = new MappingMetadataClient.MappingMetadata(mappingRules, mappingParams);
    when(client.getMappingMetadata(MARC_AUTHORITY.value())).thenReturn(mappingMetadata);
    var metadata = provider.getMappingData(MARC_AUTHORITY);
    assertThat(metadata).isNull();
  }

  static Stream<Arguments> emptyMappingMetadataArguments() {
    return Stream.of(
      arguments("", ""),
      arguments("", " "),
      arguments("", "a"),
      arguments("", null),
      arguments(" ", " "),
      arguments(" ", ""),
      arguments(" ", "a"),
      arguments(" ", null),
      arguments("a", ""),
      arguments("a", " "),
      arguments("a", null),
      arguments(null, null),
      arguments(null, " "),
      arguments(null, ""),
      arguments(null, "a"));
  }
}
