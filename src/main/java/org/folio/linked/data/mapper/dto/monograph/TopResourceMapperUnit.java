package org.folio.linked.data.mapper.dto.monograph;

import static java.util.Objects.isNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.folio.linked.data.domain.dto.PrimaryTitleField;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.domain.dto.TitleField;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapperUnit;

public abstract class TopResourceMapperUnit implements SingleResourceMapperUnit {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(
    ResourceRequestDto.class,
    ResourceResponseDto.class
  );

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }

  protected List<String> getPrimaryMainTitles(List<TitleField> titles) {
    if (isNull(titles)) {
      return new ArrayList<>();
    }
    return titles.stream()
      .filter(PrimaryTitleField.class::isInstance)
      .map(PrimaryTitleField.class::cast)
      .map(PrimaryTitleField::getPrimaryTitle)
      .flatMap(pt -> pt.getMainTitle().stream())
      .toList();
  }
}
