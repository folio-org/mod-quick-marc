package org.folio.qm.client;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import org.folio.qm.client.model.HoldingsRecordsSource;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("holdings-sources")
public interface HoldingsSourceClient {

  @GetMapping(value = "?query=name=={type}&limit=1", produces = APPLICATION_JSON_VALUE)
  HoldingsRecordsSources getHoldingSourceByName(@PathVariable("type") String type);

  record HoldingsRecordsSources(List<HoldingsRecordsSource> holdingsRecordsSources, int totalRecords) {
    public String getSourceId() {
      if (holdingsRecordsSources != null && !holdingsRecordsSources.isEmpty()) {
        HoldingsRecordsSource source = holdingsRecordsSources.getFirst();
        return source != null ? source.getId() : null;
      }
      return null;
    }
  }
}
