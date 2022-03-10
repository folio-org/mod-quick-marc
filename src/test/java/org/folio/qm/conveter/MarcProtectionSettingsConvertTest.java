package org.folio.qm.conveter;

import static org.folio.qm.domain.dto.ParsedRecordDto.RecordTypeEnum.BIB;
import static org.folio.qm.support.utils.JsonTestUtils.getMockAsObject;
import static org.folio.qm.support.utils.JsonTestUtils.getObjectAsJson;
import static org.folio.qm.support.utils.JsonTestUtils.readQuickMarc;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.FIELD_PROTECTION_SETTINGS_COLLECTION_FULL_PATH;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.PARSED_RECORDS_PROTECTION_SETTINGS;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.QM_PROTECTION_SETTINGS;
import static org.folio.qm.support.utils.testentities.TestEntitiesUtils.getParsedRecordDtoWithMinContent;

import java.time.ZoneOffset;
import java.util.Objects;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import org.folio.qm.converter.impl.MarcBibliographicDtoConverter;
import org.folio.qm.domain.dto.MarcFieldProtectionSettingsCollection;
import org.folio.qm.domain.dto.ParsedRecord;
import org.folio.qm.support.types.UnitTest;

@UnitTest
public class MarcProtectionSettingsConvertTest {
  private static final Logger logger = LogManager.getLogger(MarcProtectionSettingsConvertTest.class);

  @BeforeAll
  static void beforeAll() {
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
  }

  @Test
  void testFieldProtectionSettings() throws JSONException {
    logger.info("Testing Field Protection Settings");
    var parsedRecord = getMockAsObject(PARSED_RECORDS_PROTECTION_SETTINGS, ParsedRecord.class);
    var parsedRecordDto = getParsedRecordDtoWithMinContent(parsedRecord, BIB);
    MarcFieldProtectionSettingsCollection settingsCollection =
      getMockAsObject(FIELD_PROTECTION_SETTINGS_COLLECTION_FULL_PATH, MarcFieldProtectionSettingsCollection.class);
    var converter = new MarcBibliographicDtoConverter(settingsCollection);
    var actual = converter.convert(parsedRecordDto);

    var expected = readQuickMarc(QM_PROTECTION_SETTINGS);
    Objects.requireNonNull(expected).setRelatedRecordVersion(null);
    JSONAssert.assertEquals(getObjectAsJson(expected), getObjectAsJson(actual), true);
  }

}
