package org.folio.config;

import org.folio.converter.ParsedRecordToQuickMarcConverter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = ParsedRecordToQuickMarcConverter.class)
public class ApplicationConfig {
}
