package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.identified;

import static org.folio.linked.data.util.BibframeConstants.IDENTIFIED_BY_PRED;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_ISBN;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_ISBN_URL;
import static org.folio.linked.data.util.BibframeConstants.QUALIFIER_URL;
import static org.folio.linked.data.util.BibframeConstants.STATUS_PRED;
import static org.folio.linked.data.util.BibframeConstants.STATUS_URL;
import static org.folio.linked.data.util.BibframeConstants.VALUE_URL;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Isbn;
import org.folio.linked.data.domain.dto.IsbnField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = IDENTIFIERS_ISBN, predicate = IDENTIFIED_BY_PRED, dtoClass = IsbnField.class)
public class IsbnMapperUnit implements InstanceSubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var isbn = coreMapper.readResourceDoc(source, Isbn.class);
    coreMapper.addMappedProperties(source, STATUS_PRED, isbn::addStatusItem);
    destination.addIdentifiedByItem(new IsbnField().isbn(isbn));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var isbn = ((IsbnField) dto).getIsbn();
    var resource = new Resource();
    resource.setLabel(IDENTIFIERS_ISBN_URL);
    resource.setType(resourceTypeService.get(IDENTIFIERS_ISBN));
    resource.setDoc(getDoc(isbn));
    coreMapper.mapPropertyEdges(isbn.getStatus(), resource, STATUS_PRED, STATUS_URL);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Isbn dto) {
    var map = new HashMap<String, List<String>>();
    map.put(VALUE_URL, dto.getValue());
    map.put(QUALIFIER_URL, dto.getQualifier());
    return coreMapper.toJson(map);
  }

}