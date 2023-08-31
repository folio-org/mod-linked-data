package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.provision;

import static org.folio.linked.data.util.Bibframe2Constants.PLACE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.PROVISION_ACTIVITY_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.PUBLICATION;
import static org.folio.linked.data.util.Bibframe2Constants.PUBLICATION_URL;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance2;
import org.folio.linked.data.domain.dto.PublicationField2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.Instance2SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = PUBLICATION_URL, predicate = PROVISION_ACTIVITY_PRED, dtoClass = PublicationField2.class)
public class Publication2MapperUnit implements Instance2SubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Instance2 toDto(Resource source, Instance2 destination) {
    var publication = coreMapper.toProvisionActivity(source);
    coreMapper.addMappedProperties(source, PLACE_PRED, publication::addPlaceItem);
    return destination.addProvisionActivityItem(new PublicationField2().publication(publication));
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var publication = ((PublicationField2) dto).getPublication();
    return coreMapper.provisionActivityToEntity(publication, PUBLICATION_URL, PUBLICATION);
  }

}
