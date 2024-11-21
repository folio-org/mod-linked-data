package org.folio.linked.data.validation.spec.impl;

import static org.folio.linked.data.configuration.CacheConfiguration.SPEC_RULES;

import feign.FeignException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.folio.linked.data.client.SpecClient;
import org.folio.linked.data.validation.spec.SpecProvider;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationDtoCollection;
import org.folio.rspec.domain.dto.SpecificationRuleDto;
import org.folio.rspec.domain.dto.SpecificationRuleDtoCollection;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpecProviderImpl implements SpecProvider {

  private final SpecClient client;

  @Cacheable(value = SPEC_RULES)
  @Override
  public List<SpecificationRuleDto> getSpecRules() {
    try {
      return Optional.ofNullable(client.getBibMarcSpecs().getBody())
        .map(SpecificationDtoCollection::getSpecifications)
        .stream()
        .flatMap(Collection::stream)
        .findFirst()
        .map(SpecificationDto::getId)
        .map(client::getSpecRules)
        .map(ResponseEntity::getBody)
        .map(SpecificationRuleDtoCollection::getRules)
        .stream()
        .flatMap(Collection::stream)
        .toList();
    } catch (FeignException e) {
      log.error("Unexpected exception during specification rules retrieval", e);
      return Collections.emptyList();
    }
  }

  @CacheEvict(value = SPEC_RULES)
  @Scheduled(fixedRateString = "${mod-linked-data.caching.ttl.specRules}")
  public void emptyCache() {
    log.info("Emptying {} cache", SPEC_RULES);
  }
}
