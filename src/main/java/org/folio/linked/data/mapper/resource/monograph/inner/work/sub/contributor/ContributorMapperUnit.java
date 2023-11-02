package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.contributor;

import java.util.function.Function;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.domain.dto.AgentTypeInner;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.AgentMapperUnit;

public abstract class ContributorMapperUnit extends AgentMapperUnit {
  ContributorMapperUnit(CoreMapper coreMapper,
                        Function<Agent, AgentTypeInner> agentTypeProvider,
                        Function<Object, Agent> agentProvider,
                        ResourceTypeDictionary type) {
    super(
      coreMapper,
      (work, contributor) -> work.addContributorItem(agentTypeProvider.apply(contributor)),
      agentProvider,
      type
    );
  }
}
