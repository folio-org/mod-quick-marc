package org.folio.qm.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import org.folio.MarcFieldProtectionSettingsCollection;
import org.folio.qm.client.FieldProtectionSettingsClient;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.service.storage.source.FieldProtectionSetterServiceImpl;
import org.folio.rest.jaxrs.model.MarcFieldProtectionSetting;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class FieldProtectionSetterServiceImplTest {

  private static final String ANY = "*";
  private static final String EMPTY = "";

  @Mock
  private FieldProtectionSettingsClient protectionSettingsClient;

  @InjectMocks
  private FieldProtectionSetterServiceImpl service;

  @ParameterizedTest
  @MethodSource("testData")
  void testFieldProtectionSettingsSet(MarcFieldProtectionSetting setting, FieldItem fieldItem) {
    var settingsCollection = new MarcFieldProtectionSettingsCollection();
    settingsCollection.setMarcFieldProtectionSettings(List.of(setting));
    when(protectionSettingsClient.getFieldProtectionSettings()).thenReturn(settingsCollection);

    var marcView = new QuickMarcView().fields(List.of(fieldItem));
    service.applyFieldProtection(marcView);

    assertThat(marcView.getFields())
      .extracting("tag", "isProtected")
      .contains(tuple(setting.getField(), true));
  }

  @Test
  void testNonProtectedFieldProtectionSettingsSet() {
    var fieldItem = createFieldItem("245", List.of("\\", "\\"), "$a test");
    var settingsCollection = new MarcFieldProtectionSettingsCollection();
    var setting = createMarcFieldProtectionSetting("245", ANY, ANY, "b", ANY);
    settingsCollection.setMarcFieldProtectionSettings(List.of(setting));
    when(protectionSettingsClient.getFieldProtectionSettings()).thenReturn(settingsCollection);

    var marcView = new QuickMarcView().fields(List.of(fieldItem));
    service.applyFieldProtection(marcView);

    assertThat(marcView.getFields())
      .extracting("tag", "isProtected")
      .contains(tuple(setting.getField(), false));
  }

  @SuppressWarnings("checkstyle:MethodLength")
  private static Stream<Arguments> testData() {
    return Stream.of(
      arguments(
        createMarcFieldProtectionSetting("001", EMPTY, EMPTY, EMPTY, ANY),
        createFieldItem("001", List.of(), "test")
      ),
      arguments(
        createMarcFieldProtectionSetting("999", "f", "f", ANY, ANY),
        createFieldItem("999", List.of("f", "f"), "test")
      ),
      arguments(
        createMarcFieldProtectionSetting("010", EMPTY, EMPTY, ANY, "bcdefghijklmn"),
        createFieldItem("010", List.of("\\", "\\"), "$a bcdefghijklmn")
      ),
      arguments(
        createMarcFieldProtectionSetting("010", ANY, ANY, "a", ANY),
        createFieldItem("010", List.of("1", "a"), "$a test")
      ),
      arguments(
        createMarcFieldProtectionSetting("035", ANY, ANY, "a", "(OCoLC)123"),
        createFieldItem("035", List.of("\\", "a"), "$a (OCoLC)123")
      ),
      arguments(
        createMarcFieldProtectionSetting("040", ANY, ANY, "c", "UPB"),
        new FieldItem().tag("040").content("$c UPB")
      ),
      arguments(
        createMarcFieldProtectionSetting("300", ANY, "2", ANY, "v."),
        createFieldItem("300", List.of("1", "2"), "$a test $1 v.eqq")
      ),
      arguments(
        createMarcFieldProtectionSetting("505", ANY, ANY, "a", "Purchase price: $325.00, 1980 August."),
        createFieldItem("505", List.of(), "$a Purchase price: $325.00, 1980 August. $b test")
      )
    );
  }

  private static FieldItem createFieldItem(String tag, List<String> indicators, String content) {
    return new FieldItem().tag(tag).indicators(indicators).content(content);
  }

  private static MarcFieldProtectionSetting createMarcFieldProtectionSetting(String tag, String i1, String i2,
                                                                             String subfield, String data) {
    return new MarcFieldProtectionSetting()
      .withField(tag)
      .withSubfield(subfield)
      .withIndicator1(i1)
      .withIndicator2(i2)
      .withData(data);
  }
}
