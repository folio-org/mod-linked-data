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
import static org.folio.linked.data.util.BibframeConstants.TITLE_PRED;
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
public class MonographInstanceMapperUnit implements InnerResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;
  private final SubResourceMapper mapper;

  @Override
  public BibframeResponse toDto(Resource resource, BibframeResponse destination) {
    coreMapper.addMappedResources(mapper, resource, destination::addInstanceItem, Instance.class);
    return destination;
  }

  @Override
  public Resource toEntity(Object innerResourceDto) {
    Instance dto = (Instance) innerResourceDto;
    var resource = new Resource();
    resource.setLabel(INSTANCE_URL);
    resource.setType(resourceTypeService.get(INSTANCE));
    resource.setDoc(getDoc(dto));
    coreMapper.mapResourceEdges(dto.getTitle(), resource, null, TITLE_PRED, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getProvisionActivity(), resource, null, PROVISION_ACTIVITY_PRED, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getContribution(), resource, null, CONTRIBUTION_PRED, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getIdentifiedBy(), resource, null, IDENTIFIED_BY_PRED, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getNote(), resource, null, NOTE_PRED, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getSupplementaryContent(), resource, null, SUPP_CONTENT_PRED, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getImmediateAcquisition(), resource, null, IMM_ACQUISITION_PRED, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getExtent(), resource, null, EXTENT_PRED, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getElectronicLocator(), resource, null, ELECTRONIC_LOCATOR_PRED, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getIssuance(), resource, null, ISSUANCE_PRED, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getMedia(), resource, null, MEDIA_PRED, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getCarrier(), resource, null, CARRIER_PRED, mapper::toEntity);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Instance dto) {
    var map = new HashMap<String, List<String>>();
    map.put(DIMENSIONS_URL, dto.getDimensions());
    map.put(RESPONSIBILITY_STATEMENT_URL, dto.getResponsiblityStatement());
    map.put(EDITION_STATEMENT_URL, dto.getEditionStatement());
    map.put(COPYRIGHT_DATE_URL, dto.getCopyrightDate());
    map.put(PROJECT_PROVISION_DATE_URL, dto.getProjectProvisionDate());
    return coreMapper.toJson(map);
  }

}
