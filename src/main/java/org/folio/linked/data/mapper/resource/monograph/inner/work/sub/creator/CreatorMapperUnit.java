package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.creator;

import java.util.function.Function;
import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.domain.dto.AgentTypeInner;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.AgentMapperUnit;

public class CreatorMapperUnit extends AgentMapperUnit {
  CreatorMapperUnit(CoreMapper coreMapper, Function<Agent, AgentTypeInner> agentTypeProvider) {
    super(coreMapper, (work, creator) -> work.addCreatorItem(agentTypeProvider.apply(creator)));
  }
}
