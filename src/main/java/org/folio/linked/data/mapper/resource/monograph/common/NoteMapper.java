package org.folio.linked.data.mapper.resource.monograph.common;

import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PropertyDictionary.fromValue;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.linked.data.domain.dto.NoteDto;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {

  public List<NoteDto> toNotes(JsonNode doc, Set<PropertyDictionary> noteProperties) {
    return doc.properties().stream()
      .filter(entry -> {
        var property = fromValue(entry.getKey());
        return property.isPresent() && noteProperties.contains(property.get());
      })
      .map(entry -> {
        var noteDto = new NoteDto();
        noteDto.setValue(List.of(entry.getValue().get(0).textValue()));
        noteDto.setType(List.of(entry.getKey()));
        return noteDto;
      })
      .toList();
  }

  public void putNotes(List<NoteDto> noteDtos, Map<String, List<String>> map) {
    ofNullable(noteDtos)
      .ifPresent(notes -> notes.forEach(note -> fromValue(note.getType().get(0))
        .ifPresent(property -> putProperty(map, property, note.getValue()))));
  }
}
