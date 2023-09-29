package org.folio.linked.data.mapper.resource.monograph.inner.instance;

import static org.folio.linked.data.util.BibframeConstants.ACCESS_LOCATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.CARRIER_PRED;
import static org.folio.linked.data.util.BibframeConstants.COPYRIGHT_PRED;
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
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.InstanceTitleField;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.domain.dto.ResourceDto;
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
  public ResourceDto toDto(Resource source, ResourceDto destination) {
    var instanceField = new InstanceField();
    coreMapper.mapWithResources(mapper, source, instanceField::setInstance, Instance.class);
    instanceField.getInstance().setId(String.valueOf(source.getResourceHash()));
    return destination.resource(instanceField);
  }

  @Override
  public Resource toEntity(Object resourceDto) {
    Instance dto = ((InstanceField) resourceDto).getInstance();
    var instance = new Resource();
    instance.addType(resourceTypeService.get(INSTANCE));
    instance.setDoc(getDoc(dto));
    instance.setLabel(getFirstValue(() -> getPossibleLabels(dto)));
    coreMapper.mapResourceEdges(dto.getTitle(), instance, INSTANCE_TITLE_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getProduction(), instance, PRODUCTION_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getPublication(), instance, PUBLICATION_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getDistribution(), instance, DISTRIBUTION_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getManufacture(), instance, MANUFACTURE_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getAccessLocation(), instance, ACCESS_LOCATION_PRED, Instance.class,
      mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getMap(), instance, MAP_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getMedia(), instance, MEDIA_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getCarrier(), instance, CARRIER_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getCopyright(), instance, COPYRIGHT_PRED, Instance.class, mapper::toEntity);
    instance.setResourceHash(coreMapper.hash(instance));
    return instance;
  }

  private List<String> getPossibleLabels(Instance instance) {
    return instance.getTitle().stream()
      .sorted(Comparator.comparing(o -> o.getClass().getSimpleName()))
      .map(t -> {
        if (t instanceof InstanceTitleField instanceTitleField) {
          var instanceTitle = instanceTitleField.getInstanceTitle();
          return getFirstValue(instanceTitle::getMainTitle);
        }
        if (t instanceof ParallelTitleField parallelTitleField) {
          var parallelTitle = parallelTitleField.getParallelTitle();
          return getFirstValue(parallelTitle::getMainTitle);
        }
        if (t instanceof VariantTitleField variantTitleField) {
          var variantTitle = variantTitleField.getVariantTitle();
          return getFirstValue(variantTitle::getMainTitle);
        }
        return "";
      }).toList();
  }

  private JsonNode getDoc(Instance dto) {
    var map = new HashMap<String, List<String>>();
    map.put(DIMENSIONS, dto.getDimensions());
    map.put(RESPONSIBILITY_STATEMENT, dto.getResponsibilityStatement());
    map.put(EDITION_STATEMENT, dto.getEdition());
    map.put(PROJECTED_PROVISION_DATE, dto.getProjectProvisionDate());
    map.put(ISSUANCE, dto.getIssuance());
    return coreMapper.toJson(map);
  }

}
