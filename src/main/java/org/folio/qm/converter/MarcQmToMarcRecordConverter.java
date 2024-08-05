package org.folio.qm.converter;

import static org.folio.qm.util.MarcUtils.restoreBlanks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.rspec.validation.validator.marc.model.MarcControlField;
import org.folio.rspec.validation.validator.marc.model.MarcDataField;
import org.folio.rspec.validation.validator.marc.model.MarcIndicator;
import org.folio.rspec.validation.validator.marc.model.MarcRecord;
import org.folio.rspec.validation.validator.marc.model.MarcSubfield;
import org.folio.rspec.validation.validator.marc.model.Reference;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Primary
@Component
@RequiredArgsConstructor
public class MarcQmToMarcRecordConverter implements Converter<BaseMarcRecord, MarcRecord> {

  @Qualifier("marcFieldsSoftConverter")
  private final MarcFieldsConverter fieldsConverter;

  @Override
  public MarcRecord convert(BaseMarcRecord source) {
    List<MarcControlField> controlFields = new ArrayList<>();
    List<MarcDataField> dataFields = new ArrayList<>();

    var fields = fieldsConverter.convertQmFields(source.getFields(), source.getMarcFormat()).stream()
      .collect(Collectors.groupingBy(VariableField::getTag));

    for (var entry : fields.entrySet()) {
      var fieldList = entry.getValue();
      for (int index = 0; index < fieldList.size(); index++) {
        var variableField = fieldList.get(index);
        if (variableField instanceof ControlField controlField) {
          controlFields.add(toMarcControlField(controlField, index));
        } else if (variableField instanceof DataField dataField) {
          dataFields.add(toMarcDataField(dataField, index));
        }
      }
    }

    controlFields.add(new MarcControlField(Reference.forTag("000"), restoreBlanks(source.getLeader())));
    return new MarcRecord(controlFields, dataFields);
  }

  private MarcControlField toMarcControlField(ControlField field, int tagIndex) {
    var reference = Reference.forTag(field.getTag(), tagIndex);
    return new MarcControlField(reference, field.getData());
  }

  private MarcDataField toMarcDataField(DataField field, int tagIndex) {
    var reference = Reference.forTag(field.getTag(), tagIndex);
    var indicators = convertIndicators(field, reference);
    var marcSubfields = convertSubfields(reference, field.getSubfields());

    return new MarcDataField(reference, indicators, marcSubfields);
  }

  private List<MarcSubfield> convertSubfields(Reference parentReference, List<Subfield> subfields) {
    var marcSubfields = new ArrayList<MarcSubfield>();
    subfields.stream()
      .collect(Collectors.groupingBy(Subfield::getCode))
      .forEach((code, subfieldList) -> subfieldList.forEach(subfield ->
        marcSubfields.add(toMarcSubfield(parentReference, subfieldList, subfield))));
    return marcSubfields;
  }

  private List<MarcIndicator> convertIndicators(DataField field, Reference reference) {
    return List.of(
      toMarcIndicator(reference, field.getIndicator1(), 1),
      toMarcIndicator(reference, field.getIndicator2(), 2));
  }

  private MarcIndicator toMarcIndicator(Reference fieldReference, char indicatorValue, int indicatorIndex) {
    var reference = Reference.forIndicator(fieldReference, indicatorIndex);
    return new MarcIndicator(reference, indicatorValue);
  }

  private MarcSubfield toMarcSubfield(Reference parentReference, List<Subfield> subfieldList, Subfield subfield) {
    var reference = Reference.forSubfield(parentReference, subfield.getCode(), subfieldList.indexOf(subfield));
    return new MarcSubfield(reference, subfield.getData());
  }
}
