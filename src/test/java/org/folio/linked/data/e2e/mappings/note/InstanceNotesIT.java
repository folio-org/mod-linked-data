package org.folio.linked.data.e2e.mappings.note;

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
               "http://bibfra.me/vocab/marc/title":[
                  {
                     "http://bibfra.me/vocab/marc/Title":{
                        "http://bibfra.me/vocab/marc/mainTitle":[
                           "%s"
                        ]
                     }
                  }
               ],
               "_notes":[
                 { "type":[ "http://bibfra.me/vocab/lite/note" ], "value":[ "Note1" ] },
                 { "type":[ "http://bibfra.me/vocab/marc/additionalPhysicalForm" ], "value":[ "Note2" ] },
                 { "type":[ "http://bibfra.me/vocab/marc/descriptionSourceNote" ], "value":[ "Note3" ] },
                 { "type":[ "http://bibfra.me/vocab/marc/exhibitionsNote" ], "value":[ "Note4" ] },
                 { "type":[ "http://bibfra.me/vocab/marc/locationOfOtherArchivalMaterial" ], "value":[ "Note5" ] },
                 { "type":[ "http://bibfra.me/vocab/marc/fundingInformation" ], "value":[ "Note6" ] },
                 { "type":[ "http://bibfra.me/vocab/marc/issuanceNote" ], "value":[ "Note7" ] },
                 { "type":[ "http://bibfra.me/vocab/marc/issuingBody" ], "value":[ "Note8" ] },
                 { "type":[ "http://bibfra.me/vocab/marc/originalVersionNote" ], "value":[ "Note9" ] },
                 { "type":[ "http://bibfra.me/vocab/marc/relatedParts" ], "value":[ "Note10" ] },
                 { "type":[ "http://bibfra.me/vocab/marc/typeOfReport" ], "value":[ "Note11" ] },
                 { "type":[ "http://bibfra.me/vocab/marc/reproductionNote" ], "value":[ "Note12" ] },
                 { "type":[ "http://bibfra.me/vocab/marc/computerDataNote" ], "value":[ "Note13" ] },
                 { "type":[ "http://bibfra.me/vocab/marc/withNote" ], "value":[ "Note14" ] },
                 { "type":[ "http://bibfra.me/vocab/marc/accompanyingMaterial" ], "value":[ "Note15" ] },
                 { "type":[ "http://bibfra.me/vocab/marc/biogdata" ], "value":[ "Note16" ] },
                 { "type":[ "http://bibfra.me/vocab/marc/adminhist" ], "value":[ "Note17" ] },
                 { "type":[ "http://bibfra.me/vocab/marc/physicalDescription" ], "value":[ "Note18" ] },
                 { "type":[ "http://bibfra.me/vocab/marc/datesOfPublicationNote" ], "value":[ "Note19" ] }
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
      Map.entry("http://bibfra.me/vocab/marc/additionalPhysicalForm", "Note2"),
      Map.entry("http://bibfra.me/vocab/marc/descriptionSourceNote", "Note3"),
      Map.entry("http://bibfra.me/vocab/marc/exhibitionsNote", "Note4"),
      Map.entry("http://bibfra.me/vocab/marc/locationOfOtherArchivalMaterial", "Note5"),
      Map.entry("http://bibfra.me/vocab/marc/fundingInformation", "Note6"),
      Map.entry("http://bibfra.me/vocab/marc/issuanceNote", "Note7"),
      Map.entry("http://bibfra.me/vocab/marc/issuingBody", "Note8"),
      Map.entry("http://bibfra.me/vocab/marc/originalVersionNote", "Note9"),
      Map.entry("http://bibfra.me/vocab/marc/relatedParts", "Note10"),
      Map.entry("http://bibfra.me/vocab/marc/typeOfReport", "Note11"),
      Map.entry("http://bibfra.me/vocab/marc/reproductionNote", "Note12"),
      Map.entry("http://bibfra.me/vocab/marc/computerDataNote", "Note13"),
      Map.entry("http://bibfra.me/vocab/marc/withNote", "Note14"),
      Map.entry("http://bibfra.me/vocab/marc/accompanyingMaterial", "Note15"),
      Map.entry("http://bibfra.me/vocab/marc/biogdata", "Note16"),
      Map.entry("http://bibfra.me/vocab/marc/adminhist", "Note17"),
      Map.entry("http://bibfra.me/vocab/marc/physicalDescription", "Note18"),
      Map.entry("http://bibfra.me/vocab/marc/datesOfPublicationNote", "Note19")
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
    assertThat(getProperty(instance, "http://bibfra.me/vocab/marc/additionalPhysicalForm")).isEqualTo("Note2");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/marc/descriptionSourceNote")).isEqualTo("Note3");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/marc/exhibitionsNote")).isEqualTo("Note4");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/marc/locationOfOtherArchivalMaterial")).isEqualTo("Note5");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/marc/fundingInformation")).isEqualTo("Note6");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/marc/issuanceNote")).isEqualTo("Note7");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/marc/issuingBody")).isEqualTo("Note8");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/marc/originalVersionNote")).isEqualTo("Note9");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/marc/relatedParts")).isEqualTo("Note10");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/marc/typeOfReport")).isEqualTo("Note11");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/marc/reproductionNote")).isEqualTo("Note12");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/marc/computerDataNote")).isEqualTo("Note13");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/marc/withNote")).isEqualTo("Note14");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/marc/accompanyingMaterial")).isEqualTo("Note15");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/marc/biogdata")).isEqualTo("Note16");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/marc/adminhist")).isEqualTo("Note17");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/marc/physicalDescription")).isEqualTo("Note18");
    assertThat(getProperty(instance, "http://bibfra.me/vocab/marc/datesOfPublicationNote")).isEqualTo("Note19");
  }
}
