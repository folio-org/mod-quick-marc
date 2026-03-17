package org.folio.qm.service.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.folio.rest.jaxrs.model.Instance;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@UnitTest
class InstanceToInstanceRecordMapperTest {

  private static final String CONSORTIUM_MARC_SOURCE = "CONSORTIUM-MARC";
  private final InstanceToInstanceRecordMapper mapper =
    Mappers.getMapper(InstanceToInstanceRecordMapper.class);

  @Test
  void toInstanceRecord_shouldMapFieldsCorrectly() {
    // given
    var instanceId = UUID.randomUUID().toString();
    var instance = new Instance().withId(instanceId).withTitle("Test Title");

    // when
    var result = mapper.toInstanceRecord(instance);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(instanceId);
    assertThat(result.getTitle()).isEqualTo("Test Title");

    // constant mapping
    assertThat(result.getSource()).isEqualTo(CONSORTIUM_MARC_SOURCE);

    // ignored fields
    assertThat(result.getPrecedingTitles()).isNull();
    assertThat(result.getSucceedingTitles()).isNull();
  }

  @Test
  void toInstanceRecord_shouldReturnNull_whenInputIsNull() {
    // when
    var result = mapper.toInstanceRecord(null);

    // then
    assertThat(result).isNull();
  }

  @Test
  void toInstanceRecord_shouldHandleEmptyInstance() {
    // given
    var instance = new Instance();

    // when
    var result = mapper.toInstanceRecord(instance);

    // then
    assertThat(result).isNotNull();

    // fields should be null except constant
    assertThat(result.getId()).isNull();
    assertThat(result.getTitle()).isNull();
    assertThat(result.getSource()).isEqualTo(CONSORTIUM_MARC_SOURCE);
  }

  @Test
  void toInstanceRecord_shouldOverrideSourceWithConstant() {
    // given
    var instance = new Instance().withSource("MARC");

    // when
    var result = mapper.toInstanceRecord(instance);

    // then
    assertThat(result.getSource()).isEqualTo(CONSORTIUM_MARC_SOURCE);
  }
}
