package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.contributor;

import java.util.function.Function;
import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.domain.dto.AgentTypeInner;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.AgentMapperUnit;

public abstract class ContributorMapperUnit extends AgentMapperUnit {
  ContributorMapperUnit(CoreMapper coreMapper, Function<Agent, AgentTypeInner> agentTypeProvider) {
    super(coreMapper, (work, contributor) -> work.addContributorItem(agentTypeProvider.apply(contributor)));
  }
}
