package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.provision;

import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PROVISION_ACTIVITY_PRED;
import static org.folio.linked.data.util.BibframeConstants.PUBLICATION;
import static org.folio.linked.data.util.BibframeConstants.PUBLICATION_URL;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.PublicationField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = PUBLICATION, predicate = PROVISION_ACTIVITY_PRED, dtoClass = PublicationField.class)
public class PublicationMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var publication = coreMapper.toProvisionActivity(source);
    coreMapper.addMappedProperties(source, PLACE_PRED, publication::addPlaceItem);
    return destination.addProvisionActivityItem(new PublicationField().publication(publication));
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var publication = ((PublicationField) dto).getPublication();
    return coreMapper.provisionActivityToEntity(publication, PUBLICATION_URL, PUBLICATION);
  }

}
