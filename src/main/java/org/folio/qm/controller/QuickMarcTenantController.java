package org.folio.qm.controller;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
