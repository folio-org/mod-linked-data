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
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_PRED;
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
    resource.setType(resourceTypeService.get(INSTANCE));
    resource.setDoc(getDoc(dto));
    resource.setLabel(getLabel(dto));
    coreMapper.mapResourceEdges(dto.getTitle(), resource, INSTANCE_TITLE_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getProvisionActivity(), resource, PROVISION_ACTIVITY_PRED, Instance.class,
      mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getContribution(), resource, CONTRIBUTION_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getIdentifiedBy(), resource, IDENTIFIED_BY_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getNote(), resource, NOTE_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getSupplementaryContent(), resource, SUPP_CONTENT_PRED, Instance.class,
      mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getImmediateAcquisition(), resource, IMM_ACQUISITION_PRED, Instance.class,
      mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getExtent(), resource, EXTENT_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getElectronicLocator(), resource, ELECTRONIC_LOCATOR_PRED, Instance.class,
      mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getIssuance(), resource, ISSUANCE_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getMedia(), resource, MEDIA_PRED, Instance.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getCarrier(), resource, CARRIER_PRED, Instance.class, mapper::toEntity);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private String getLabel(Instance dto) {
    var emptyString = "";
    var title = getTitle(dto, InstanceTitleField.class);
    if (title != null) {
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
    if (dto.getTitle() == null) {
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
    map.put(DIMENSIONS_URL, dto.getDimensions());
    map.put(RESPONSIBILITY_STATEMENT_URL, dto.getResponsiblityStatement());
    map.put(EDITION_STATEMENT_URL, dto.getEditionStatement());
    map.put(COPYRIGHT_DATE_URL, dto.getCopyrightDate());
    map.put(PROJECT_PROVISION_DATE_URL, dto.getProjectProvisionDate());
    return coreMapper.toJson(map);
  }

}
