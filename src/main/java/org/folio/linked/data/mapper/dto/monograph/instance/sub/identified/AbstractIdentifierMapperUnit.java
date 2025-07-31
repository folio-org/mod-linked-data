package org.folio.linked.data.mapper.dto.monograph.instance.sub.identified;

import static org.folio.ld.dictionary.PredicateDictionary.STATUS;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.QUALIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.IDENTIFIER;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.IdentifierFieldResponse;
import org.folio.linked.data.domain.dto.IdentifierRequest;
import org.folio.linked.data.domain.dto.IdentifierResponse;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.monograph.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;

abstract class AbstractIdentifierMapperUnit implements InstanceSubResourceMapperUnit {
  private final CoreMapper coreMapper;
  private final HashService hashService;

  protected AbstractIdentifierMapperUnit(CoreMapper coreMapper, HashService hashService) {
    this.coreMapper = coreMapper;
    this.hashService = hashService;
  }

  protected abstract ResourceTypeDictionary getIdentifierType();

  protected abstract IdentifierFieldResponse toFieldResponse(IdentifierResponse identifierResponse);

  protected abstract IdentifierRequest toIdentifierRequest(Object dto);

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof InstanceResponse instance) {
      var identifierResponse = getResponseDto(resourceToConvert);
      instance.addMapItem(toFieldResponse(identifierResponse));
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var identifierRequest = toIdentifierRequest(dto);
    var resource = new Resource();
    resource.setLabel(getFirstValue(identifierRequest::getValue));
    resource.addTypes(IDENTIFIER, getIdentifierType());
    resource.setDoc(getDoc(identifierRequest));
    coreMapper.addOutgoingEdges(resource, IdentifierRequest.class, identifierRequest.getStatus(), STATUS);
    resource.setId(hashService.hash(resource));
    return resource;
  }

  protected IdentifierResponse getResponseDto(Resource resourceToConvert) {
    var identifierResponse = coreMapper.toDtoWithEdges(resourceToConvert, IdentifierResponse.class, false);
    identifierResponse.setId(String.valueOf(resourceToConvert.getId()));
    return identifierResponse;
  }

  protected JsonNode getDoc(IdentifierRequest dto) {
    var properties = new HashMap<String, List<String>>();
    putProperty(properties, NAME, dto.getValue());
    putProperty(properties, QUALIFIER, dto.getQualifier());
    return properties.isEmpty() ? null : coreMapper.toJson(properties);
  }
}
