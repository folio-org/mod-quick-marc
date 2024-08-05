package org.folio.qm.converter;

import java.util.List;
import org.folio.qm.converter.field.FieldItemConverter;
import org.folio.qm.converter.field.VariableFieldConverter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class MarcFieldsHardConverter extends MarcFieldsConverter {
  public MarcFieldsHardConverter(List<FieldItemConverter> fieldItemConverters,
                                 List<VariableFieldConverter<DataField>> dataFieldConverters,
                                 List<VariableFieldConverter<ControlField>> controlFieldConverters) {
    super(false, fieldItemConverters, dataFieldConverters, controlFieldConverters);
  }
}
