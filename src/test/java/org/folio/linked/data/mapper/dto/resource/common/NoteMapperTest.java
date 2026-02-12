package org.folio.linked.data.mapper.dto.resource.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PropertyDictionary.DIMENSIONS;
import static org.folio.ld.dictionary.PropertyDictionary.ISSUANCE_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.WITH_NOTE;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.folio.linked.data.domain.dto.Note;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.JsonNode;

@UnitTest
class NoteMapperTest {
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
          createNote(List.of(ISSUANCE_NOTE.getValue()), "issuance note"),
          createNote(List.of(ISSUANCE_NOTE.getValue()), "another issuance note"),
          createNote(List.of(NOTE.getValue()), "general note"))
      ),
      Arguments.of(
        TEST_JSON_MAPPER.convertValue(Map.of(), JsonNode.class),
        List.of()
      )
    );
  }

  @ParameterizedTest
  @MethodSource("provideDocAndExpectedNotes")
  void toNotes_shouldReturnNoteDtosOfSpecifiedTypes(JsonNode doc, List<Note> expectedNotes) {
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
  void putNotes_shouldAddNotesToMap(List<Note> notes, Map<String, List<String>> expectedMap) {
    //given
    var map = new HashMap<String, List<String>>();

    //when
    noteMapper.putNotes(notes, map);

    //then
    assertThat(map).isEqualTo(expectedMap);
  }

  private static JsonNode createDocWithoutNotes() {
    var map = new HashMap<String, List<String>>();
    putProperty(map, DIMENSIONS, List.of("dimensions"));
    return TEST_JSON_MAPPER.convertValue(map, JsonNode.class);
  }

  private static JsonNode createDocWithNotes() {
    var map = new HashMap<String, List<String>>();
    putProperty(map, DIMENSIONS, List.of("dimensions"));
    putProperty(map, NOTE, List.of("general note"));
    putProperty(map, ISSUANCE_NOTE, List.of("issuance note", "another issuance note"));
    return TEST_JSON_MAPPER.convertValue(map, JsonNode.class);
  }

  private static Note createNote(List<String> types, String value) {
    var note = new Note();
    note.setType(types);
    note.setValue(List.of(value));
    return note;
  }
}
