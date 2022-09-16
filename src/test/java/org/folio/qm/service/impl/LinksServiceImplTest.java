package org.folio.qm.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomUtils;
import org.folio.qm.client.LinksClient;
import org.folio.qm.client.LinksClient.InstanceLink;
import org.folio.qm.client.LinksClient.InstanceLinks;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.support.types.UnitTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class LinksServiceImplTest {

  @Mock
  private LinksClient linksClient;

  @InjectMocks
  private LinksServiceImpl service;

  public static Stream<Arguments> testData() {
    return Stream.of(
      arguments(
        Collections.emptyList(),
        List.of(
          getFieldItem()),
        0
      ),
      arguments(
        List.of(
          getInstanceLink(UUID.fromString("b9a5f035-de63-4e2c-92c2-07240c88b817"), List.of("a", "b", "c"))),
        List.of(
          getFieldItem()),
        1
      ),
      arguments(
        List.of(
          getInstanceLink(UUID.fromString("b9a5f035-de63-4e2c-92c2-07240c88b817"), List.of("a", "b", "c")),
          getInstanceLink(UUID.fromString("81404611-aab2-4e24-806a-c1e9b8eb3ad1"), List.of("a", "b"))),
        List.of(
          getFieldItem(),
          getFieldItem("b9a5f035-de63-4e2c-92c2-07240c88b817"),
          getFieldItem("81404611-aab2-4e24-806a-c1e9b8eb3ad1")),
        2
      ),
      arguments(
        List.of(
          getInstanceLink(UUID.fromString("b9a5f035-de63-4e2c-92c2-07240c88b817"), List.of("a", "b", "c"))),
        List.of(
          getFieldItem(),
          getFieldItem()),
        0
      )
    );
  }

  @ParameterizedTest
  @MethodSource("testData")
  void testRecordLinksSet(List<InstanceLink> links, List<FieldItem> fieldItems, Integer linkedFieldsCount) {
    var instanceLinks = new InstanceLinks(links, 1);

    when(linksClient.fetchLinksByInstanceId(any())).thenReturn(Optional.of(instanceLinks));

    var record = getQuickMarc(fieldItems);
    service.setRecordLinks(record);

    var linkedFields = record.getFields().stream()
      .filter(fieldItem -> fieldItem.getAuthorityId() != null)
      .collect(Collectors.toList());
    assertThat(linkedFields)
      .hasSize(linkedFieldsCount);

    for (int i = 0; i < linkedFields.size(); i++) {
      var linkedField = linkedFields.get(i);
      assertThat(linkedField.getAuthorityId())
        .isEqualTo(links.get(i).getAuthorityId());
      assertThat(linkedField.getAuthorityControlledSubfields())
        .isEqualTo(links.get(i).getBibRecordSubfields());
    }
  }

  @ParameterizedTest
  @EnumSource(value = MarcFormat.class, names = "BIBLIOGRAPHIC", mode = EnumSource.Mode.EXCLUDE)
  void testRecordLinksSetNotBib(MarcFormat marcFormat) {
    var quickMarc = new QuickMarc().marcFormat(marcFormat);
    service.setRecordLinks(quickMarc);
    verifyNoInteractions(linksClient);
  }

  private static FieldItem getFieldItem() {
    return new FieldItem().tag("650").indicators(List.of("\\", "\\")).content("$a bcdefghijklmn");
  }

  private static FieldItem getFieldItem(String authorityId) {
    return getFieldItem().authorityId(UUID.fromString(authorityId));
  }

  private static InstanceLink getInstanceLink(UUID authorityId, List<String> subfields) {
    return new InstanceLink(RandomUtils.nextInt(),
      authorityId,
      "12345",
      UUID.fromString("b9a5f035-de63-4e2c-92c2-07240c89b817"),
      "650",
      subfields);
  }

  private QuickMarc getQuickMarc(List<FieldItem> fieldItems) {
    return new QuickMarc().marcFormat(MarcFormat.BIBLIOGRAPHIC).fields(fieldItems);
  }

}
