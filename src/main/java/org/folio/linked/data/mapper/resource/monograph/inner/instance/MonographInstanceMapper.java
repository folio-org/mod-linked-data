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

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
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
  private final CommonMapper commonMapper;
  private final SubResourceMapper mapper;

  @Override
  public BibframeResponse toDto(Resource resource, BibframeResponse destination) {
    commonMapper.addMappedResources(mapper, resource, destination::addInstanceItem, Instance.class);
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
    resource.setDoc(getDoc(dto));
    commonMapper.mapResourceEdges(dto.getTitle(), resource, INSTANCE_TITLE_PRED, mapper::toEntity);
    commonMapper.mapResourceEdges(dto.getProvisionActivity(), resource, PROVISION_ACTIVITY_PRED, mapper::toEntity);
    commonMapper.mapResourceEdges(dto.getContribution(), resource, CONTRIBUTION_PRED, mapper::toEntity);
    commonMapper.mapResourceEdges(dto.getIdentifiedBy(), resource, IDENTIFIED_BY_PRED, mapper::toEntity);
    commonMapper.mapResourceEdges(dto.getNote(), resource, NOTE_PRED, mapper::toEntity);
    commonMapper.mapResourceEdges(dto.getSupplementaryContent(), resource, SUPP_CONTENT_PRED, mapper::toEntity);
    commonMapper.mapResourceEdges(dto.getImmediateAcquisition(), resource, IMM_ACQUISITION_PRED, mapper::toEntity);
    commonMapper.mapResourceEdges(dto.getExtent(), resource, EXTENT_PRED, mapper::toEntity);
    commonMapper.mapResourceEdges(dto.getElectronicLocator(), resource, ELECTRONIC_LOCATOR_PRED, mapper::toEntity);
    commonMapper.mapResourceEdges(dto.getIssuance(), resource, ISSUANCE_PRED, mapper::toEntity);
    commonMapper.mapResourceEdges(dto.getMedia(), resource, MEDIA_PRED, mapper::toEntity);
    commonMapper.mapResourceEdges(dto.getCarrier(), resource, CARRIER_PRED, mapper::toEntity);
    resource.setResourceHash(commonMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Instance dto) {
    var map = new HashMap<String, List<String>>();
    map.put(DIMENSIONS_URL, dto.getDimensions());
    map.put(RESPONSIBILITY_STATEMENT_URL, dto.getResponsiblityStatement());
    map.put(EDITION_STATEMENT_URL, dto.getEditionStatement());
    map.put(COPYRIGHT_DATE_URL, dto.getCopyrightDate());
    map.put(PROJECT_PROVISION_DATE_URL, dto.getProjectProvisionDate());
    return commonMapper.toJson(map);
  }

}
