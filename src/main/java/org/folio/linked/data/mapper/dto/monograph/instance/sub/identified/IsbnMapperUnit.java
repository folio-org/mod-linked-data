package org.folio.linked.data.mapper.dto.monograph.instance.sub.identified;

import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.STATUS;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.QUALIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.IDENTIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.Isbn;
import org.folio.linked.data.domain.dto.IsbnField;
import org.folio.linked.data.domain.dto.IsbnFieldResponse;
import org.folio.linked.data.domain.dto.IsbnResponse;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ID_ISBN, predicate = MAP, requestDto = IsbnField.class)
public class IsbnMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    if (parentDto instanceof InstanceResponse instance) {
      var isbn = coreMapper.toDtoWithEdges(source, IsbnResponse.class, false);
      isbn.setId(String.valueOf(source.getId()));
      instance.addMapItem(new IsbnFieldResponse().isbn(isbn));
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var isbn = ((IsbnField) dto).getIsbn();
    var resource = new Resource();
    resource.setLabel(getFirstValue(isbn::getValue));
    resource.addTypes(IDENTIFIER, ID_ISBN);
    resource.setDoc(getDoc(isbn));
    coreMapper.addOutgoingEdges(resource, Isbn.class, isbn.getStatus(), STATUS);
    resource.setId(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Isbn dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, NAME, dto.getValue());
    putProperty(map, QUALIFIER, dto.getQualifier());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
