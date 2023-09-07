package org.folio.linked.data.mapper.resource.monograph.inner.instance;

import static org.folio.linked.data.util.BibframeConstants.ACCESS_LOCATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.CARRIER_PRED;
import static org.folio.linked.data.util.BibframeConstants.COPYRIGHT_DATE;
import static org.folio.linked.data.util.BibframeConstants.DIMENSIONS;
import static org.folio.linked.data.util.BibframeConstants.DISTRIBUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.EDITION_STATEMENT;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.ISSUANCE;
import static org.folio.linked.data.util.BibframeConstants.MANUFACTURE_PRED;
import static org.folio.linked.data.util.BibframeConstants.MAP_PRED;
import static org.folio.linked.data.util.BibframeConstants.MEDIA_PRED;
import static org.folio.linked.data.util.BibframeConstants.PRODUCTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.PROJECTED_PROVISION_DATE;
import static org.folio.linked.data.util.BibframeConstants.PUBLICATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.RESPONSIBILITY_STATEMENT;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.InstanceTitleField;
import org.folio.linked.data.domain.dto.InstanceTitleInner;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.InnerResourceMapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = INSTANCE)
public class InstanceMapperUnit implements InnerResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;
  private final SubResourceMapper mapper;

  @Override
  public BibframeResponse toDto(Resource source, BibframeResponse destination) {
    coreMapper.mapWithResources(mapper, source, destination::addInstanceItem, Instance.class);
    return destination;
  }

  @Override
  public Resource toEntity(Object resourceDto) {
    Instance dto = (Instance) resourceDto;
    var resource = new Resource();
    resource.setType(resourceTypeService.get(INSTANCE));
    resource.setDoc(getDoc(dto));
    resource.setLabel(getLabel(dto));
    coreMapper.mapResourceEdges(dto.getTitle(), resource, INSTANCE_TITLE_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getProduction(), resource, PRODUCTION_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getPublication(), resource, PUBLICATION_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getDistribution(), resource, DISTRIBUTION_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getManufacture(), resource, MANUFACTURE_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getAccessLocation(), resource, ACCESS_LOCATION_PRED, Instance.class,
      mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getMap(), resource, MAP_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getMedia(), resource, MEDIA_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getCarrier(), resource, CARRIER_PRED, Instance.class, mapper::toEntity);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private String getLabel(Instance dto) {
    var emptyString = "";
    var title = getTitle(dto, InstanceTitleField.class);
    if (!Objects.isNull(title)) {
      return title.getInstanceTitle().getMainTitle().stream().findFirst().orElse(emptyString);
    }

    var parTitle = getTitle(dto, ParallelTitleField.class);
    if (parTitle != null) {
      return parTitle.getParallelTitle().getMainTitle().stream().findFirst().orElse(emptyString);
    }

    var varTitle = getTitle(dto, VariantTitleField.class);
    if (varTitle != null) {
      return varTitle.getVariantTitle().getMainTitle().stream().findFirst().orElse(emptyString);
    }

    return emptyString;
  }

  private <T extends InstanceTitleInner> T getTitle(Instance dto, Class<T> titleClass) {
    if (Objects.isNull(dto.getTitle())) {
      return null;
    } else {
      return dto.getTitle().stream()
        .filter(titleClass::isInstance)
        .findFirst()
        .map(titleClass::cast)
        .orElse(null);
    }
  }

  private JsonNode getDoc(Instance dto) {
    var map = new HashMap<String, List<String>>();
    map.put(DIMENSIONS, dto.getDimensions());
    map.put(RESPONSIBILITY_STATEMENT, dto.getResponsibilityStatement());
    map.put(EDITION_STATEMENT, dto.getEdition());
    map.put(COPYRIGHT_DATE, dto.getCopyrightDate());
    map.put(PROJECTED_PROVISION_DATE, dto.getProjectProvisionDate());
    map.put(ISSUANCE, dto.getIssuance());
    return coreMapper.toJson(map);
  }

}
