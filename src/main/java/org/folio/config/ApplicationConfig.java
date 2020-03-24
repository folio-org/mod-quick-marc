package org.folio.config;

import org.folio.converter.RecordToQuickMarcConverter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = RecordToQuickMarcConverter.class)
public class ApplicationConfig {
}
