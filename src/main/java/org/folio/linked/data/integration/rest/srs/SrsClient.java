package org.folio.linked.data.integration.rest.srs;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import org.folio.rest.jaxrs.model.Record;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@Profile("!" + STANDALONE_PROFILE)
@HttpExchange("source-storage")
public interface SrsClient {

  @GetExchange("/records/{inventoryId}/formatted?idType=INSTANCE")
  ResponseEntity<Record> getSourceStorageInstanceRecordById(@PathVariable("inventoryId") String inventoryId);

  @GetExchange("/records/{srsId}/formatted?idType=SRS_RECORD")
  ResponseEntity<Record> getAuthorityBySrsId(@PathVariable("srsId") String srsId);

  @GetExchange("/records/{inventoryId}/formatted?idType=AUTHORITY")
  ResponseEntity<Record> getAuthorityByInventoryId(@PathVariable("inventoryId") String inventoryId);
}
