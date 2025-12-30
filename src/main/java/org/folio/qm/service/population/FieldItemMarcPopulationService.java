package org.folio.qm.service.population;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.model.BaseQuickMarcRecord;

@Log4j2
public abstract class FieldItemMarcPopulationService implements MarcPopulationService {

  @Override
  public void populate(BaseQuickMarcRecord qmRecord) {
    log.trace("populate:: Populating field items for format: {}", qmRecord.getMarcFormat());
    var fields = qmRecord.getFields();
    var format = qmRecord.getMarcFormat();

    var processedCount = new AtomicInteger();
    fields.stream()
      .filter(this::canProcess)
      .forEach(fieldItem -> {
        populateValues(fieldItem, format);
        processedCount.getAndIncrement();
      });

    log.trace("populate:: Processed {} field items", processedCount.get());
  }

  protected abstract boolean canProcess(FieldItem field);

  protected abstract void populateValues(FieldItem fieldItem, MarcFormat marcFormat);
}
