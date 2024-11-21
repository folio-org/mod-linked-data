package org.folio.linked.data.client;

import java.util.UUID;
import org.folio.rspec.domain.dto.SpecificationDtoCollection;
import org.folio.rspec.domain.dto.SpecificationRuleDtoCollection;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "specification-storage")
public interface SpecClient {

  @GetMapping(value = "/specifications?profile=bibliographic&family=MARC")
  ResponseEntity<SpecificationDtoCollection> getBibMarcSpecs();

  @GetMapping(value = "/specifications/{specId}/rules")
  ResponseEntity<SpecificationRuleDtoCollection> getSpecRules(@PathVariable("specId") UUID specId);
}
