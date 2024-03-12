package org.folio.linked.data.mapper.dto.monograph.work.sub.contributor;

import java.util.function.Function;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.domain.dto.AgentContainer;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkReference;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.monograph.work.sub.AgentMapperUnit;
import org.folio.linked.data.mapper.dto.monograph.work.sub.AgentRoleAssigner;
import org.folio.linked.data.service.HashService;

public abstract class ContributorMapperUnit extends AgentMapperUnit {

  ContributorMapperUnit(CoreMapper coreMapper,
                        HashService hashService,
                        Function<Agent, AgentContainer> agentTypeProvider,
                        Function<Object, Agent> agentProvider,
                        AgentRoleAssigner agentRoleAssigner,
                        ResourceTypeDictionary type) {
    super(coreMapper, hashService,
      (dto, contributor) -> {
        if (dto instanceof Work work) {
          work.addContributorItem(agentTypeProvider.apply(contributor));
        }
        if (dto instanceof WorkReference work) {
          work.addContributorItem(agentTypeProvider.apply(contributor));
        }
      },
      agentProvider,
      agentRoleAssigner,
      type
    );
  }

}
