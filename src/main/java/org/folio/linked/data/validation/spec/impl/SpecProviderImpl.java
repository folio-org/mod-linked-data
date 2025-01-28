package org.folio.linked.data.validation.spec.impl;

import feign.FeignException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.client.SpecClient;
import org.folio.linked.data.validation.spec.SpecProvider;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationDtoCollection;
import org.folio.rspec.domain.dto.SpecificationRuleDto;
import org.folio.rspec.domain.dto.SpecificationRuleDtoCollection;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class SpecProviderImpl implements SpecProvider {

  private final SpecClient client;

  @Override
  public List<SpecificationRuleDto> getSpecRules() {
    try {
      return Optional.ofNullable(client.getBibMarcSpecs())
        .map(ResponseEntity::getBody)
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
}
