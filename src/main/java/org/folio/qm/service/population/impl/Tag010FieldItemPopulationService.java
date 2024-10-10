package org.folio.qm.service.population.impl;

import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.service.population.FieldItemMarcPopulationService;
import org.springframework.stereotype.Service;

import static org.folio.qm.converter.elements.Constants.LCCN_CONTROL_FIELD;

@Service
public class Tag010FieldItemPopulationService extends FieldItemMarcPopulationService {

  @Override
  protected boolean canProcess(FieldItem field, MarcFormat marcFormat) {
    return field.getTag().equals(LCCN_CONTROL_FIELD);
  }

  @Override
  protected void populateValues(FieldItem fieldItem, MarcFormat marcFormat) {
    var content = fieldItem.getContent().toString();
  }

  @Override
  public boolean supportFormat(MarcFormat marcFormat) {
    return marcFormat == MarcFormat.BIBLIOGRAPHIC || marcFormat == MarcFormat.AUTHORITY;
  }
}
