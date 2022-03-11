package org.folio.qm.converternew.dto;

import static org.apache.commons.lang3.StringUtils.SPACE;

import static org.folio.qm.converter.elements.Constants.BLANK_REPLACEMENT;
import static org.folio.qm.converter.elements.Constants.SPACE_CHARACTER;

import java.util.List;
import java.util.stream.Collectors;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import org.folio.qm.converternew.VariableFieldConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;

public class CommonDataFieldConverter implements VariableFieldConverter<DataField> {

  @Override
  public FieldItem convert(DataField field, Leader leader) {
    return new FieldItem().tag(field.getTag())
      .addIndicatorsItem(convertIndicator(field.getIndicator1()))
      .addIndicatorsItem(convertIndicator(field.getIndicator2()))
      .content(convertSubfields(field.getSubfields()));
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
}
