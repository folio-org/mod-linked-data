package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.provision;

import static org.folio.linked.data.util.BibframeConstants.MANUFACTURE;
import static org.folio.linked.data.util.BibframeConstants.MANUFACTURE_URL;
import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PROVISION_ACTIVITY_PRED;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.provisionActivityToEntity;
import static org.folio.linked.data.util.MappingUtil.toProvisionActivity;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.ManufactureField;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapper;
import org.folio.linked.data.model.entity.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = MANUFACTURE, predicate = PROVISION_ACTIVITY_PRED, dtoClass = ManufactureField.class)
public class ManufactureMapper implements InstanceSubResourceMapper {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final DictionaryService<Predicate> predicateService;
  private final ObjectMapper mapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var manufacture = toProvisionActivity(mapper, source);
    addMappedProperties(mapper, source, PLACE_PRED, manufacture::addPlaceItem);
    return destination.addProvisionActivityItem(new ManufactureField().manufacture(manufacture));
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var manufacture = ((ManufactureField) dto).getManufacture();
    return provisionActivityToEntity(manufacture, MANUFACTURE_URL, resourceTypeService.get(MANUFACTURE), predicateService,
      resourceTypeService, mapper);
  }

}
