package org.folio.linked.data.e2e.resource;

import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.RESOURCE_URL;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.InstanceIngressEvent;
import org.folio.linked.data.domain.dto.LinkedDataTitle;
import org.folio.linked.data.domain.dto.LinkedDataWork;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.domain.dto.ResourceIndexEventType;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.resource.hash.HashService;
import org.folio.linked.data.test.kafka.KafkaInventoryTopicListener;
import org.folio.linked.data.test.kafka.KafkaProducerTestConfiguration;
import org.folio.linked.data.test.kafka.KafkaSearchWorkIndexTopicListener;
import org.folio.linked.data.test.resource.ResourceTestService;
import org.folio.marc4ld.service.marc2ld.reader.MarcReaderProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@SpringBootTest(classes = {KafkaProducerTestConfiguration.class})
class ResourceControllerUpdateAndMergeWorksIT {
  @Autowired
  private ResourceTestService resourceTestService;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private HashService hashService;
  @Autowired
  private Environment env;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private KafkaInventoryTopicListener inventoryTopicListener;
  @Autowired
  private KafkaSearchWorkIndexTopicListener searchWorkIndexTopicListener;
  @Autowired
  private ResourceRepository resourceRepository;
  @Autowired
  private MarcReaderProcessor marcReader;

  @AfterEach
  void clenUp() {
    searchWorkIndexTopicListener.getMessages().clear();
    inventoryTopicListener.getMessages().clear();
  }

