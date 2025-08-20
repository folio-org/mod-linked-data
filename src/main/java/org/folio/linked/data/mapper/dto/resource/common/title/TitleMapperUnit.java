package org.folio.linked.data.mapper.dto.resource.common.title;

import static java.lang.String.join;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Set;
import org.folio.linked.data.domain.dto.InstanceRequest;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.WorkRequest;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.SingleResourceMapperUnit;

public abstract class TitleMapperUnit implements SingleResourceMapperUnit {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(
    InstanceRequest.class,
    InstanceResponse.class,
    WorkRequest.class,
    WorkResponse.class
  );

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }

  protected String getLabel(String mainTitle, String subTitle) {
    return isBlank(subTitle) ? mainTitle : join(" ", mainTitle, subTitle);
  }
}
