package org.folio.linked.data.mapper.dto.monograph.common;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Optional;

public interface MarcCodeProvider {

  String getLinkPrefix();

  default Optional<String> getMarcCode(String linkSuffix) {
    return ofNullable(linkSuffix);
  }

  default List<String> getMarcCodes(List<String> links) {
    if (isNull(links)) {
      return List.of();
    }
    final var linkPrefix = getLinkPrefix().endsWith("/") ? getLinkPrefix() : getLinkPrefix() + "/";
    return links.stream()
      .filter(link -> link.startsWith(linkPrefix))
      .map(link -> link.substring(linkPrefix.length()))
      .flatMap(linkSuffix -> getMarcCode(linkSuffix).stream())
      .toList();
  }
}
