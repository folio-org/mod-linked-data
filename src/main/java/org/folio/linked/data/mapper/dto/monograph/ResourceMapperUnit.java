package org.folio.linked.data.mapper.dto.monograph;

import static java.util.Objects.isNull;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.folio.linked.data.domain.dto.BasicTitleField;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.domain.dto.TitleTitleInner;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapperUnit;

public abstract class ResourceMapperUnit implements SingleResourceMapperUnit {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Collections.singleton(ResourceDto.class);

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }

  protected List<String> getPossibleLabels(List<TitleTitleInner> titles) {
    if (isNull(titles)) {
      return new ArrayList<>();
    }
    return titles.stream()
      .sorted(Comparator.comparing(o -> o.getClass().getSimpleName()))
      .map(ResourceMapperUnit::getMainTitle).toList();
  }

  private static String getMainTitle(TitleTitleInner t) {
    if (t instanceof BasicTitleField basicTitleField) {
      var basicTitle = basicTitleField.getBasicTitle();
      return getFirstValue(basicTitle::getMainTitle);
    }
    if (t instanceof ParallelTitleField parallelTitleField) {
      var parallelTitle = parallelTitleField.getParallelTitle();
      return getFirstValue(parallelTitle::getMainTitle);
    }
    if (t instanceof VariantTitleField variantTitleField) {
      var variantTitle = variantTitleField.getVariantTitle();
      return getFirstValue(variantTitle::getMainTitle);
    }
    return "";
  }
}