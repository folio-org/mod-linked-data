package org.folio.linked.data.mapper.dto.monograph.common;

import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.ResourceTypeDictionary.STATUS;
import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED_FOR_PREDICATE;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;
import static org.folio.linked.data.util.Constants.RIGHT_SQUARE_BRACKET;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.domain.dto.Classification;
import org.folio.linked.data.domain.dto.ClassificationResponse;
import org.folio.linked.data.domain.dto.IsbnRequest;
import org.folio.linked.data.domain.dto.IsbnResponse;
import org.folio.linked.data.domain.dto.LccnRequest;
import org.folio.linked.data.domain.dto.LccnResponse;
import org.folio.linked.data.domain.dto.Status;
import org.folio.linked.data.domain.dto.StatusResponse;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = STATUS, predicate = PredicateDictionary.STATUS, requestDto = Status.class)
public class StatusMapperUnit implements SingleResourceMapperUnit {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(
    LccnRequest.class,
    LccnResponse.class,
    IsbnRequest.class,
    IsbnResponse.class,
    Classification.class,
    ClassificationResponse.class
  );
  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    var status = coreMapper.toDtoWithEdges(resourceToConvert, StatusResponse.class, false);
    status.setId(String.valueOf(resourceToConvert.getId()));
    switch (parentDto) {
      case LccnResponse lccn -> lccn.addStatusItem(status);
      case IsbnResponse isbn -> isbn.addStatusItem(status);
      case ClassificationResponse classification -> classification.addStatusItem(status);
      default -> throw new NotSupportedException(RESOURCE_TYPE + parentDto.getClass().getSimpleName()
          + IS_NOT_SUPPORTED_FOR_PREDICATE + PredicateDictionary.STATUS.getUri() + RIGHT_SQUARE_BRACKET);
    }
    return parentDto;
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var status = (Status) dto;
    var resource = new Resource();
    resource.setLabel(getFirstValue(status::getValue));
    resource.addTypes(STATUS);
    resource.setDoc(getDoc(status));
    resource.setId(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Status dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, LINK, dto.getLink());
    putProperty(map, LABEL, dto.getValue());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
