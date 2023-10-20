package org.folio.linked.data.mapper.resource.monograph.inner.common;

import static org.folio.ld.dictionary.Property.LABEL;
import static org.folio.ld.dictionary.Property.LINK;
import static org.folio.ld.dictionary.ResourceTypeDictionary.STATUS;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;
import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED_FOR_PREDICATE;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;
import static org.folio.linked.data.util.Constants.RIGHT_SQUARE_BRACKET;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.domain.dto.Isbn;
import org.folio.linked.data.domain.dto.Lccn;
import org.folio.linked.data.domain.dto.Status;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = STATUS, predicate = PredicateDictionary.STATUS, dtoClass = Status.class)
public class StatusMapperUnit<T> implements SubResourceMapperUnit<T> {

  private static final Set<Class> SUPPORTED_PARENTS = Set.of(Lccn.class, Isbn.class);
  private final CoreMapper coreMapper;

  @Override
  public T toDto(Resource source, T destination) {
    var status = coreMapper.readResourceDoc(source, Status.class);
    status.setId(String.valueOf(source.getResourceHash()));
    if (destination instanceof Lccn lccn) {
      lccn.addStatusItem(status);
    } else if (destination instanceof Isbn isbn) {
      isbn.addStatusItem(status);
    } else {
      throw new NotSupportedException(RESOURCE_TYPE + destination.getClass().getSimpleName()
        + IS_NOT_SUPPORTED_FOR_PREDICATE + PredicateDictionary.STATUS.getUri() + RIGHT_SQUARE_BRACKET);
    }
    return destination;
  }

  @Override
  public Set<Class> getParentDto() {
    return SUPPORTED_PARENTS;
  }

  @Override
  public Resource toEntity(Object dto) {
    var status = (Status) dto;
    var resource = new Resource();
    resource.setLabel(getFirstValue(status::getValue));
    resource.addType(STATUS);
    resource.setDoc(getDoc(status));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Status status) {
    var map = new HashMap<String, List<String>>();
    map.put(LINK, status.getLink());
    map.put(LABEL, status.getValue());
    return coreMapper.toJson(map);
  }

}
