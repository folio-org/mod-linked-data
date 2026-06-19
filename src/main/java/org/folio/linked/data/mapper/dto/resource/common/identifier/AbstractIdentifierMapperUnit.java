package org.folio.linked.data.mapper.dto.resource.common.identifier;

import static org.folio.ld.dictionary.PredicateDictionary.STATUS;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.QUALIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.IDENTIFIER;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.AuthorityRequest;
import org.folio.linked.data.domain.dto.AuthorityResponse;
import org.folio.linked.data.domain.dto.IdentifierRequest;
import org.folio.linked.data.domain.dto.IdentifierResponse;
import org.folio.linked.data.domain.dto.InstanceRequest;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.MapResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.SingleResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import tools.jackson.databind.JsonNode;

abstract class AbstractIdentifierMapperUnit implements SingleResourceMapperUnit {
  private final CoreMapper coreMapper;
  private final HashService hashService;

  protected AbstractIdentifierMapperUnit(CoreMapper coreMapper, HashService hashService) {
    this.coreMapper = coreMapper;
    this.hashService = hashService;
  }

  protected abstract ResourceTypeDictionary getIdentifierType();

  protected abstract MapResponse toFieldResponse(IdentifierResponse identifierResponse);

  protected abstract IdentifierRequest toIdentifierRequest(Object dto);

  @Override
  public Set<Class<?>> supportedParents() {
    return Set.of(InstanceRequest.class, InstanceResponse.class, AuthorityRequest.class, AuthorityResponse.class);
  }

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, SingleResourceMapperUnit.ResourceMappingContext context) {
    if (parentDto instanceof InstanceResponse instance) {
      var identifierResponse = getResponseDto(resourceToConvert);
      instance.addMapItem(toFieldResponse(identifierResponse));
    } else if (parentDto instanceof AuthorityResponse authority) {
      var identifierResponse = getResponseDto(resourceToConvert);
      authority.addMapItem(toFieldResponse(identifierResponse));
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
    resource.setIdAndRefreshEdges(hashService.hash(resource));
    return resource;
  }

  private IdentifierResponse getResponseDto(Resource resourceToConvert) {
    var identifierResponse = coreMapper.toDtoWithEdges(resourceToConvert, IdentifierResponse.class, false);
    identifierResponse.setId(String.valueOf(resourceToConvert.getId()));
    return identifierResponse;
  }

  private JsonNode getDoc(IdentifierRequest dto) {
    var properties = new HashMap<String, List<String>>();
    putProperty(properties, NAME, dto.getValue());
    putProperty(properties, QUALIFIER, dto.getQualifier());
    return properties.isEmpty() ? null : coreMapper.toJson(properties);
  }
}
