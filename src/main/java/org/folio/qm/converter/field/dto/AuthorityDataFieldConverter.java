package org.folio.qm.converter.field.dto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.util.MarcUtils;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.springframework.stereotype.Component;

@Component
public class AuthorityDataFieldConverter extends CommonDataFieldConverter {
  private static final char AUTHORITY_ID_SUBFIELD_CODE = '9';

  @Override
  public FieldItem convert(DataField field, Leader leader) {
    var fieldItem = super.convert(field, leader);
    extractAuthorityId(field.getSubfields()).ifPresent(fieldItem::setAuthorityId);
    return fieldItem;
  }

  @Override
  public boolean canProcess(VariableField field, MarcFormat marcFormat) {
    return super.canProcess(field, marcFormat) && marcFormat == MarcFormat.AUTHORITY;
  }

  private Optional<UUID> extractAuthorityId(List<Subfield> subfields) {
    return subfields.stream()
      .filter(subfield -> subfield.getCode() == AUTHORITY_ID_SUBFIELD_CODE)
      .filter(subfield -> MarcUtils.isValidUuid(subfield.getData()))
      .map(subfield -> UUID.fromString(subfield.getData()))
      .findFirst();
  }
}
