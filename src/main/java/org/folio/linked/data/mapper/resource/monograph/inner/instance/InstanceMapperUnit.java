package org.folio.linked.data.mapper.resource.monograph.inner.instance;

import static org.folio.ld.dictionary.PredicateDictionary.ACCESS_LOCATION;
import static org.folio.ld.dictionary.PredicateDictionary.CARRIER;
import static org.folio.ld.dictionary.PredicateDictionary.COPYRIGHT;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.MEDIA;
import static org.folio.ld.dictionary.PredicateDictionary.PE_DISTRIBUTION;
import static org.folio.ld.dictionary.PredicateDictionary.PE_MANUFACTURE;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PRODUCTION;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.Property.DIMENSIONS;
import static org.folio.ld.dictionary.Property.EDITION_STATEMENT;
import static org.folio.ld.dictionary.Property.EXTENT;
import static org.folio.ld.dictionary.Property.ISSUANCE;
import static org.folio.ld.dictionary.Property.PROJECTED_PROVISION_DATE;
import static org.folio.ld.dictionary.Property.RESPONSIBILITY_STATEMENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
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
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = INSTANCE)
public class InstanceMapperUnit implements InnerResourceMapperUnit {

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
    instance.addType(INSTANCE);
    instance.setDoc(getDoc(dto));
    instance.setLabel(getFirstValue(() -> getPossibleLabels(dto)));
    coreMapper.mapInnerEdges(dto.getTitle(), instance, TITLE, Instance.class, mapper::toEntity);
    coreMapper.mapInnerEdges(dto.getProduction(), instance, PE_PRODUCTION, Instance.class, mapper::toEntity);
    coreMapper.mapInnerEdges(dto.getPublication(), instance, PE_PUBLICATION, Instance.class, mapper::toEntity);
    coreMapper.mapInnerEdges(dto.getDistribution(), instance, PE_DISTRIBUTION, Instance.class, mapper::toEntity);
    coreMapper.mapInnerEdges(dto.getManufacture(), instance, PE_MANUFACTURE, Instance.class, mapper::toEntity);
    coreMapper.mapInnerEdges(dto.getAccessLocation(), instance, ACCESS_LOCATION, Instance.class, mapper::toEntity);
    coreMapper.mapInnerEdges(dto.getMap(), instance, MAP, Instance.class, mapper::toEntity);
    coreMapper.mapInnerEdges(dto.getMedia(), instance, MEDIA, Instance.class, mapper::toEntity);
    coreMapper.mapInnerEdges(dto.getCarrier(), instance, CARRIER, Instance.class, mapper::toEntity);
    coreMapper.mapInnerEdges(dto.getCopyright(), instance, COPYRIGHT, Instance.class, mapper::toEntity);
    coreMapper.mapInnerEdges(dto.getInstantiates(), instance, INSTANTIATES, Instance.class, mapper::toEntity);
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
    map.put(EXTENT, dto.getExtent());
    map.put(DIMENSIONS, dto.getDimensions());
    map.put(RESPONSIBILITY_STATEMENT, dto.getResponsibilityStatement());
    map.put(EDITION_STATEMENT, dto.getEdition());
    map.put(PROJECTED_PROVISION_DATE, dto.getProjectProvisionDate());
    map.put(ISSUANCE, dto.getIssuance());
    return coreMapper.toJson(map);
  }

}
