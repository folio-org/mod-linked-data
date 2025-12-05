package org.folio.linked.data.integration.rest.specification;

import static org.folio.linked.data.util.Constants.Cache.SPEC_RULES;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.UUID;
import org.folio.rspec.domain.dto.SpecificationDtoCollection;
import org.folio.rspec.domain.dto.SpecificationRuleDtoCollection;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@SuppressWarnings("java:S7180")
@Profile("!" + STANDALONE_PROFILE)
@FeignClient(name = "specification-storage")
public interface SpecClient {

  @Cacheable(cacheNames = "bib-marc-specs")
  @GetMapping(value = "/specifications?profile=bibliographic&family=MARC")
  ResponseEntity<SpecificationDtoCollection> getBibMarcSpecs();

  @Cacheable(cacheNames = SPEC_RULES, key = "#specId")
  @GetMapping(value = "/specifications/{specId}/rules")
  ResponseEntity<SpecificationRuleDtoCollection> getSpecRules(@PathVariable("specId") UUID specId);
}
