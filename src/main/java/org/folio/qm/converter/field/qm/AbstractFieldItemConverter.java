package org.folio.qm.converter.field.qm;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.folio.qm.converter.elements.Constants.CONCAT_CONDITION_PATTERN;
import static org.folio.qm.converter.elements.Constants.SPLIT_PATTERN;
import static org.folio.qm.converter.elements.Constants.TOKEN_MIN_LENGTH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.folio.qm.converter.field.FieldItemConverter;
import org.folio.qm.domain.dto.FieldItem;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.DataFieldImpl;

public abstract class AbstractFieldItemConverter implements FieldItemConverter {

  @Override
  public VariableField convert(FieldItem field) {
    var dataField = new DataFieldImpl(field.getTag(), getIndicator(field, 0), getIndicator(field, 1));
    dataField.getSubfields().addAll(extractSubfields(field));
    return dataField;
  }

  private List<Subfield> extractSubfields(FieldItem field) {
    var tokens = Arrays.stream(SPLIT_PATTERN.split(field.getContent().toString()))
      .collect(Collectors.toCollection(LinkedList::new));

    List<Subfield> subfields = new ArrayList<>();
    while (!tokens.isEmpty()) {
      String token = tokens.pop();
      String subfieldString = token.concat(checkNextToken(tokens));
      if (subfieldString.length() < TOKEN_MIN_LENGTH) {
        throw new IllegalArgumentException("Subfield length");
      }
      subfields.add(subfieldFromString(subfieldString));
    }

    return subfields;
  }

  private String checkNextToken(LinkedList<String> tokens) {
    return !tokens.isEmpty() && CONCAT_CONDITION_PATTERN.matcher(tokens.peek()).matches()
      ? requireNonNull(tokens.poll()).concat(checkNextToken(tokens))
      : EMPTY;
  }

  protected abstract Subfield subfieldFromString(String string);
}
