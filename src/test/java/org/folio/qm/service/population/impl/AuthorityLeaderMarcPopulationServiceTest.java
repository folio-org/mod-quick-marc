package org.folio.qm.service.population.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.support.types.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class AuthorityLeaderMarcPopulationServiceTest {

  private static final String VALID_LEADER = "06059cz\\\\\\2201201n\\\\4500";
  private static final String WRONG_AUTHORITY_RECORD_TYPE = "06059ca\\\\\\2201201n\\\\4500";
  private static final String WRONG_AUTHORITY_CODING_SCHEME = "06059cz\\\\02201201n\\\\4500";
  private static final String WRONG_AUTHORITY_PUNCTUATION_POLICY = "06059cz\\\\\\2201201na\\4500";
  private static final String WRONG_UNDEFINED_CHARACTER_POSITION_7 = "06059cza\\\\2201201n\\\\4500";
  private static final String WRONG_UNDEFINED_CHARACTER_POSITION_8 = "06059cz\\a\\2201201n\\\\4500";
  private static final String WRONG_UNDEFINED_CHARACTER_POSITION_19 = "06059cz\\\\\\2201201n\\a4500";

  private final AuthorityLeaderMarcPopulationService populationService = new AuthorityLeaderMarcPopulationService();

  @Test
  void shouldSupportAuthorityFormat() {
    assertTrue(populationService.supportFormat(MarcFormat.AUTHORITY));
  }

  @Test
  void shouldNotSupportBibliographicFormat() {
    assertFalse(populationService.supportFormat(MarcFormat.BIBLIOGRAPHIC));
  }

  @Test
  void shouldNotSupportHoldingsFormat() {
    assertFalse(populationService.supportFormat(MarcFormat.HOLDINGS));
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnAuthorityRecordType() {
    var quickMarc = getQuickMarc(WRONG_AUTHORITY_RECORD_TYPE);
    populationService.populate(quickMarc);
    assertEquals(VALID_LEADER, quickMarc.getLeader());
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnAuthorityCodingScheme() {
    var quickMarc = getQuickMarc(WRONG_AUTHORITY_CODING_SCHEME);
    populationService.populate(quickMarc);
    assertEquals(VALID_LEADER, quickMarc.getLeader());
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnAuthorityPunctuationPolicy() {
    var quickMarc = getQuickMarc(WRONG_AUTHORITY_PUNCTUATION_POLICY);
    populationService.populate(quickMarc);
    assertEquals(VALID_LEADER, quickMarc.getLeader());
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnAuthorityUndefinedCharacterPosition7() {
    var quickMarc = getQuickMarc(WRONG_UNDEFINED_CHARACTER_POSITION_7);
    populationService.populate(quickMarc);
    assertEquals(VALID_LEADER, quickMarc.getLeader());
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnAuthorityUndefinedCharacterPosition8() {
    var quickMarc = getQuickMarc(WRONG_UNDEFINED_CHARACTER_POSITION_8);
    populationService.populate(quickMarc);
    assertEquals(VALID_LEADER, quickMarc.getLeader());
  }

  @Test
  void shouldSetDefaultValueForInvalidValueOnAuthorityUndefinedCharacterPosition19() {
    var quickMarc = getQuickMarc(WRONG_UNDEFINED_CHARACTER_POSITION_19);
    populationService.populate(quickMarc);
    assertEquals(VALID_LEADER, quickMarc.getLeader());
  }

  private BaseMarcRecord getQuickMarc(String leader) {
    return new BaseMarcRecord()
      .leader(leader);
  }
}
