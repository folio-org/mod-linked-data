package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.identified;

import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.STATUS;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.QUALIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Isbn;
import org.folio.linked.data.domain.dto.IsbnField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.common.StatusMapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ID_ISBN, predicate = MAP, dtoClass = IsbnField.class)
public class IsbnMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final StatusMapperUnit<Isbn> statusMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var isbn = coreMapper.readResourceDoc(source, Isbn.class);
    isbn.setId(String.valueOf(source.getResourceHash()));
    coreMapper.addMappedResources(statusMapper, source, STATUS, isbn);
    destination.addMapItem(new IsbnField().isbn(isbn));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto) {
    var isbn = ((IsbnField) dto).getIsbn();
    var resource = new Resource();
    resource.setLabel(getFirstValue(isbn::getValue));
    resource.addType(ID_ISBN);
    resource.setDoc(getDoc(isbn));
    coreMapper.mapSubEdges(isbn.getStatus(), resource, STATUS, statusMapper::toEntity);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Isbn dto) {
    var map = new HashMap<String, List<String>>();
    map.put(NAME.getValue(), dto.getValue());
    map.put(QUALIFIER.getValue(), dto.getQualifier());
    return coreMapper.toJson(map);
  }
}
