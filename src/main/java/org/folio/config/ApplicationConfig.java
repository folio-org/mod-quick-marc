package org.folio.config;

import org.folio.client.HttpClient;
import org.folio.client.OkapiHttpClient;
import org.folio.converter.ParsedRecordDtoToQuickMarcConverter;
import org.folio.converter.QuickMarcToParsedRecordDtoConverter;
import org.folio.rest.jaxrs.model.QuickMarcJson;
import org.folio.service.ChangeManagerService;
import org.folio.service.MarcRecordsService;
import org.folio.srs.model.ParsedRecordDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class ApplicationConfig {
  @Bean
  public HttpClient httpClient() {
    return OkapiHttpClient.getInstance();
  }
  @Bean
  public MarcRecordsService marcRecordsService() {
    return new ChangeManagerService();
  }
  @Bean
  public Converter<ParsedRecordDto, QuickMarcJson> parsedRecordToQuickMarcConverter() {
    return new ParsedRecordDtoToQuickMarcConverter();
  }
  @Bean
  Converter<QuickMarcJson, ParsedRecordDto> quickMarcToParsedRecordConverter() {
    return new QuickMarcToParsedRecordDtoConverter();
  }
}
