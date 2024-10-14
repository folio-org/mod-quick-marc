package org.folio.qm.service.population;

import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;

public abstract class FieldItemMarcPopulationService implements MarcPopulationService {

  @Override
  public void populate(BaseMarcRecord qmRecord) {
    var fields = qmRecord.getFields();
    var format = qmRecord.getMarcFormat();

    fields.stream()
      .filter(this::canProcess)
      .forEach(fieldItem -> populateValues(fieldItem, format));
  }

  protected abstract boolean canProcess(FieldItem field);

  protected abstract void populateValues(FieldItem fieldItem, MarcFormat marcFormat);
}
