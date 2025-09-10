package org.folio.linked.data.e2e.mappings.note;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

public class WorkNotesIT extends PostResourceIT {

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  protected String postPayload() {
    return """
      {
         "resource":{
            "http://bibfra.me/vocab/lite/Work":{
               "profileId": 2,
               "http://bibfra.me/vocab/library/title":[
                 {
                  "http://bibfra.me/vocab/library/Title":{
                     "http://bibfra.me/vocab/library/mainTitle":[ "%s" ]
                  }
                 }
               ],
               "_notes":[
                  { "type":[ "http://bibfra.me/vocab/lite/note" ], "value":[ "Note1" ] },
                  { "type":[ "http://bibfra.me/vocab/library/awardsNote" ], "value":[ "Note2" ] },
                  {
                    "type":[ "http://bibfra.me/vocab/library/awardsNote", "http://bibfra.me/vocab/library/bibliographyNote" ],
                    "value":[ "Note3" ]
                  },
                  {
                    "type":[ "http://bibfra.me/vocab/library/languageNote", "http://bibfra.me/vocab/library/bibliographyNote" ],
                    "value":[ "Note4" ]
                  }
               ]
            }
         }
      }"""
      .formatted("TEST: " + this.getClass().getSimpleName());
  }

  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    var expectedNotes = Map.of(
      "Note1", Set.of("http://bibfra.me/vocab/lite/note"),
      "Note2", Set.of("http://bibfra.me/vocab/library/awardsNote"),
      "Note3", Set.of("http://bibfra.me/vocab/library/awardsNote", "http://bibfra.me/vocab/library/bibliographyNote"),
      "Note4", Set.of("http://bibfra.me/vocab/library/languageNote", "http://bibfra.me/vocab/library/bibliographyNote")
    );

    var responsePayload = apiResponse.andReturn().getResponse().getContentAsString();
    var notes = objectMapper.readTree(responsePayload)
      .path("resource")
      .path("http://bibfra.me/vocab/lite/Work")
      .path("_notes");

    Map<String, Set<String>> actualNotes = new HashMap<>();
    notes.forEach(note -> {
      var value = note.path("value").get(0).asText();
      note.path("type").forEach(typeNode ->
        actualNotes.computeIfAbsent(value, k -> new HashSet<>()).add(typeNode.asText())
      );
    });

    assertThat(actualNotes).isEqualTo(expectedNotes);
  }

  @Override
  protected void validateGraph(Resource work) {
    assertThat(getProperty(work, "http://bibfra.me/vocab/lite/note"))
      .isEqualTo("Note1");
    assertThat(getProperties(work, "http://bibfra.me/vocab/library/awardsNote"))
      .isEqualTo(Set.of("Note2", "Note3"));
    assertThat(getProperties(work, "http://bibfra.me/vocab/library/bibliographyNote"))
      .isEqualTo(Set.of("Note3", "Note4"));
    assertThat(getProperty(work, "http://bibfra.me/vocab/library/languageNote"))
      .isEqualTo("Note4");
  }
}
