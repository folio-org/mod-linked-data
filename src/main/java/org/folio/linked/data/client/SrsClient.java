package org.folio.linked.data.client;

import org.folio.rest.jaxrs.model.Record;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "source-storage")
public interface SrsClient {

  @GetMapping(value = "/records/{inventoryId}/formatted?idType=INSTANCE")
  ResponseEntity<Record> getSourceStorageInstanceRecordById(@PathVariable("inventoryId") String inventoryId);

  @GetMapping(value = "/records/{srsId}/formatted?idType=SRS_RECORD")
  ResponseEntity<Record> getSourceStorageRecordBySrsId(@PathVariable("srsId") String srsId);
}
