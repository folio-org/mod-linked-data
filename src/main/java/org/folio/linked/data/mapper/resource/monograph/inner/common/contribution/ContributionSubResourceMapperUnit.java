package org.folio.linked.data.mapper.resource.monograph.inner.common.contribution;

import java.util.Set;
import org.folio.linked.data.domain.dto.Contribution2;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;

public interface ContributionSubResourceMapperUnit extends SubResourceMapperUnit<Contribution2> {

  @Override
  default Set<Class> getParentDto() {
    return Set.of(Contribution2.class);
  }
}
