package org.folio.linked.data.service.rdf;

import static java.util.Optional.ofNullable;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.client.SearchClient;
import org.folio.linked.data.domain.dto.AuthorityItem;
import org.folio.linked.data.domain.dto.AuthoritySearchResponse;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LccnResourceProvider implements Function<String, Optional<Resource>> {
  private final SearchClient searchClient;
  private final ResourceModelMapper resourceModelMapper;
  private final ResourceMarcAuthorityService resourceMarcAuthorityService;

  @Override
  public Optional<Resource> apply(String lccn) {
    return searchForInventoryId(lccn)
      .flatMap(resourceMarcAuthorityService::fetchAuthorityOrCreateByInventoryId)
      .map(resourceModelMapper::toModel);
  }

  private Optional<String> searchForInventoryId(String lccn) {
    return ofNullable(searchClient.searchAuthorities("lccn = " + lccn)
        .getBody())
      .map(AuthoritySearchResponse::getAuthorities)
      .stream()
      .flatMap(Collection::stream)
      .map(AuthorityItem::getId)
      .filter(Objects::nonNull)
      .findFirst();
  }
}
