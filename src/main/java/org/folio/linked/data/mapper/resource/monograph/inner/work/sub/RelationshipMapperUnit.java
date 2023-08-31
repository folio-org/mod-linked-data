package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import static org.folio.linked.data.util.Bibframe2Constants.RELATED_TO_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.RELATION_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.REL_WORK_LOOKUP;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Relationship2;
import org.folio.linked.data.domain.dto.RelationshipField2;
import org.folio.linked.data.domain.dto.Work2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = REL_WORK_LOOKUP)
public class RelationshipMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Work2 toDto(Resource source, Work2 destination) {
    var relationship = coreMapper.readResourceDoc(source, Relationship2.class);
    coreMapper.addMappedProperties(source, RELATED_TO_PRED, relationship::addRelatedToItem);
    coreMapper.addMappedProperties(source, RELATION_PRED, relationship::addRelationItem);
    destination.addRelationshipItem(new RelationshipField2().relationship(relationship));
    return destination;
  }
}
