package org.folio.linked.data.mapper.resource.monograph.work.sub;

import static org.folio.ld.dictionary.PropertyDictionary.LCNAF_ID;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.domain.dto.AgentContainer;
import org.folio.linked.data.domain.dto.FamilyField;
import org.folio.linked.data.domain.dto.MeetingField;
import org.folio.linked.data.domain.dto.OrganizationField;
import org.folio.linked.data.domain.dto.PersonField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.model.entity.Resource;

@RequiredArgsConstructor
public abstract class AgentMapperUnit implements WorkSubResourceMapperUnit {
  protected static final Function<Agent, AgentContainer> FAMILY_TO_FIELD_CONVERTER = f -> new FamilyField().family(f);
  protected static final Function<Agent, AgentContainer> PERSON_TO_FIELD_CONVERTER = p -> new PersonField().person(p);
  protected static final Function<Agent, AgentContainer> ORG_TO_FIELD_CONVERTER =
    o -> new OrganizationField().organization(o);
  protected static final Function<Agent, AgentContainer> MEETING_TO_FIELD_CONVERTER =
    m -> new MeetingField().meeting(m);

  protected static final Function<Object, Agent> FIELD_TO_FAMILY_CONVERTER = o -> ((FamilyField) o).getFamily();
  protected static final Function<Object, Agent> FIELD_TO_PERSON_CONVERTER = o -> ((PersonField) o).getPerson();
  protected static final Function<Object, Agent> FIELD_TO_ORG_CONVERTER =
    o -> ((OrganizationField) o).getOrganization();
  protected static final Function<Object, Agent> FIELD_TO_MEETING_CONVERTER = o -> ((MeetingField) o).getMeeting();

  private final CoreMapper coreMapper;
  private final BiConsumer<Work, Agent> agentConsumer;
  private final Function<Object, Agent> agentProvider;
  private final ResourceTypeDictionary type;

  @Override
  public Work toDto(Resource source, Work destination) {
    var person = coreMapper.readResourceDoc(source, Agent.class);
    person.setId(String.valueOf(source.getResourceHash()));
    agentConsumer.accept(destination, person);
    return destination;
  }

  @Override
  public Resource toEntity(Object dto) {
    var agent = agentProvider.apply(dto);
    var resource = new Resource();
    resource.addType(type);
    resource.setDoc(getDoc(agent));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Agent dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, NAME, dto.getName());
    putProperty(map, LCNAF_ID, dto.getLcnafId());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
