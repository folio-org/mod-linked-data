package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.identified;

import static com.google.common.collect.Iterables.getFirst;
import static org.folio.linked.data.util.BibframeConstants.ISBN;
import static org.folio.linked.data.util.BibframeConstants.MAP_PRED;
import static org.folio.linked.data.util.BibframeConstants.NAME;
import static org.folio.linked.data.util.BibframeConstants.QUALIFIER;
import static org.folio.linked.data.util.BibframeConstants.STATUS;
import static org.folio.linked.data.util.BibframeConstants.STATUS_PRED;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Isbn;
import org.folio.linked.data.domain.dto.IsbnField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.common.StatusMapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ISBN, predicate = MAP_PRED, dtoClass = IsbnField.class)
public class IsbnMapperUnit implements InstanceSubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;
  private final StatusMapperUnit<Isbn> statusMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var isbn = coreMapper.readResourceDoc(source, Isbn.class);
    coreMapper.addMappedResources(statusMapper, source, STATUS_PRED, isbn);
    destination.addMapItem(new IsbnField().isbn(isbn));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var isbn = ((IsbnField) dto).getIsbn();
    var resource = new Resource();
    resource.setLabel(getFirst(isbn.getValue(), ""));
    resource.setType(resourceTypeService.get(ISBN));
    resource.setDoc(getDoc(isbn));
    coreMapper.mapResourceEdges(isbn.getStatus(), resource, STATUS, STATUS_PRED,
      (status, pred) -> statusMapper.toEntity(status, pred, null));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Isbn dto) {
    var map = new HashMap<String, List<String>>();
    map.put(NAME, dto.getValue());
    map.put(QUALIFIER, dto.getQualifier());
    return coreMapper.toJson(map);
  }
}
