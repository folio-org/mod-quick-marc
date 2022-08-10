package org.folio.qm.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.folio.qm.client.DICSFieldProtectionSettingsClient;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFieldProtectionSetting;
import org.folio.qm.domain.dto.MarcFieldProtectionSettingsCollection;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.support.types.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class FieldProtectionSetterServiceImplTest {

  @Mock
  private DICSFieldProtectionSettingsClient protectionSettingsClient;

  @InjectMocks
  private FieldProtectionSetterServiceImpl service;

  public static Stream<Arguments> testData() {
    return Stream.of(
      arguments(
        new MarcFieldProtectionSetting().field("001").indicator1("").indicator2("").subfield("").data("*"),
        new FieldItem().tag("001").content("test")
      ),
      arguments(
        new MarcFieldProtectionSetting().field("999").indicator1("f").indicator2("f").subfield("*").data("*"),
        new FieldItem().tag("999").indicators(List.of("f", "f")).content("test")
      ),
      arguments(
        new MarcFieldProtectionSetting().field("010").indicator1("").indicator2("").subfield("*").data(
          "bcdefghijklmn"),
        new FieldItem().tag("010").indicators(List.of("\\", "\\")).content("$a bcdefghijklmn")
      ),
      arguments(
        new MarcFieldProtectionSetting().field("010").indicator1("*").indicator2("*").subfield("a").data("*"),
        new FieldItem().tag("010").indicators(List.of("1", "a")).content("$a test")
      ),
      arguments(
        new MarcFieldProtectionSetting().field("035").indicator1("*").indicator2("*").subfield("a").data("(OCoLC)123"),
        new FieldItem().tag("035").indicators(List.of("\\", "a")).content("$a (OCoLC)123")
      ),
      arguments(
        new MarcFieldProtectionSetting().field("040").indicator1("*").indicator2("*").subfield("c").data("UPB"),
        new FieldItem().tag("040").content("$c UPB")
      ),
      arguments(
        new MarcFieldProtectionSetting().field("300").indicator1("*").indicator2("2").subfield("*").data("v."),
        new FieldItem().tag("300").indicators(List.of("1", "2")).content("$a test $1 v.eqq")
      ),
      arguments(
        new MarcFieldProtectionSetting().field("505").indicator1("*").indicator2("*").subfield("a").data("Purchase price: $325.00, 1980 August."),
        new FieldItem().tag("505").content("$a Purchase price: $325.00, 1980 August. $b test")
      )
    );
  }

  @ParameterizedTest
  @MethodSource("testData")
  void testFieldProtectionSettingsSet(MarcFieldProtectionSetting setting, FieldItem fieldItem) {
    var settingsCollection = new MarcFieldProtectionSettingsCollection();
    settingsCollection.addMarcFieldProtectionSettingsItem(setting);
    when(protectionSettingsClient.getFieldProtectionSettings()).thenReturn(settingsCollection);

    var record = new QuickMarc().fields(List.of(fieldItem));
    var actual = service.applyFieldProtection(record);

    assertThat(actual.getFields())
      .extracting("tag", "isProtected")
      .contains(tuple(setting.getField(), true));
  }

  public static Stream<Arguments> nonProtectedTestData() {
    return Stream.of(
      arguments(
        new MarcFieldProtectionSetting().field("245").indicator1("*").indicator2("*").subfield("b").data("*"),
        new FieldItem().tag("245").indicators(List.of("\\", "\\")).content("$a test")
      )
    );
  }

  @ParameterizedTest
  @MethodSource("nonProtectedTestData")
  void testNonProtectedFieldProtectionSettingsSet(MarcFieldProtectionSetting setting, FieldItem fieldItem) {
    var settingsCollection = new MarcFieldProtectionSettingsCollection();
    settingsCollection.addMarcFieldProtectionSettingsItem(setting);
    when(protectionSettingsClient.getFieldProtectionSettings()).thenReturn(settingsCollection);

    var record = new QuickMarc().fields(List.of(fieldItem));
    var actual = service.applyFieldProtection(record);

    assertThat(actual.getFields())
      .extracting("tag", "isProtected")
      .contains(tuple(setting.getField(), false));
  }

}
