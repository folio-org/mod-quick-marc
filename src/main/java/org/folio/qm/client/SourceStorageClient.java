package org.folio.qm.client;

import java.util.List;
import org.folio.qm.client.model.ParsedRecordDto;
import org.folio.qm.client.model.Record;
import org.folio.qm.client.model.Snapshot;
import org.folio.qm.client.model.SourceRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "source-storage")
public interface SourceStorageClient {

  @GetMapping(value = "/source-records/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  SourceRecord getSourceRecord(@PathVariable("id") String id, @RequestParam("idType") IdType idType);

  @PutMapping(value = "/source-records/parsed-record")
  ResponseEntity<Void> putParsedRecordDto(ParsedRecordDto parsedRecordDto);

  @PostMapping(value = "/snapshots")
  ResponseEntity<Snapshot> createSnapshot(Snapshot snapshot);

  @PostMapping(value = "/records")
  ResponseEntity<Record> createSrsRecord(Record record);

  @PostMapping(value = "/batch/verified-records")
  MarcBibCollection verifyMarcBibRecords(List<String> hrids);

  enum IdType {
    EXTERNAL
  }

  record MarcBibCollection(List<String> invalidMarcBibIds) {
    public boolean isMarcBibIdsValid() {
      return invalidMarcBibIds == null || invalidMarcBibIds.isEmpty();
    }
  }
}
