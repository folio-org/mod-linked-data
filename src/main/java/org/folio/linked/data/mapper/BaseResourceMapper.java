package org.folio.linked.data.mapper;

import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.BibframeConstants.SAME_AS_PRED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Lookup;
import org.folio.linked.data.domain.dto.Person;
import org.folio.linked.data.domain.dto.PersonField;
import org.folio.linked.data.domain.dto.Property;
import org.folio.linked.data.domain.dto.Url;
import org.folio.linked.data.domain.dto.UrlField;
import org.folio.linked.data.exception.JsonException;
import org.folio.linked.data.model.entity.Resource;

@RequiredArgsConstructor
public abstract class BaseResourceMapper<T> {

  protected final Map<String, BiConsumer<Resource, T>> pred2Action = initPred2Action();
  private final ObjectMapper mapper;

  public abstract T map(Resource resource);

  protected T map(Resource resource, Class<T> resourceClass) {
    return toDto(resource, resourceClass, pred2Action);
  }

  protected abstract Map<String, BiConsumer<Resource, T>> initPred2Action();

  protected UrlField toElectronicLocator(Resource electronicLocator) {
    var electronicLocDto = new UrlField();
    electronicLocDto.setUrl(toDto(electronicLocator, Url.class, Map.of(
        NOTE_PRED, (target, dto) -> dto.addNoteItem(toProperty(target)))));
    return electronicLocDto;
  }

  protected PersonField toContributionPerson(Resource person) {
    return new PersonField().person(toDto(
      person, Person.class, Map.of(SAME_AS_PRED, (target, dto) -> dto.addSameAsItem(toLookupProperty(target)))));
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

  protected Lookup toLookupProperty(Resource resource) {
    return readResourceDoc(resource, Lookup.class);
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
