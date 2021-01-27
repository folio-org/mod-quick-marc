package org.folio.qm.controller;

import javax.validation.Valid;

import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.folio.spring.FolioExecutionContext;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.folio.tenant.rest.resource.TenantApi;

@RestController("folioTenantController")
@RequestMapping(value = "/_/")
public class QuickMarcTenantController implements TenantApi {

  @Override
  public ResponseEntity<String> postTenant(@Valid TenantAttributes tenantAttributes) {
    return ResponseEntity.ok().body("true");
  }
}
