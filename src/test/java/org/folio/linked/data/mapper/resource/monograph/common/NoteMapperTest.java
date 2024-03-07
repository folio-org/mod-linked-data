package org.folio.linked.data.mapper.resource.monograph.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PropertyDictionary.DIMENSIONS;
import static org.folio.ld.dictionary.PropertyDictionary.EXTENT;
import static org.folio.ld.dictionary.PropertyDictionary.ISSUANCE_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.WITH_NOTE;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.folio.linked.data.domain.dto.NoteDto;
import org.folio.linked.data.mapper.dto.monograph.common.NoteMapper;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@UnitTest
class NoteMapperTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final NoteMapper noteMapper = new NoteMapper();

  private static Stream<Arguments> provideDocAndExpectedNotes() {
    return Stream.of(
      Arguments.of(
        createDocWithoutNotes(),
        List.of()
      ),
      Arguments.of(
        createDocWithNotes(),
        List.of(
          createNote(List.of(NOTE.getValue()), "general note"),
          createNote(List.of(ISSUANCE_NOTE.getValue()), "issuance note"),
          createNote(List.of(ISSUANCE_NOTE.getValue()), "another issuance note"))
      ),
      Arguments.of(
        OBJECT_MAPPER.convertValue(Map.of(), JsonNode.class),
        List.of()
      )
    );
  }

  @ParameterizedTest
  @MethodSource("provideDocAndExpectedNotes")
  void toNotes_shouldReturnNoteDtosOfSpecifiedTypes(JsonNode doc, List<NoteDto> expectedNotes) {
    //given
    var noteTypes = Set.of(NOTE, ISSUANCE_NOTE);

    //when
    var notes = noteMapper.toNotes(doc, noteTypes);

    //then
    assertThat(notes).isEqualTo(expectedNotes);
  }

  private static Stream<Arguments> provideNotesAndExpectedMap() {
    return Stream.of(
      Arguments.of(
        List.of(
          createNote(List.of(NOTE.getValue(), WITH_NOTE.getValue()), "note"),
          createNote(List.of(ISSUANCE_NOTE.getValue()), "issuance note"),
          createNote(List.of(ISSUANCE_NOTE.getValue()), "another issuance note")),
        Map.of(
          NOTE.getValue(), List.of("note"),
          WITH_NOTE.getValue(), List.of("note"),
          ISSUANCE_NOTE.getValue(), List.of("issuance note", "another issuance note")
        )
      ),
      Arguments.of(
        List.of(createNote(List.of("invalid type"), "invalid note")),
        Map.of()
      ),
      Arguments.of(
        null,
        Map.of()
      ),
      Arguments.of(
        List.of(),
        Map.of()
      )
    );
  }

  @ParameterizedTest
  @MethodSource("provideNotesAndExpectedMap")
  void putNotes_shouldAddNotesToMap(List<NoteDto> notes, Map<String, List<String>> expectedMap) {
    //given
    var map = new HashMap<String, List<String>>();

    //when
    noteMapper.putNotes(notes, map);

    //then
    assertThat(map).isEqualTo(expectedMap);
  }

  private static JsonNode createDocWithoutNotes() {
    var map = new HashMap<String, List<String>>();
    putProperty(map, EXTENT, List.of("extent"));
    putProperty(map, DIMENSIONS, List.of("dimensions"));
    return OBJECT_MAPPER.convertValue(map, JsonNode.class);
  }

  private static JsonNode createDocWithNotes() {
    var map = new HashMap<String, List<String>>();
    putProperty(map, EXTENT, List.of("extent"));
    putProperty(map, DIMENSIONS, List.of("dimensions"));
    putProperty(map, NOTE, List.of("general note"));
    putProperty(map, ISSUANCE_NOTE, List.of("issuance note", "another issuance note"));
    return OBJECT_MAPPER.convertValue(map, JsonNode.class);
  }

  private static NoteDto createNote(List<String> types, String value) {
    var note = new NoteDto();
    note.setType(types);
    note.setValue(List.of(value));
    return note;
  }
}
