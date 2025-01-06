package org.folio.linked.data.mapper.dto.monograph;

import static java.lang.String.join;
import static java.util.Objects.isNull;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.folio.linked.data.domain.dto.PrimaryTitleField;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.domain.dto.TitleFieldRequestTitleInner;
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

  protected List<String> getPrimaryMainTitles(List<TitleFieldRequestTitleInner> titles) {
    if (isNull(titles)) {
      return new ArrayList<>();
    }
    return titles.stream()
      .filter(PrimaryTitleField.class::isInstance)
      .map(PrimaryTitleField.class::cast)
      .map(PrimaryTitleField::getPrimaryTitle)
      .map(pt -> join(" ", getFirstValue(pt::getMainTitle), getFirstValue(pt::getSubTitle)))
      .toList();
  }
}
