package org.folio.linked.data.mapper.dto.monograph.instance;

import static java.util.stream.StreamSupport.stream;
import static org.folio.ld.dictionary.PropertyDictionary.BOOK_FORMAT;
import static org.folio.linked.data.util.JsonUtils.getProperty;
import static org.springframework.util.ObjectUtils.isEmpty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.folio.linked.data.domain.dto.Category;
import org.folio.linked.data.domain.dto.CategoryResponse;
import org.springframework.stereotype.Component;

@Component
class TextBookFormatConverter {

  /**
   * Converts Text book formats in the graph to CategoryResponse objects for rendering in UI.
   */
  List<CategoryResponse> convertTextToCategories(JsonNode doc) {
    return getProperty(doc, BOOK_FORMAT.getValue())
      .filter(JsonNode::isArray)
      .map(this::getCategories)
      .orElse(List.of());
  }

  /**
   * Non-standard book formats will not have a 'link' property in the API request. Convert such book formats in to Text
   * for storing them as text properties of the Instance node.
   */
  List<String> convertCategoriesToText(List<Category> bookFormats) {
    return bookFormats.stream()
      .filter(category -> isEmpty(category.getLink()))
      .flatMap(format -> format.getTerm().stream())
      .toList();
  }

  private List<CategoryResponse> getCategories(JsonNode arr) {
    return stream(arr.spliterator(), false)
      .map(el -> new CategoryResponse().term(List.of(el.asText())))
      .toList();
  }
}
