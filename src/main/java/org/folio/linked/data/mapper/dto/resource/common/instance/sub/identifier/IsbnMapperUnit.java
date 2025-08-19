package org.folio.linked.data.mapper.dto.resource.common.instance.sub.identifier;

import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;

import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.IdentifierFieldResponse;
import org.folio.linked.data.domain.dto.IdentifierRequest;
import org.folio.linked.data.domain.dto.IdentifierResponse;
import org.folio.linked.data.domain.dto.IsbnField;
import org.folio.linked.data.domain.dto.IsbnFieldResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = ID_ISBN, predicate = MAP, requestDto = IsbnField.class)
public class IsbnMapperUnit extends AbstractIdentifierMapperUnit {

  protected IsbnMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService);
  }

  @Override
  protected ResourceTypeDictionary getIdentifierType() {
    return ID_ISBN;
  }

  @Override
  protected IdentifierFieldResponse toFieldResponse(IdentifierResponse identifierResponse) {
    return new IsbnFieldResponse().isbn(identifierResponse);
  }

  @Override
  protected IdentifierRequest toIdentifierRequest(Object dto) {
    return ((IsbnField) dto).getIsbn();
  }
}
