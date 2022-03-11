package org.folio.qm.validation.impl.bibliographic;

import static org.folio.qm.converter.elements.LeaderItem.BIBLIOGRAPHIC_LEVEL;
import static org.folio.qm.converter.elements.LeaderItem.BIB_RECORD_STATUS;
import static org.folio.qm.converter.elements.LeaderItem.BIB_RECORD_TYPE;
import static org.folio.qm.converter.elements.LeaderItem.CATALOGING_FORM;
import static org.folio.qm.converter.elements.LeaderItem.CONTROL_TYPE;
import static org.folio.qm.converter.elements.LeaderItem.RESOURCE_RECORD_LEVEL;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import org.folio.qm.converter.elements.LeaderItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.validation.LeaderValidationRule;
import org.folio.qm.validation.ValidationError;

@Component
public class BibliographicLeaderValidationRule extends LeaderValidationRule {

  private static final List<LeaderItem> BIBLIOGRAPHIC_LEADER_ITEMS = List.of(BIB_RECORD_STATUS, BIB_RECORD_TYPE, BIBLIOGRAPHIC_LEVEL,
    CONTROL_TYPE, CATALOGING_FORM, RESOURCE_RECORD_LEVEL);

  @Override
  public boolean supportFormat(MarcFormat marcFormat) {
    return MarcFormat.BIBLIOGRAPHIC == marcFormat;
  }

  @Override
  public Optional<ValidationError> validate(String leader) {
    return commonLeaderValidation(leader, BIBLIOGRAPHIC_LEADER_ITEMS);
  }
}

