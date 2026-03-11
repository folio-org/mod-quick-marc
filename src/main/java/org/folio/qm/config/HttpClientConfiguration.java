package org.folio.qm.config;

import org.folio.qm.client.AuthorityStorageClient;
import org.folio.qm.client.AuthorityTenantSettingsClient;
import org.folio.qm.client.FieldProtectionSettingsClient;
import org.folio.qm.client.HoldingsStorageClient;
import org.folio.qm.client.InstanceStorageClient;
import org.folio.qm.client.LinkingRulesClient;
import org.folio.qm.client.LinksClient;
import org.folio.qm.client.LinksSuggestionsClient;
import org.folio.qm.client.MappingMetadataClient;
import org.folio.qm.client.PrecedingSucceedingTitlesClient;
import org.folio.qm.client.SourceStorageClient;
import org.folio.qm.client.SpecificationStorageClient;
import org.folio.qm.client.UsersClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientConfiguration {

  @Bean
  public FieldProtectionSettingsClient fieldProtectionSettingsClient(HttpServiceProxyFactory factory) {
    return factory.createClient(FieldProtectionSettingsClient.class);
  }

  @Bean
  public LinkingRulesClient linkingRulesClient(HttpServiceProxyFactory factory) {
    return factory.createClient(LinkingRulesClient.class);
  }

  @Bean
  public LinksClient linksClient(HttpServiceProxyFactory factory) {
    return factory.createClient(LinksClient.class);
  }

  @Bean
  public LinksSuggestionsClient linksSuggestionsClient(HttpServiceProxyFactory factory) {
    return factory.createClient(LinksSuggestionsClient.class);
  }

  @Bean
  public SourceStorageClient sourceStorageClient(HttpServiceProxyFactory factory) {
    return factory.createClient(SourceStorageClient.class);
  }

  @Bean
  public SpecificationStorageClient specificationStorageClient(HttpServiceProxyFactory factory) {
    return factory.createClient(SpecificationStorageClient.class);
  }

  @Bean
  public UsersClient usersClient(HttpServiceProxyFactory factory) {
    return factory.createClient(UsersClient.class);
  }

  @Bean
  public AuthorityStorageClient authorityStorageClient(HttpServiceProxyFactory factory) {
    return factory.createClient(AuthorityStorageClient.class);
  }

  @Bean
  public MappingMetadataClient mappingMetadataClient(HttpServiceProxyFactory factory) {
    return factory.createClient(MappingMetadataClient.class);
  }

  @Bean
  public PrecedingSucceedingTitlesClient precedingSucceedingTitlesClient(HttpServiceProxyFactory factory) {
    return factory.createClient(PrecedingSucceedingTitlesClient.class);
  }

  @Bean
  public HoldingsStorageClient holdingsStorageClient(HttpServiceProxyFactory factory) {
    return factory.createClient(HoldingsStorageClient.class);
  }

  @Bean
  public InstanceStorageClient instanceStorageClient(HttpServiceProxyFactory factory) {
    return factory.createClient(InstanceStorageClient.class);
  }

  @Bean
  public AuthorityTenantSettingsClient authorityTenantSettingsClient(HttpServiceProxyFactory factory) {
    return factory.createClient(AuthorityTenantSettingsClient.class);
  }
}
