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
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_2;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_2_PRED;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_URL;
import static org.folio.linked.data.util.BibframeConstants.ISSUANCE_PRED;
import static org.folio.linked.data.util.BibframeConstants.MEDIA_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PROJECTED_PROVISION_DATE_URL;
import static org.folio.linked.data.util.BibframeConstants.PROVISION_ACTIVITY_PRED;
import static org.folio.linked.data.util.BibframeConstants.RESPONSIBILITY_STATEMENT_URL;
import static org.folio.linked.data.util.BibframeConstants.SUPP_CONTENT_PRED;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Bibframe2Response;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.Instance2;
import org.folio.linked.data.domain.dto.Instance2TitleInner;
import org.folio.linked.data.domain.dto.InstanceTitleField2;
import org.folio.linked.data.domain.dto.ParallelTitleField2;
import org.folio.linked.data.domain.dto.VariantTitleField2;
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
@MapperUnit(type = INSTANCE_URL)
public class MonographInstance2MapperUnit implements InnerResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;
  private final SubResourceMapper mapper;

  @Override
  public BibframeResponse toDto(Resource source, BibframeResponse destination) {
    return destination;
  }

  @Override
  public Bibframe2Response toDto(Resource resource, Bibframe2Response destination) {
    coreMapper.mapWithResources(mapper, resource, destination::addInstanceItem, Instance2.class);
    return destination;
  }

  @Override
  public Resource toEntity(Object innerResourceDto) {
    Instance2 dto = (Instance2) innerResourceDto;
    var resource = new Resource();
    resource.setType(resourceTypeService.get(INSTANCE_2));
    resource.setDoc(getDoc(dto));
    resource.setLabel(getLabel(dto));
    coreMapper.mapResourceEdges(dto.getTitle(), resource, INSTANCE_TITLE_2_PRED, Instance2.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getProvisionActivity(), resource, PROVISION_ACTIVITY_PRED, Instance2.class,
      mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getContribution(), resource, CONTRIBUTION_PRED, Instance2.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getIdentifiedBy(), resource, IDENTIFIED_BY_PRED, Instance2.class,
      mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getNote(), resource, NOTE_PRED, Instance2.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getSupplementaryContent(), resource, SUPP_CONTENT_PRED, Instance2.class,
      mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getImmediateAcquisition(), resource, IMM_ACQUISITION_PRED, Instance2.class,
      mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getExtent(), resource, EXTENT_PRED, Instance2.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getElectronicLocator(), resource, ELECTRONIC_LOCATOR_PRED, Instance2.class,
      mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getIssuance(), resource, ISSUANCE_PRED, Instance2.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getMedia(), resource, MEDIA_PRED, Instance2.class, mapper::toEntity);
    coreMapper.mapResourceEdges(dto.getCarrier(), resource, CARRIER_PRED, Instance2.class, mapper::toEntity);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private String getLabel(Instance2 dto) {
    var emptyString = "";
    var title = getTitle(dto, InstanceTitleField2.class);
    if (!Objects.isNull(title)) {
      return title.getInstanceTitle().getMainTitle().stream().findFirst().orElse(emptyString);
    }

    var parTitle = getTitle(dto, ParallelTitleField2.class);
    if (parTitle != null) {
      return parTitle.getParallelTitle().getMainTitle().stream().findFirst().orElse(emptyString);
    }

    var varTitle = getTitle(dto, VariantTitleField2.class);
    if (varTitle != null) {
      return varTitle.getVariantTitle().getMainTitle().stream().findFirst().orElse(emptyString);
    }

    return emptyString;
  }

  private <T extends Instance2TitleInner> T getTitle(Instance2 dto, Class<T> titleClass) {
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

  private JsonNode getDoc(Instance2 dto) {
    var map = new HashMap<String, List<String>>();
    map.put(DIMENSIONS_URL, dto.getDimensions());
    map.put(RESPONSIBILITY_STATEMENT_URL, dto.getResponsibilityStatement());
    map.put(EDITION_STATEMENT_URL, dto.getEditionStatement());
    map.put(COPYRIGHT_DATE_URL, dto.getCopyrightDate());
    map.put(PROJECTED_PROVISION_DATE_URL, dto.getProjectProvisionDate());
    return coreMapper.toJson(map);
  }

}
