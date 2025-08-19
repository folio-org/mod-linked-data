package org.folio.linked.data.mapper.dto.resource.common.instance.sub.identifier;

import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_IAN;

import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.IanField;
import org.folio.linked.data.domain.dto.IanFieldResponse;
import org.folio.linked.data.domain.dto.IdentifierFieldResponse;
import org.folio.linked.data.domain.dto.IdentifierRequest;
import org.folio.linked.data.domain.dto.IdentifierResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = ID_IAN, predicate = MAP, requestDto = IanField.class)
public class IanMapperUnit extends AbstractIdentifierMapperUnit {

  public IanMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService);
  }

  @Override
  protected IdentifierFieldResponse toFieldResponse(IdentifierResponse identifierResponse) {
    return new IanFieldResponse().ian(identifierResponse);
  }

  @Override
  protected IdentifierRequest toIdentifierRequest(Object dto) {
    return ((IanField) dto).getIan();
  }

  @Override
  protected ResourceTypeDictionary getIdentifierType() {
    return ID_IAN;
  }
}
