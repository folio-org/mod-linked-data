package org.folio.linked.data.mapper.resource.monograph.inner.common;

import static com.google.common.collect.Iterables.getFirst;
import static org.folio.linked.data.util.BibframeConstants.LABEL;
import static org.folio.linked.data.util.BibframeConstants.LINK;
import static org.folio.linked.data.util.BibframeConstants.STATUS;
import static org.folio.linked.data.util.BibframeConstants.STATUS_PRED;
import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED_FOR_PREDICATE;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;
import static org.folio.linked.data.util.Constants.RIGHT_SQUARE_BRACKET;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Isbn;
import org.folio.linked.data.domain.dto.Lccn;
import org.folio.linked.data.domain.dto.Status;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = STATUS, predicate = STATUS_PRED, dtoClass = Status.class)
public class StatusMapperUnit<T> implements SubResourceMapperUnit<T> {

  private static final Set<Class> SUPPORTED_PARENTS = Set.of(Lccn.class, Isbn.class);
  private final CoreMapper coreMapper;
  private final DictionaryService<ResourceType> resourceTypeService;

  @Override
  public T toDto(Resource source, T destination) {
    var status = coreMapper.readResourceDoc(source, Status.class);
    if (destination instanceof Lccn lccn) {
      lccn.addStatusItem(status);
    } else if (destination instanceof Isbn isbn) {
      isbn.addStatusItem(status);
    } else {
      throw new NotSupportedException(RESOURCE_TYPE + destination.getClass().getSimpleName()
        + IS_NOT_SUPPORTED_FOR_PREDICATE + STATUS_PRED + RIGHT_SQUARE_BRACKET);
    }
    return destination;
  }

  @Override
  public Set<Class> getParentDto() {
    return SUPPORTED_PARENTS;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var status = (Status) dto;
    var resource = new Resource();
    resource.setLabel(getFirst(status.getValue(), ""));
    resource.setType(resourceTypeService.get(STATUS));
    resource.setDoc(getDoc(status));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Status status) {
    var map = new HashMap<String, List<String>>();
    map.put(LABEL, status.getValue());
    map.put(LINK, status.getLink());
    return coreMapper.toJson(map);
  }

}
