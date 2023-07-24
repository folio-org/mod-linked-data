package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import java.util.Set;
import org.folio.linked.data.domain.dto.Contribution;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;

public interface ContributionSubResourceMapperUnit extends SubResourceMapperUnit<Contribution> {

  @Override
  default Set<Class> getParentDto() {
    return Set.of(Contribution.class);
  }
}
