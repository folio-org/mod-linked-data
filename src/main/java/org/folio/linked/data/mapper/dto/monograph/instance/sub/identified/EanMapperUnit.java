package org.folio.linked.data.mapper.dto.monograph.instance.sub.identified;

import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PropertyDictionary.EAN_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.QUALIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_EAN;
import static org.folio.linked.data.util.ResourceUtils.getPropertyValues;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.EanField;
import org.folio.linked.data.domain.dto.EanFieldResponse;
import org.folio.linked.data.domain.dto.IdentifierFieldResponse;
import org.folio.linked.data.domain.dto.IdentifierRequest;
import org.folio.linked.data.domain.dto.IdentifierResponse;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = ID_EAN, predicate = MAP, requestDto = EanField.class)
public class EanMapperUnit extends AbstractIdentifierMapperUnit {
  private final CoreMapper coreMapper;

  public EanMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService);
    this.coreMapper = coreMapper;
  }

  @Override
  protected IdentifierFieldResponse toFieldResponse(IdentifierResponse identifierResponse) {
    return new EanFieldResponse().ean(identifierResponse);
  }

  @Override
  protected IdentifierRequest toIdentifierRequest(Object dto) {
    return ((EanField) dto).getEan();
  }

  @Override
  protected ResourceTypeDictionary getIdentifierType() {
    return ID_EAN;
  }

  @Override
  protected IdentifierResponse getResponseDto(Resource resourceToConvert) {
    var identifierResponse = super.getResponseDto(resourceToConvert);
    identifierResponse.setValue(getPropertyValues(resourceToConvert, EAN_VALUE));
    return identifierResponse;
  }

  @Override
  protected JsonNode getDoc(IdentifierRequest dto) {
    var properties = new HashMap<String, List<String>>();
    putProperty(properties, EAN_VALUE, dto.getValue());
    putProperty(properties, QUALIFIER, dto.getQualifier());
    return properties.isEmpty() ? null : coreMapper.toJson(properties);
  }
}
