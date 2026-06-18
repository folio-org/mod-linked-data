package org.folio.linked.data.mapper.dto.resource.common;

import java.util.Set;
import org.folio.linked.data.domain.dto.AuthorityRequest;
import org.folio.linked.data.domain.dto.AuthorityResponse;
import org.folio.linked.data.domain.dto.InstanceRequest;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.mapper.dto.resource.base.SingleResourceMapperUnit;

public interface SubResourceMapperUnit extends SingleResourceMapperUnit {

  @Override
  default Set<Class<?>> supportedParents() {
    return Set.of(InstanceRequest.class, InstanceResponse.class, AuthorityRequest.class, AuthorityResponse.class);
  }
}
