package org.folio.linked.data.mapper.resource.monograph.inner.instance;

import static org.folio.linked.data.util.BibframeConstants.CARRIER_PRED;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.COPYRIGHT_DATE_URL;
import static org.folio.linked.data.util.BibframeConstants.DIMENSIONS_URL;
import static org.folio.linked.data.util.BibframeConstants.EDITION_STATEMENT_URL;
import static org.folio.linked.data.util.BibframeConstants.ELECTRONIC_LOCATOR_PRED;
import static org.folio.linked.data.util.BibframeConstants.EXTENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIED_BY_PRED;
import static org.folio.linked.data.util.BibframeConstants.IMM_ACQUISITION_PRED;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_PRED;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_URL;
import static org.folio.linked.data.util.BibframeConstants.ISSUANCE_PRED;
import static org.folio.linked.data.util.BibframeConstants.MEDIA_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PROJECT_PROVISION_DATE_URL;
import static org.folio.linked.data.util.BibframeConstants.PROVISION_ACTIVITY_PRED;
import static org.folio.linked.data.util.BibframeConstants.RESPONSIBILITY_STATEMENT_URL;
import static org.folio.linked.data.util.BibframeConstants.SUPP_CONTENT_PRED;
import static org.folio.linked.data.util.MappingUtil.addMappedResources;
import static org.folio.linked.data.util.MappingUtil.hash;
import static org.folio.linked.data.util.MappingUtil.mapResourceEdges;
import static org.folio.linked.data.util.MappingUtil.toJson;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.common.inner.InnerResourceMapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.model.entity.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = INSTANCE)
public class MonographInstanceMapper implements InnerResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final DictionaryService<Predicate> predicateService;
  private final ObjectMapper objectMapper;
  private final SubResourceMapper mapper;

  @Override
  public BibframeResponse toDto(Resource resource, BibframeResponse destination) {
    addMappedResources(objectMapper, mapper, resource, destination::addInstanceItem, Instance.class);
    return destination;
  }

  @Override
  public ResourceEdge toEntity(Object innerResourceDto, Resource parent) {
    var edge = new ResourceEdge();
    edge.setSource(parent);
    edge.setPredicate(predicateService.get(INSTANCE_PRED));
    edge.setTarget(mapInstance((Instance) innerResourceDto));
    return edge;
  }

  private Resource mapInstance(Instance dto) {
    var resource = new Resource();
    resource.setLabel(INSTANCE_URL);
    resource.setType(resourceTypeService.get(INSTANCE));
    resource.setDoc(toJson(getDoc(dto), objectMapper));
    mapResourceEdges(dto.getTitle(), resource, getPredicate(INSTANCE_TITLE_PRED), mapper::toEntity);
    mapResourceEdges(dto.getProvisionActivity(), resource, getPredicate(PROVISION_ACTIVITY_PRED), mapper::toEntity);
    mapResourceEdges(dto.getContribution(), resource, getPredicate(CONTRIBUTION_PRED), mapper::toEntity);
    mapResourceEdges(dto.getIdentifiedBy(), resource, getPredicate(IDENTIFIED_BY_PRED), mapper::toEntity);
    mapResourceEdges(dto.getNote(), resource, getPredicate(NOTE_PRED), mapper::toEntity);
    mapResourceEdges(dto.getSupplementaryContent(), resource, getPredicate(SUPP_CONTENT_PRED), mapper::toEntity);
    mapResourceEdges(dto.getImmediateAcquisition(), resource, getPredicate(IMM_ACQUISITION_PRED), mapper::toEntity);
    mapResourceEdges(dto.getExtent(), resource, getPredicate(EXTENT_PRED), mapper::toEntity);
    mapResourceEdges(dto.getElectronicLocator(), resource, getPredicate(ELECTRONIC_LOCATOR_PRED), mapper::toEntity);
    mapResourceEdges(dto.getIssuance(), resource, getPredicate(ISSUANCE_PRED), mapper::toEntity);
    mapResourceEdges(dto.getMedia(), resource, getPredicate(MEDIA_PRED), mapper::toEntity);
    mapResourceEdges(dto.getCarrier(), resource, getPredicate(CARRIER_PRED), mapper::toEntity);
    resource.setResourceHash(hash(resource, objectMapper));
    return resource;
  }

  private Supplier<Predicate> getPredicate(String predicate) {
    return () -> predicateService.get(predicate);
  }

  private Map<String, List<String>> getDoc(Instance dto) {
    var map = new HashMap<String, List<String>>();
    map.put(DIMENSIONS_URL, dto.getDimensions());
    map.put(RESPONSIBILITY_STATEMENT_URL, dto.getResponsiblityStatement());
    map.put(EDITION_STATEMENT_URL, dto.getEditionStatement());
    map.put(COPYRIGHT_DATE_URL, dto.getCopyrightDate());
    map.put(PROJECT_PROVISION_DATE_URL, dto.getProjectProvisionDate());
    return map;
  }

}
