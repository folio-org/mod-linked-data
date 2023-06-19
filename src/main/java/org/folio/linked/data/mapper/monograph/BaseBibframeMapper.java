package org.folio.linked.data.mapper.monograph;

import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Property;
import org.folio.linked.data.domain.dto.Url;
import org.folio.linked.data.domain.dto.UrlField;
import org.folio.linked.data.exception.JsonException;
import org.folio.linked.data.model.entity.Resource;

@RequiredArgsConstructor
public abstract class BaseBibframeMapper {

  private final ObjectMapper mapper;

  protected UrlField toElectronicLocator(Resource electronicLocator) {
    var electronicLocDto = new UrlField();
    electronicLocDto.setUrl(toDto(electronicLocator, Url.class, Map.of(
        NOTE_PRED, (target, dto) -> dto.addNoteItem(toProperty(target)))));
    return electronicLocDto;
  }

  protected  <T> T readResourceDoc(Resource resource, Class<T> dtoClass) {
    try {
      var node = resource.getDoc() != null ? resource.getDoc() : mapper.createObjectNode();
      return mapper.treeToValue(node, dtoClass);
    } catch (JsonProcessingException e) {
      throw new JsonException(e.getMessage());
    }
  }

  protected Property toProperty(Resource resource) {
    return readResourceDoc(resource, Property.class);
  }

  protected <T> T toDto(Resource resource, Class<T> dtoClass,
                      Map<String, BiConsumer<Resource, T>> predicate2action) {
    var dto = readResourceDoc(resource, dtoClass);
    for (var re : resource.getOutgoingEdges()) {
      var predicate = re.getPredicate().getLabel();
      var action = predicate2action.get(predicate);
      if (action != null) {
        action.accept(re.getTarget(), dto);
      }
    }
    return dto;
  }
}
