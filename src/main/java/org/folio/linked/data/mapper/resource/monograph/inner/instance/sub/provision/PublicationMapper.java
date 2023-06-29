package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.provision;

import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PROVISION_ACTIVITY_PRED;
import static org.folio.linked.data.util.BibframeConstants.PUBLICATION;
import static org.folio.linked.data.util.BibframeConstants.PUBLICATION_URL;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.PublicationField;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = PUBLICATION, predicate = PROVISION_ACTIVITY_PRED, dtoClass = PublicationField.class)
public class PublicationMapper implements InstanceSubResourceMapper {

  private final CommonMapper commonMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var publication = commonMapper.toProvisionActivity(source);
    commonMapper.addMappedProperties(source, PLACE_PRED, publication::addPlaceItem);
    return destination.addProvisionActivityItem(new PublicationField().publication(publication));
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var publication = ((PublicationField) dto).getPublication();
    return commonMapper.provisionActivityToEntity(publication, PUBLICATION_URL, PUBLICATION);
  }

}
