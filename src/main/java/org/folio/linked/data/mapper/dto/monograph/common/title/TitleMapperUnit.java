package org.folio.linked.data.mapper.dto.monograph.common.title;

import java.util.Set;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapperUnit;

public abstract class TitleMapperUnit implements SingleResourceMapperUnit {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(
    Instance.class,
    InstanceResponse.class,
    Work.class,
    WorkResponse.class
  );

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }
}
