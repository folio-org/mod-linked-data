package org.folio.linked.data.mapper.dto.resource.common.instance.sub;

import static org.folio.ld.dictionary.PredicateDictionary.ADMIN_METADATA;
import static org.folio.ld.dictionary.PredicateDictionary.CATALOGING_LANGUAGE;
import static org.folio.ld.dictionary.PropertyDictionary.CATALOGING_AGENCY;
import static org.folio.ld.dictionary.PropertyDictionary.CONTROL_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.CREATED_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.MODIFYING_AGENCY;
import static org.folio.ld.dictionary.PropertyDictionary.TRANSCRIBING_AGENCY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ANNOTATION;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.AdminMetadata;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ANNOTATION, predicate = ADMIN_METADATA, requestDto = AdminMetadata.class)
public class AdminMetadataMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof InstanceResponse instance) {
      var adminMetadata = coreMapper.toDtoWithEdges(resourceToConvert, AdminMetadata.class, false);
      instance.addAdminMetadataItem(adminMetadata);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var metadata = (AdminMetadata) dto;
    var resource = new Resource()
      .addTypes(ANNOTATION)
      .setLabel(getLabel(metadata, parentEntity))
      .setDoc(getDoc(metadata));

    coreMapper.addOutgoingEdges(resource, AdminMetadata.class, metadata.getCatalogingLanguage(), CATALOGING_LANGUAGE);

    resource.setIdAndRefreshEdges(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(AdminMetadata dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, CONTROL_NUMBER, dto.getControlNumber());
    putProperty(map, CREATED_DATE, dto.getCreatedDate());
    putProperty(map, CATALOGING_AGENCY, dto.getCatalogingAgency());
    putProperty(map, TRANSCRIBING_AGENCY, dto.getTranscribingAgency());
    putProperty(map, MODIFYING_AGENCY, dto.getModifyingAgency());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

  private String getLabel(AdminMetadata adminMetadata, Resource parentEntity) {
    var controlNumber = getFirstValue(adminMetadata::getControlNumber);
    return controlNumber.isEmpty()
      ? parentEntity.getLabel() + " - Administrative Metadata"
      : controlNumber;
  }
}
