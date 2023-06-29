package org.folio.qm.service.impl;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.LINK_ERROR_CAUSE;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.LINK_STATUS_ERROR;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.folio.qm.client.LinkingRulesClient;
import org.folio.qm.client.LinksClient;
import org.folio.qm.client.LinksClient.InstanceLink;
import org.folio.qm.client.LinksClient.InstanceLinks;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.LinkDetails;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.exception.ValidationException;
import org.folio.qm.support.types.UnitTest;
import org.folio.tenant.domain.dto.Error;
import org.folio.tenant.domain.dto.Errors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@UnitTest
@ExtendWith(MockitoExtension.class)
class LinksServiceImplTest {

  private static final String AUTHORITY_ID = "b9a5f035-de63-4e2c-92c2-07240c88b817";
  private static final int LINKING_RULE_ID = 1;
  private static final String BIB_TAG = "650";
  private static final String LINK_STATUS_ACTUAL = "ACTUAL";

  @Mock
  private LinksClient linksClient;

  @Mock
  private LinkingRulesServiceImpl rulesService;

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
          getInstanceLink(UUID.fromString(AUTHORITY_ID))),
        List.of(
          getFieldItem(AUTHORITY_ID)),
        1
      ),
      arguments(
        List.of(
          getInstanceLink(UUID.fromString(AUTHORITY_ID)),
          getInstanceLink(UUID.fromString("81404611-aab2-4e24-806a-c1e9b8eb3ad1"))),
        List.of(
          getFieldItem(),
          getFieldItem(AUTHORITY_ID),
          getFieldItem("81404611-aab2-4e24-806a-c1e9b8eb3ad1")),
        2
      ),
      arguments(
        List.of(
          getInstanceLink(UUID.fromString(AUTHORITY_ID))),
        List.of(
          getFieldItem(),
          getFieldItem()),
        0
      ),
      arguments(
        List.of(
          getInstanceLink(UUID.fromString(AUTHORITY_ID), LINK_STATUS_ERROR, LINK_ERROR_CAUSE)),
        List.of(
          getFieldItem(AUTHORITY_ID)),
        1
      )
    );
  }

  public static Stream<Arguments> updateLinksTestData() {
    return Stream.of(
      arguments(List.of(
        getFieldItem(),
        getFieldItemLinked()
      ), 1),
      arguments(singletonList(
        getFieldItemLinkedWithError()
      ), 1),
      arguments(Collections.emptyList(), 0)
    );
  }

  private static FieldItem getFieldItem() {
    return new FieldItem().tag(BIB_TAG).indicators(List.of("\\", "\\")).content("$a bcdefghijklmn");
  }

  private static FieldItem getFieldItem(String authorityId) {
    return getFieldItem()
      .linkDetails(new LinkDetails().authorityId(UUID.fromString(authorityId)).authorityNaturalId("12345"));
  }

  private static FieldItem getFieldItemLinked() {
    var fieldItem = getFieldItem(AUTHORITY_ID);
    fieldItem.getLinkDetails().linkingRuleId(LINKING_RULE_ID);
    return fieldItem;
  }

  private static FieldItem getFieldItemLinkedWithError() {
    var fieldItem = getFieldItem(AUTHORITY_ID);
    fieldItem.getLinkDetails().linkingRuleId(LINKING_RULE_ID).status(LINK_STATUS_ERROR).errorCause(LINK_ERROR_CAUSE);
    return fieldItem;
  }

  private static InstanceLink getInstanceLink(UUID authorityId, String status, String errorCause) {
    return new InstanceLink(RandomUtils.nextInt(),
      authorityId,
      "12345",
      UUID.fromString("b9a5f035-de63-4e2c-92c2-07240c89b817"),
      LINKING_RULE_ID,
      status,
      errorCause);
  }

  private static InstanceLink getInstanceLink(UUID authorityId) {
    return getInstanceLink(authorityId, LINK_STATUS_ACTUAL, EMPTY);
  }

  @ParameterizedTest
  @MethodSource("setLinksTestData")
  void testRecordLinksSet(List<InstanceLink> links, List<FieldItem> fieldItemsMock, Integer expectedLinkedFieldsCount) {
    var instanceLinks = new InstanceLinks(links, LINKING_RULE_ID);
    var linkingRules = singletonList(new LinkingRulesClient.LinkingRuleDto()
      .setId(LINKING_RULE_ID)
      .setBibField(BIB_TAG));

    when(linksClient.fetchLinksByInstanceId(any())).thenReturn(Optional.of(instanceLinks));
    when(rulesService.getLinkingRules()).thenReturn(linkingRules);

    var record = getQuickMarcView(fieldItemsMock);
    service.setRecordLinks(record);

    var linkedFields = record.getFields().stream()
      .filter(fieldItem -> fieldItem.getLinkDetails() != null)
      .collect(Collectors.toList());
    assertThat(linkedFields)
      .hasSize(expectedLinkedFieldsCount);

    for (int i = 0; i < linkedFields.size(); i++) {
      var linkDetails = linkedFields.get(i).getLinkDetails();
      assertThat(linkDetails.getAuthorityId())
        .isEqualTo(links.get(i).getAuthorityId());
      assertThat(linkDetails.getAuthorityNaturalId())
        .isEqualTo(links.get(i).getAuthorityNaturalId());
      assertThat(linkDetails.getLinkingRuleId())
        .isEqualTo(links.get(i).getLinkingRuleId());
      assertThat(linkDetails.getStatus())
        .isEqualTo(links.get(i).getStatus());
      assertThat(linkDetails.getErrorCause())
        .isEqualTo(links.get(i).getErrorCause());
    }
  }

  @ParameterizedTest
  @MethodSource("updateLinksTestData")
  void testRecordLinksUpdated(List<FieldItem> fieldsMock, Integer expectedLinkUpdates) {
    when(linksClient.putLinksByInstanceId(any(), any())).thenReturn(ResponseEntity.ok(new Errors()));
    var quickMarcMock = getQuickMarcEdit(fieldsMock);

    service.updateRecordLinks(quickMarcMock);

    var expectedLinks = fieldsMock.stream()
      .filter(fieldItem -> fieldItem.getLinkDetails() != null)
      .map(fieldItem -> new InstanceLink()
        .setInstanceId(quickMarcMock.getExternalId())
        .setAuthorityId(fieldItem.getLinkDetails().getAuthorityId())
        .setAuthorityNaturalId(fieldItem.getLinkDetails().getAuthorityNaturalId())
        .setLinkingRuleId(fieldItem.getLinkDetails().getLinkingRuleId()))
      .collect(Collectors.toList());
    var expectedInstanceLinks = new InstanceLinks(expectedLinks, expectedLinks.size());

    assertThat(expectedLinks).hasSize(expectedLinkUpdates);
    verify(linksClient).putLinksByInstanceId(quickMarcMock.getExternalId(), expectedInstanceLinks);
  }

  @Test
  void testRecordLinksUpdatedWithErrors() {
    var errorMessage = new Error("Authority was deleted");
    var errors = new Errors().addErrorsItem(errorMessage);
    when(linksClient.putLinksByInstanceId(any(), any())).thenReturn(ResponseEntity.badRequest().body(errors));

    var quickMarcMock = getQuickMarcEdit();

    var exception = assertThrows(ValidationException.class, () -> service.updateRecordLinks(quickMarcMock));
    assertTrue(exception.getError().getMessage().contains(errorMessage.getMessage()));
  }

  @ParameterizedTest
  @EnumSource(value = MarcFormat.class, names = "BIBLIOGRAPHIC", mode = EnumSource.Mode.EXCLUDE)
  void testRecordLinksSetNotBib(MarcFormat marcFormat) {
    var quickMarc = new QuickMarcView().marcFormat(marcFormat);
    service.setRecordLinks(quickMarc);
    verifyNoInteractions(linksClient);
  }

  @ParameterizedTest
  @EnumSource(value = MarcFormat.class, names = "BIBLIOGRAPHIC", mode = EnumSource.Mode.EXCLUDE)
  void testRecordLinksUpdateNotBib(MarcFormat marcFormat) {
    var quickMarc = new QuickMarcEdit().marcFormat(marcFormat);
    service.updateRecordLinks(quickMarc);
    verifyNoInteractions(linksClient);
  }

  private QuickMarcView getQuickMarcView(List<FieldItem> fieldItems) {
    return new QuickMarcView().marcFormat(MarcFormat.BIBLIOGRAPHIC)
      .externalId(UUID.randomUUID())
      .fields(fieldItems);
  }

  private QuickMarcEdit getQuickMarcEdit(List<FieldItem> fieldItems) {
    return new QuickMarcEdit().marcFormat(MarcFormat.BIBLIOGRAPHIC)
      .externalId(UUID.randomUUID())
      .fields(fieldItems);
  }

  private QuickMarcEdit getQuickMarcEdit() {
    return new QuickMarcEdit().marcFormat(MarcFormat.BIBLIOGRAPHIC)
      .externalId(UUID.randomUUID());
  }
}
