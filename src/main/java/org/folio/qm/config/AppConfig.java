package org.folio.qm.config;

import static org.folio.ActionProfile.FolioRecord.MARC_AUTHORITY;
import static org.folio.ActionProfile.FolioRecord.MARC_AUTHORITY_EXTENDED;

import org.folio.Authority;
import org.folio.processing.mapping.defaultmapper.RecordMapper;
import org.folio.processing.mapping.defaultmapper.RecordMapperBuilder;
import org.folio.qm.domain.model.BaseQuickMarcRecord;
import org.folio.rspec.i18n.TranslationProvider;
import org.folio.rspec.validation.SpecificationGuidedValidator;
import org.folio.rspec.validation.validator.marc.model.MarcRecord;
import org.folio.spring.i18n.config.TranslationConfiguration;
import org.folio.spring.i18n.service.TranslationService;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.impl.MarcFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.support.ResourcePatternResolver;

@Configuration
public class AppConfig {

  @Bean
  public MarcFactory marcFactory() {
    return new MarcFactoryImpl();
  }

  @Bean
  public TranslationProvider translationProvider(ResourcePatternResolver resourceResolver,
                                                 TranslationConfiguration configuration) {
    class TranslationProviderImpl extends TranslationService implements TranslationProvider {
      TranslationProviderImpl(ResourcePatternResolver resourceResolver,
                              TranslationConfiguration configuration) {
        super(resourceResolver, configuration);
      }
    }

    return new TranslationProviderImpl(resourceResolver, configuration);
  }

  @Bean("validatableRecordValidator")
  public SpecificationGuidedValidator validatableRecordValidator(TranslationProvider translationProvider,
                                                                 Converter<BaseQuickMarcRecord, MarcRecord> converter) {
    return new SpecificationGuidedValidator(translationProvider,
      source -> converter.convert((BaseQuickMarcRecord) source));
  }

  @Bean
  public RecordMapper<Authority> extendedRecordMapper() {
    return RecordMapperBuilder.buildMapper(MARC_AUTHORITY_EXTENDED.value());
  }

  @Bean
  public RecordMapper<Authority> simpleRecordMapper() {
    return RecordMapperBuilder.buildMapper(MARC_AUTHORITY.value());
  }
}
