package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.provision;

import static org.folio.linked.data.util.BibframeConstants.DISTRIBUTION;
import static org.folio.linked.data.util.BibframeConstants.DISTRIBUTION_URL;
import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PROVISION_ACTIVITY_PRED;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.provisionActivityToEntity;
import static org.folio.linked.data.util.MappingUtil.toProvisionActivity;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.DistributionField;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapper;
import org.folio.linked.data.model.entity.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = DISTRIBUTION, predicate = PROVISION_ACTIVITY_PRED, dtoClass = DistributionField.class)
public class DistributionMapper implements InstanceSubResourceMapper {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final DictionaryService<Predicate> predicateService;
  private final ObjectMapper mapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var distribution = toProvisionActivity(mapper, source);
    addMappedProperties(mapper, source, PLACE_PRED, distribution::addPlaceItem);
    return destination.addProvisionActivityItem(new DistributionField().distribution(distribution));
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var distribution = ((DistributionField) dto).getDistribution();
    return provisionActivityToEntity(distribution, DISTRIBUTION_URL, resourceTypeService.get(DISTRIBUTION),
      predicateService, resourceTypeService, mapper);
  }

}
