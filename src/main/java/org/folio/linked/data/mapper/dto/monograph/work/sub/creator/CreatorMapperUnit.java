package org.folio.linked.data.mapper.dto.monograph.work.sub.creator;

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

public abstract class CreatorMapperUnit extends AgentMapperUnit {

  CreatorMapperUnit(CoreMapper coreMapper,
                    HashService hashService,
                    Function<Agent, AgentContainer> agentTypeProvider,
                    Function<Object, Agent> agentProvider,
                    AgentRoleAssigner agentRoleAssigner,
                    ResourceTypeDictionary type) {
    super(coreMapper, hashService,
      (dto, creator) -> {
        var agentContainer = agentTypeProvider.apply(creator);
        if (dto instanceof Work work) {
          work.addCreatorItem(agentContainer);
        }
        if (dto instanceof WorkReference work) {
          work.addCreatorItem(agentContainer);
        }
      },
      agentProvider,
      agentRoleAssigner,
      type);
  }

}
