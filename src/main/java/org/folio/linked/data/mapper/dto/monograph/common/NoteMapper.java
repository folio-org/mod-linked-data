package org.folio.linked.data.mapper.dto.monograph.common;

import static java.util.Optional.ofNullable;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.collections4.CollectionUtils.union;
import static org.folio.ld.dictionary.PropertyDictionary.fromValue;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.linked.data.domain.dto.Note;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {

  public List<Note> toNotes(JsonNode doc, Set<PropertyDictionary> noteProperties) {
    return ofNullable(doc)
      .stream()
      .map(JsonNode::properties)
      .flatMap(Collection::stream)
      .filter(entry -> {
        var property = fromValue(entry.getKey());
        return property.isPresent() && noteProperties.contains(property.get());
      })
      .flatMap(entry ->
        stream(spliteratorUnknownSize(entry.getValue().iterator(), ORDERED), false)
          .map(value -> new Note()
            .value(List.of(value.textValue()))
            .type(List.of(entry.getKey()))
          )
      )
      .toList();
  }

  public void putNotes(List<Note> noteDtos, Map<String, List<String>> map) {
    ofNullable(noteDtos)
      .ifPresent(notes -> notes
        .forEach(note -> note.getType()
          .forEach(type -> fromValue(type)
            .ifPresent(property -> {
              if (map.containsKey(property.getValue())) {
                putProperty(map, property, (List<String>) union(map.get(property.getValue()), note.getValue()));
              } else {
                putProperty(map, property, note.getValue());
              }
            }))));
  }
}
