package org.folio.qm.client;

import java.util.Optional;
import java.util.UUID;
import org.folio.Record;
import org.folio.qm.client.model.SourceRecord;
import org.folio.qm.client.model.SourceRecordSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "source-storage")
public interface SourceStorageClient {

  @GetMapping(value = "/source-records/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  Optional<SourceRecord> getSourceRecord(@PathVariable UUID id, @RequestParam("idType") IdType idType);

  @GetMapping(value = "/records/{id}")
  Optional<Record> getSourceRecord(@PathVariable UUID id);

  @PostMapping(value = "/records", produces = MediaType.APPLICATION_JSON_VALUE)
  Record createSourceRecord(@RequestBody Record srsRecord);

  @PutMapping(value = "/records/{id}/generation")
  void updateSourceRecord(@PathVariable UUID id, Record srsRecord);

  @PostMapping(value = "/source-records/snapshot")
  SourceRecordSnapshot createSnapshot(SourceRecordSnapshot snapshot);

  enum IdType {
    EXTERNAL
  }
}
