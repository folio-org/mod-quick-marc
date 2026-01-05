package org.folio.qm.config;

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
}
