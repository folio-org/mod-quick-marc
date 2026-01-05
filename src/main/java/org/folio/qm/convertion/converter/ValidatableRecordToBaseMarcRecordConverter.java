package org.folio.qm.convertion.converter;

import java.util.ArrayList;
import java.util.List;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ValidatableRecord;
import org.folio.qm.domain.model.BaseQuickMarcRecord;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ValidatableRecordToBaseMarcRecordConverter implements Converter<ValidatableRecord, BaseQuickMarcRecord> {

  @Override
  @SuppressWarnings("checkstyle:methodLength")
  public BaseQuickMarcRecord convert(ValidatableRecord validatableRecord) {
    var fields = validatableRecord.getFields().stream()
      .map(field -> new FieldItem()
        .tag(field.getTag())
        .content(field.getContent())
        .indicators(field.getIndicators()))
      .toList();

    var baseMarcRecord = new BaseQuickMarcRecord() {

      private @Nullable MarcFormat marcFormat;
      private @Nullable List<FieldItem> fields;
      private @Nullable String leader;

      @Override
      public @Nullable String getLeader() {
        return this.leader;
      }

      @Override
      public void setLeader(@Nullable String leader) {
        this.leader = leader;
      }

      @Override
      public BaseQuickMarcRecord fields(@Nullable List<FieldItem> fields) {
        this.fields = fields;
        return this;
      }

      @Override
      public BaseQuickMarcRecord addFieldsItem(FieldItem fieldsItem) {
        if (fields == null) {
          fields = new ArrayList<>();
        }
        fields.add(fieldsItem);
        return this;
      }

      @Override
      public @Nullable List<FieldItem> getFields() {
        return this.fields;
      }

      @Override
      public void setFields(@Nullable List<FieldItem> fields) {
        this.fields = fields;
      }

      @Override
      public BaseQuickMarcRecord suppressDiscovery(Boolean suppressDiscovery) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Boolean getSuppressDiscovery() {
        throw new UnsupportedOperationException();
      }

      @Override
      public void setSuppressDiscovery(Boolean suppressDiscovery) {
        throw new UnsupportedOperationException();
      }

      @Override
      public BaseQuickMarcRecord marcFormat(MarcFormat marcFormat) {
        this.marcFormat = marcFormat;
        return this;
      }

      @Override
      public @Nullable MarcFormat getMarcFormat() {
        return marcFormat;
      }

      @Override
      public void setMarcFormat(@Nullable MarcFormat marcFormat) {
        this.marcFormat = marcFormat;
      }

      @Override
      public BaseQuickMarcRecord sourceVersion(Integer sourceVersion) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Integer getSourceVersion() {
        throw new UnsupportedOperationException();
      }

      @Override
      public void setSourceVersion(Integer sourceVersion) {
        throw new UnsupportedOperationException();
      }
    };

    baseMarcRecord.setLeader(validatableRecord.getLeader());
    baseMarcRecord.setMarcFormat(validatableRecord.getMarcFormat());
    baseMarcRecord.setFields(fields);
    return baseMarcRecord;
  }
}
