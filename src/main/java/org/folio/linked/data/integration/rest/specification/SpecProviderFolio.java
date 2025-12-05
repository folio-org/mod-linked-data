package org.folio.linked.data.integration.rest.specification;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import feign.FeignException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationDtoCollection;
import org.folio.rspec.domain.dto.SpecificationRuleDto;
import org.folio.rspec.domain.dto.SpecificationRuleDtoCollection;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class SpecProviderFolio implements SpecProvider {

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
