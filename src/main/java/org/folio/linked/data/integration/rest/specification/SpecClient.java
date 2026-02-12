package org.folio.linked.data.integration.rest.specification;

import static org.folio.linked.data.util.Constants.Cache.SPEC_RULES;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.UUID;
import org.folio.rspec.domain.dto.SpecificationDtoCollection;
import org.folio.rspec.domain.dto.SpecificationRuleDtoCollection;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@SuppressWarnings("java:S7180")
@Profile("!" + STANDALONE_PROFILE)
@HttpExchange("specification-storage")
public interface SpecClient {

  @Cacheable(cacheNames = "bib-marc-specs", key = "@folioExecutionContext.tenantId")
  @GetExchange("/specifications?profile=bibliographic&family=MARC")
  ResponseEntity<SpecificationDtoCollection> getBibMarcSpecs();

  @Cacheable(cacheNames = SPEC_RULES, key = "@folioExecutionContext.tenantId + '_' + #specId")
  @GetExchange("/specifications/{specId}/rules")
  ResponseEntity<SpecificationRuleDtoCollection> getSpecRules(@PathVariable("specId") UUID specId);
}
