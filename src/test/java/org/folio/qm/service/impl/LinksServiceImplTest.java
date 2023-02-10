package org.folio.qm.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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

  private static final String AUTHORITY_ID = "b9a5f035-de63-4e2c-92c2-07240c88b817";
  private static final int LINKING_RULE_ID = 1;

  @Mock
  private LinksClient linksClient;

  @InjectMocks
  private LinksServiceImpl service;

  public static Stream<Arguments> setLinksTestData() {
    return Stream.of(
      arguments(
        Collections.emptyList(),
        List.of(
          getFieldItem()),
        0
      ),
      arguments(
        List.of(
          getInstanceLink(UUID.fromString(AUTHORITY_ID), List.of("a", "b", "c"))),
        List.of(
          getFieldItem()),
        1
      ),
      arguments(
        List.of(
          getInstanceLink(UUID.fromString(AUTHORITY_ID), List.of("a", "b", "c")),
          getInstanceLink(UUID.fromString("81404611-aab2-4e24-806a-c1e9b8eb3ad1"), List.of("a", "b"))),
        List.of(
          getFieldItem(),
          getFieldItem(AUTHORITY_ID),
          getFieldItem("81404611-aab2-4e24-806a-c1e9b8eb3ad1")),
        2
      ),
      arguments(
        List.of(
          getInstanceLink(UUID.fromString(AUTHORITY_ID), List.of("a", "b", "c"))),
        List.of(
          getFieldItem(),
          getFieldItem()),
        0
      )
    );
  }

  @ParameterizedTest
  @MethodSource("setLinksTestData")
  void testRecordLinksSet(List<InstanceLink> links, List<FieldItem> fieldItemsMock, Integer expectedLinkedFieldsCount) {
    var instanceLinks = new InstanceLinks(links, LINKING_RULE_ID);

    when(linksClient.fetchLinksByInstanceId(any())).thenReturn(Optional.of(instanceLinks));

    var record = getQuickMarc(fieldItemsMock);
    service.setRecordLinks(record);

    var linkedFields = record.getFields().stream()
      .filter(fieldItem -> fieldItem.getAuthorityId() != null)
      .collect(Collectors.toList());
    assertThat(linkedFields)
      .hasSize(expectedLinkedFieldsCount);

    for (int i = 0; i < linkedFields.size(); i++) {
      var linkedField = linkedFields.get(i);
      assertThat(linkedField.getAuthorityId())
        .isEqualTo(links.get(i).getAuthorityId());
      assertThat(linkedField.getAuthorityNaturalId())
        .isEqualTo(links.get(i).getAuthorityNaturalId());
      assertThat(linkedField.getAuthorityControlledSubfields())
        .isEqualTo(links.get(i).getBibRecordSubfields());
    }
  }


  public static Stream<Arguments> updateLinksTestData() {
    return Stream.of(
      arguments(List.of(
        getFieldItem(),
        getFieldItemLinked()
      ), 1),
      arguments(Collections.emptyList(), 0)
    );
  }

  @ParameterizedTest
  @MethodSource("updateLinksTestData")
  void testRecordLinksUpdated(List<FieldItem> fieldsMock, Integer expectedLinkUpdates) {
    var quickMarcMock = getQuickMarc(fieldsMock);

    service.updateRecordLinks(quickMarcMock);

    var expectedLinks = fieldsMock.stream()
      .filter(fieldItem -> fieldItem.getAuthorityId() != null)
      .map(fieldItem -> new InstanceLink()
        .setInstanceId(quickMarcMock.getExternalId())
        .setAuthorityId(fieldItem.getAuthorityId())
        .setAuthorityNaturalId(fieldItem.getAuthorityNaturalId())
        .setBibRecordTag(fieldItem.getTag())
        .setBibRecordSubfields(fieldItem.getAuthorityControlledSubfields())
        .setLinkingRuleId(fieldItem.getLinkingRuleId()))
      .collect(Collectors.toList());
    var expectedInstanceLinks = new InstanceLinks(expectedLinks, expectedLinks.size());

    assertThat(expectedLinks).hasSize(expectedLinkUpdates);
    verify(linksClient).putLinksByInstanceId(quickMarcMock.getExternalId(), expectedInstanceLinks);
  }

  @ParameterizedTest
  @EnumSource(value = MarcFormat.class, names = "BIBLIOGRAPHIC", mode = EnumSource.Mode.EXCLUDE)
  void testRecordLinksSetNotBib(MarcFormat marcFormat) {
    var quickMarc = new QuickMarc().marcFormat(marcFormat);
    service.setRecordLinks(quickMarc);
    verifyNoInteractions(linksClient);
  }

  @ParameterizedTest
  @EnumSource(value = MarcFormat.class, names = "BIBLIOGRAPHIC", mode = EnumSource.Mode.EXCLUDE)
  void testRecordLinksUpdateNotBib(MarcFormat marcFormat) {
    var quickMarc = new QuickMarc().marcFormat(marcFormat);
    service.updateRecordLinks(quickMarc);
    verifyNoInteractions(linksClient);
  }

  private static FieldItem getFieldItem() {
    return new FieldItem().tag("650").indicators(List.of("\\", "\\")).content("$a bcdefghijklmn");
  }

  private static FieldItem getFieldItem(String authorityId) {
    return getFieldItem().authorityId(UUID.fromString(authorityId));
  }

  private static FieldItem getFieldItemLinked() {
    return getFieldItem(AUTHORITY_ID).authorityNaturalId("12345")
      .authorityControlledSubfields(List.of("a", "b", "c")).linkingRuleId(LINKING_RULE_ID);
  }

  private static InstanceLink getInstanceLink(UUID authorityId, List<String> subfields) {
    return new InstanceLink(RandomUtils.nextInt(),
      authorityId,
      "12345",
      UUID.fromString("b9a5f035-de63-4e2c-92c2-07240c89b817"),
      "650",
      subfields,
      LINKING_RULE_ID);
  }

  private QuickMarc getQuickMarc(List<FieldItem> fieldItems) {
    return new QuickMarc().marcFormat(MarcFormat.BIBLIOGRAPHIC)
      .externalId(UUID.randomUUID())
      .fields(fieldItems);
  }

}
