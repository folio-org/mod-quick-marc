package org.folio.qm.converter;

import java.util.List;
import org.folio.qm.converter.field.FieldItemConverter;
import org.folio.qm.converter.field.VariableFieldConverter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.springframework.stereotype.Component;

@Component
public class MarcFieldsSoftConverter extends MarcFieldsConverter {
  public MarcFieldsSoftConverter(List<FieldItemConverter> fieldItemConverters,
                                 List<VariableFieldConverter<DataField>> dataFieldConverters,
                                 List<VariableFieldConverter<ControlField>> controlFieldConverters) {
    super(true, fieldItemConverters, dataFieldConverters, controlFieldConverters);
  }
}
