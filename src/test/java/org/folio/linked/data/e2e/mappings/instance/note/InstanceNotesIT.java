package org.folio.linked.data.e2e.mappings.instance.note;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

public class InstanceNotesIT extends PostResourceIT {
  @Autowired
  private ObjectMapper objectMapper;

  @Override
  protected String postPayload() {
    return """
      {
         "resource":{
            "http://bibfra.me/vocab/lite/Instance":{
               "profileId": 3,
               "http://bibfra.me/vocab/library/title":[
                  {
                     "http://bibfra.me/vocab/library/Title":{
                        "http://bibfra.me/vocab/library/mainTitle":[
                           "%s"
                        ]
                     }
                  }
               ],
               "_notes":[
                 { "type":[ "http://bibfra.me/vocab/lite/note" ], "value":[ "Note1" ] },
                 { "type":[ "http://bibfra.me/vocab/library/additionalPhysicalForm" ], "value":[ "Note2" ] },
                 { "type":[ "http://bibfra.me/vocab/library/descriptionSourceNote" ], "value":[ "Note3" ] },
                 { "type":[ "http://bibfra.me/vocab/library/exhibitionsNote" ], "value":[ "Note4" ] },
                 { "type":[ "http://bibfra.me/vocab/library/locationOfOtherArchivalMaterial" ], "value":[ "Note5" ] },
                 { "type":[ "http://bibfra.me/vocab/library/fundingInformation" ], "value":[ "Note6" ] },
                 { "type":[ "http://bibfra.me/vocab/library/issuanceNote" ], "value":[ "Note7" ] },
                 { "type":[ "http://bibfra.me/vocab/library/issuingBody" ], "value":[ "Note8" ] },
                 { "type":[ "http://bibfra.me/vocab/library/originalVersionNote" ], "value":[ "Note9" ] },
                 { "type":[ "http://bibfra.me/vocab/library/relatedParts" ], "value":[ "Note10" ] },
                 { "type":[ "http://bibfra.me/vocab/library/typeOfReport" ], "value":[ "Note11" ] },
                 { "type":[ "http://bibfra.me/vocab/library/reproductionNote" ], "value":[ "Note12" ] },
                 { "type":[ "http://bibfra.me/vocab/library/computerDataNote" ], "value":[ "Note13" ] },
                 { "type":[ "http://bibfra.me/vocab/library/withNote" ], "value":[ "Note14" ] },
                 { "type":[ "http://bibfra.me/vocab/library/accompanyingMaterial" ], "value":[ "Note15" ] },
                 { "type":[ "http://bibfra.me/vocab/library/biogdata" ], "value":[ "Note16" ] },
                 { "type":[ "http://bibfra.me/vocab/library/adminhist" ], "value":[ "Note17" ] },
                 { "type":[ "http://bibfra.me/vocab/library/physicalDescription" ], "value":[ "Note18" ] },
                 { "type":[ "http://bibfra.me/vocab/library/datesOfPublicationNote" ], "value":[ "Note19" ] }
               ]
            }
         }
      }"""
      .formatted("TEST: " + this.getClass().getSimpleName());
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    var responsePayload = apiResponse.andReturn().getResponse().getContentAsString();
    var notesResponse = objectMapper.readTree(responsePayload)
      .path("resource")
      .path("http://bibfra.me/vocab/lite/Instance")
      .path("_notes");

    var expectedNotes = Map.ofEntries(
      Map.entry("http://bibfra.me/vocab/lite/note", "Note1"),
      Map.entry("http://bibfra.me/vocab/library/additionalPhysicalForm", "Note2"),
      Map.entry("http://bibfra.me/vocab/library/descriptionSourceNote", "Note3"),
      Map.entry("http://bibfra.me/vocab/library/exhibitionsNote", "Note4"),
      Map.entry("http://bibfra.me/vocab/library/locationOfOtherArchivalMaterial", "Note5"),
      Map.entry("http://bibfra.me/vocab/library/fundingInformation", "Note6"),
      Map.entry("http://bibfra.me/vocab/library/issuanceNote", "Note7"),
      Map.entry("http://bibfra.me/vocab/library/issuingBody", "Note8"),
      Map.entry("http://bibfra.me/vocab/library/originalVersionNote", "Note9"),
      Map.entry("http://bibfra.me/vocab/library/relatedParts", "Note10"),
      Map.entry("http://bibfra.me/vocab/library/typeOfReport", "Note11"),
      Map.entry("http://bibfra.me/vocab/library/reproductionNote", "Note12"),
      Map.entry("http://bibfra.me/vocab/library/computerDataNote", "Note13"),
      Map.entry("http://bibfra.me/vocab/library/withNote", "Note14"),
      Map.entry("http://bibfra.me/vocab/library/accompanyingMaterial", "Note15"),
      Map.entry("http://bibfra.me/vocab/library/biogdata", "Note16"),
      Map.entry("http://bibfra.me/vocab/library/adminhist", "Note17"),
      Map.entry("http://bibfra.me/vocab/library/physicalDescription", "Note18"),
      Map.entry("http://bibfra.me/vocab/library/datesOfPublicationNote", "Note19")
    );

    var actualNotes = new HashMap<>();
    for (var note : notesResponse) {
      String type = note.get("type").get(0).asText();
      String value = note.get("value").get(0).asText();
      actualNotes.put(type, value);
    }

    assertThat(actualNotes).isEqualTo(expectedNotes);
  }

  @Override
  protected void validateGraph(Resource instance) {
    assertThat(getProperty(instance, "http://bibfra.me/vocab/lite/note")).isEqualTo("Note1");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/library/additionalPhysicalForm")).isEqualTo("Note2");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/library/descriptionSourceNote")).isEqualTo("Note3");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/library/exhibitionsNote")).isEqualTo("Note4");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/library/locationOfOtherArchivalMaterial")).isEqualTo("Note5");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/library/fundingInformation")).isEqualTo("Note6");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/library/issuanceNote")).isEqualTo("Note7");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/library/issuingBody")).isEqualTo("Note8");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/library/originalVersionNote")).isEqualTo("Note9");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/library/relatedParts")).isEqualTo("Note10");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/library/typeOfReport")).isEqualTo("Note11");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/library/reproductionNote")).isEqualTo("Note12");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/library/computerDataNote")).isEqualTo("Note13");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/library/withNote")).isEqualTo("Note14");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/library/accompanyingMaterial")).isEqualTo("Note15");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/library/biogdata")).isEqualTo("Note16");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/library/adminhist")).isEqualTo("Note17");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/library/physicalDescription")).isEqualTo("Note18");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/library/datesOfPublicationNote")).isEqualTo("Note19");
  }
}