  /**
   * Merge two works into a single work and ensure the following
   * 1. The instances under both works are merged into the merged work
   * 2. Appropriate events are sent to mod-search and inventory
   */
  @Test
  void merge_two_works_should_send_correct_events_to_inventory_and_search() throws Exception {
    // given
    // Create two works with 2 different titles
    String work1Title = "simple_work1";
    String work2Title = "simple_work2";
    var work1 = getWork(work1Title);
    var work2 = getWork(work2Title);
    var work1Instance = getInstance(work1);
    var work2Instance = getInstance(work2);
    resourceTestService.saveGraph(work1Instance);
    resourceTestService.saveGraph(work2Instance);

    // when
    // Update Work1's title to Work2's title so that both works will be merged
    var work1UpdateRequestDto = getWorkRequestDto(work1Instance.getId(), work2Title);

    var updateRequest = put(RESOURCE_URL + "/" + work1.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(work1UpdateRequestDto);

    mockMvc.perform(updateRequest).andExpect(status().isOk());

    // then
    // Assert that instances under both works are merged into the single work
    var mergedWork = resourceTestService.getResourceById(work2.getId().toString(), 2);
    var mergedWorkInstanceIds = mergedWork.getIncomingEdges().stream()
      .filter(edge -> edge.getPredicate().getUri().equals(PredicateDictionary.INSTANTIATES.getUri()))
      .map(ResourceEdge::getSource)
      .map(Resource::getId)
      .toList();
    assertTrue(mergedWorkInstanceIds.contains(work1Instance.getId()));
    assertTrue(mergedWorkInstanceIds.contains(work2Instance.getId()));

    // Assert that work1 is deleted
    assertTrue(resourceRepository.findById(work1.getId()).isEmpty());

    // Assert that appropriate events are sent to mod-search
    Set<ResourceIndexEvent> searchEventList = new HashSet<>();
    awaitAndAssert(() ->
      assertTrue(
        searchWorkIndexTopicListener.getMessages()
          .stream()
          .anyMatch(m -> {
            searchEventList.add(parse(m, ResourceIndexEvent.class));
            return isExpectedSearchEvents(searchEventList, work1.getId(), work2.getId());
          })
      )
    );

    // Assert that appropriate events are sent to inventory
    Set<InstanceIngressEvent> inventoryEventList = new HashSet<>();
    awaitAndAssert(() ->
      assertTrue(inventoryTopicListener.getMessages().stream()
        .anyMatch(m -> {
          inventoryEventList.add(parse(m, InstanceIngressEvent.class));
          return isExpectedInventoryEvents(inventoryEventList);
        })
      )
    );
  }

  private String getWorkRequestDto(Long instanceId, String title) {
    return """
      {
        "resource": {
          "http://bibfra.me/vocab/lite/Work": {
            "http://bibfra.me/vocab/marc/title": [
                {
                  "http://bibfra.me/vocab/marc/Title": {
                      "http://bibfra.me/vocab/marc/mainTitle": [ "%TITLE%" ]
                  }
                }
            ],
            "http://bibfra.me/vocab/marc/summary": ["new summary"],
            "_instanceReference": [ { "id": "%INSTANCE_ID%"} ]
          }
        }
      }
      """
      .replace("%INSTANCE_ID%", instanceId.toString())
      .replace("%TITLE%", title);
  }

  private Resource getWork(String titleStr) {
    String titleDoc = """
      {
        "http://bibfra.me/vocab/marc/mainTitle": ["%TITLE%"]
      }
      """
      .replace("%TITLE%", titleStr);
    var workDoc = """
      {
        "http://bibfra.me/vocab/marc/summary": ["%SUMMARY_NOTE%"]
      }
      """
      .replace("%SUMMARY_NOTE%", titleStr + "_summary_note");
    var title = new Resource()
      .addTypes(ResourceTypeDictionary.TITLE)
      .setDoc(getDoc(titleDoc))
      .setLabel(titleStr);
    var work = new Resource()
      .addTypes(ResourceTypeDictionary.WORK)
      .setDoc(getDoc(workDoc))
      .setLabel(titleStr);

    work.addOutgoingEdge(new ResourceEdge(work, title, PredicateDictionary.TITLE));

    title.setId(hashService.hash(title));
    work.setId(hashService.hash(work));

    return work;
  }

  private Resource getInstance(Resource work) {
    var titleStr = work.getLabel() + "_instance";
    var titleDoc = """
      {
        "http://bibfra.me/vocab/marc/mainTitle": ["%TITLE%"]
      }
      """
      .replace("%TITLE%", titleStr);
    var title = new Resource()
      .addTypes(ResourceTypeDictionary.TITLE)
      .setDoc(getDoc(titleDoc))
      .setLabel(titleStr);
    var instance = new Resource()
      .addTypes(ResourceTypeDictionary.INSTANCE)
      .setDoc(getDoc("{}"))
      .setLabel(titleStr);

    instance.addOutgoingEdge(new ResourceEdge(instance, title, PredicateDictionary.TITLE));
    instance.addOutgoingEdge(new ResourceEdge(instance, work, PredicateDictionary.INSTANTIATES));

    title.setId(hashService.hash(title));
    instance.setId(hashService.hash(instance));

    return instance;
  }

  @SneakyThrows
  private JsonNode getDoc(String doc) {
    return objectMapper.readTree(doc);
  }

  @SneakyThrows
  private <T> T parse(String json, Class<T> clazz) {
    return objectMapper.readValue(json, clazz);
  }

  private boolean isExpectedSearchEvents(Set<ResourceIndexEvent> events, long deletedWorkId, long createdWorkId) {
    if (events.size() != 2) {
      return false;
    }
    return events.stream()
      .allMatch(e -> isSearchDeleteEvent(e, deletedWorkId) || isSearchCreateEvent(e, createdWorkId));
  }

  private boolean isSearchCreateEvent(ResourceIndexEvent event, Long createdWorkId) {
    var work = getWorkFromEvent(event);
    ;
    return event.getType().equals(ResourceIndexEventType.CREATE)
      && work.getId().equals(createdWorkId.toString())
      && work.getInstances().size() == 2
      && work.getInstances().stream().allMatch(
      i -> i.getTitles().stream().map(LinkedDataTitle::getValue)
        .allMatch(t -> t.equals("simple_work1_instance") || t.equals("simple_work2_instance"))
    );
  }

  private boolean isSearchDeleteEvent(ResourceIndexEvent event, Long deletedWorkId) {
    var work = getWorkFromEvent(event);
    return event.getType().equals(ResourceIndexEventType.DELETE) && work.getId().equals(deletedWorkId.toString());
  }

  private LinkedDataWork getWorkFromEvent(ResourceIndexEvent event) {
    Map<String, Object> data = (Map<String, Object>) event.getNew();
    return objectMapper.convertValue(data, LinkedDataWork.class);
  }

  private boolean isExpectedInventoryEvents(Set<InstanceIngressEvent> inventoryEventList) {
    return inventoryEventList.size() == 2 && inventoryEventList.stream()
      .allMatch(
        e -> {
          var payload = e.getEventPayload();
          var marcStr = payload.getSourceRecordObject();
          var marcRecord = marcReader.readMarc(marcStr).toList().get(0);
          return e.getEventType().equals(InstanceIngressEvent.EventTypeEnum.UPDATE_INSTANCE)
            && isExpectedMarc(marcRecord);
        }
      );
  }

  private boolean isExpectedMarc(Record marcRecord) {
    var df520 = (DataField) marcRecord.getVariableField("520");
    var df520SfA = df520.getSubfields('a');
    return df520SfA.size() == 2 &&
      df520SfA.stream()
        .map(Subfield::getData)
        .allMatch(data -> data.equals("simple_work2_summary_note") || data.equals("new summary"));
  }
}
