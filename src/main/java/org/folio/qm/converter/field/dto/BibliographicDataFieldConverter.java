package org.folio.qm.converter.field.dto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.qm.client.LinksClient;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.util.MarcUtils;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BibliographicDataFieldConverter extends CommonDataFieldConverter {
  private static final char AUTHORITY_ID_SUBFIELD_CODE = '9';
  private static final char AUTHORITY_NATURAL_ID_SUBFIELD_CODE = '0';
  private final LinksClient linksClient;

  @Override
  public FieldItem convert(DataField field, Leader leader) {
    var fieldItem = super.convert(field, leader);
    extractAuthorityId(field.getSubfields()).ifPresent(fieldItem::setAuthorityId);
    setLinkingRuleId(fieldItem, field);
    return fieldItem;
  }

  private void setLinkingRuleId(FieldItem fieldItem, DataField dataField) {
    var subfields = dataField.getSubfields();
    if (subfields.stream()
      .anyMatch(subfield -> subfield.getCode() == AUTHORITY_ID_SUBFIELD_CODE)
      && subfields.stream()
      .anyMatch(subfield -> subfield.getCode() == AUTHORITY_NATURAL_ID_SUBFIELD_CODE)) {

      var linkingRules = linksClient.fetchLinkingRules();
      linkingRules.stream()
        .filter(l -> l.getBibField().equals(dataField.getTag()))
        .findFirst()
        .ifPresent(l -> fieldItem.setLinkingRuleId(l.getId()));
    }
  }

  @Override
  public boolean canProcess(VariableField field, MarcFormat marcFormat) {
    return field instanceof DataField && marcFormat == MarcFormat.BIBLIOGRAPHIC;
  }

  private Optional<UUID> extractAuthorityId(List<Subfield> subfields) {
    return subfields.stream()
      .filter(subfield -> subfield.getCode() == AUTHORITY_ID_SUBFIELD_CODE)
      .filter(subfield -> MarcUtils.isValidUuid(subfield.getData()))
      .map(subfield -> UUID.fromString(subfield.getData()))
      .findFirst();
  }
}
