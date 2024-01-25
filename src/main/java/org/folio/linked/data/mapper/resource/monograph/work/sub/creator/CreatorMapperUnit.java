package org.folio.linked.data.mapper.resource.monograph.work.sub.creator;

import java.util.function.Function;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.domain.dto.AgentContainer;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkReference;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.monograph.work.sub.AgentMapperUnit;

public class CreatorMapperUnit extends AgentMapperUnit {
  CreatorMapperUnit(CoreMapper coreMapper,
                    Function<Agent, AgentContainer> agentTypeProvider,
                    Function<Object, Agent> agentProvider,
                    ResourceTypeDictionary type) {
    super(coreMapper, (dto, creator) -> {
      if (dto instanceof Work work) {
        work.addCreatorItem(agentTypeProvider.apply(creator));
      }
      if (dto instanceof WorkReference work) {
        work.addCreatorItem(agentTypeProvider.apply(creator));
      }
    }, agentProvider, type);
  }
}
