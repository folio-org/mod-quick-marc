package org.folio.qm.converter.field.dto;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.folio.qm.converter.elements.Constants.BLANK_REPLACEMENT;
import static org.folio.qm.converter.elements.Constants.SPACE_CHARACTER;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.folio.qm.converter.field.VariableFieldConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.util.MarcUtils;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.springframework.stereotype.Component;

@Component
public class CommonDataFieldConverter implements VariableFieldConverter<DataField> {

  private static final char AUTHORITY_ID_SUBFIELD_CODE = '9';

  @Override
  public FieldItem convert(DataField field, Leader leader) {
    var fieldItem = new FieldItem().tag(field.getTag())
      .addIndicatorsItem(convertIndicator(field.getIndicator1()))
      .addIndicatorsItem(convertIndicator(field.getIndicator2()))
      .content(convertSubfields(field.getSubfields()));

    extractAuthorityId(field.getSubfields()).ifPresent(fieldItem::setAuthorityId);

    return fieldItem;
  }

  @Override
  public boolean canProcess(VariableField field, MarcFormat marcFormat) {
    return field instanceof DataField;
  }

  private String convertSubfields(List<Subfield> subfields) {
    return subfields.stream().map(this::convertSubfield).collect(Collectors.joining(SPACE));
  }

  private String convertSubfield(Subfield subfield) {
    return "$" + subfield.getCode() + SPACE + subfield.getData();
  }

  private String convertIndicator(char ind) {
    return ind == SPACE_CHARACTER ? BLANK_REPLACEMENT : Character.toString(ind);
  }

  private Optional<UUID> extractAuthorityId(List<Subfield> subfields) {
    return subfields.stream()
      .filter(subfield -> subfield.getCode() == AUTHORITY_ID_SUBFIELD_CODE)
      .filter(subfield -> MarcUtils.isValidUuid(subfield.getData()))
      .map(subfield -> UUID.fromString(subfield.getData()))
      .findFirst();
  }
}
