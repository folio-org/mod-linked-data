package org.folio.linked.data.e2e.resource;

import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.RESOURCE_URL;
import static org.folio.linked.data.test.MonographTestUtil.getWork;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.util.ResourceUtils.extractInstancesFromWork;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.e2e.base.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.kafka.sender.inventory.InstanceUpdateMessageSender;
import org.folio.linked.data.integration.kafka.sender.search.WorkCreateMessageSender;
import org.folio.linked.data.integration.kafka.sender.search.WorkDeleteMessageSender;
import org.folio.linked.data.mapper.kafka.inventory.InstanceIngressMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.test.kafka.KafkaProducerTestConfiguration;
import org.folio.linked.data.test.resource.ResourceTestService;
import org.folio.marc4ld.service.marc2ld.reader.MarcReaderProcessor;
import org.junit.jupiter.api.Test;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@IntegrationTest
@SpringBootTest(classes = {KafkaProducerTestConfiguration.class})
class ResourceControllerUpdateAndMergeWorksIT extends ITBase {
  @Autowired
  private ResourceTestService resourceTestService;
  @Autowired
  private ResourceRepository resourceRepository;
  @Autowired
  private MarcReaderProcessor marcReader;
  @Autowired
  private InstanceIngressMessageMapper instanceIngressMessageMapper;
  @MockitoSpyBean
  private WorkDeleteMessageSender workDeleteMessageSender;
  @MockitoSpyBean
  private WorkCreateMessageSender workCreateMessageSender;
  @MockitoSpyBean
  private InstanceUpdateMessageSender instanceUpdateMessageSender;

  /**
   * Merge two works into a single work and ensure the following
   * 1. The instances under both works are merged into the merged work
   * 2. Appropriate events are sent to mod-search and inventory
   */
  @Test
  void merge_two_works_should_send_correct_events_to_inventory_and_search() throws Exception {
    // given
    // Create two works with 2 different titles
    var work1Title = "simple_work1";
    var work2Title = "simple_work2";
    var work1 = getWork(work1Title, hashService);
    var work2 = getWork(work2Title, hashService);
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
      .filter(edge -> edge.getPredicate().getUri().equals(INSTANTIATES.getUri()))
      .map(ResourceEdge::getSource)
      .map(Resource::getId)
      .toList();
    assertTrue(mergedWorkInstanceIds.contains(work1Instance.getId()));
    assertTrue(mergedWorkInstanceIds.contains(work2Instance.getId()));

    // Assert that work1 is deleted
    assertTrue(resourceRepository.findById(work1.getId()).isEmpty());

    // Assert that appropriate events are sent to mod-search
    var deleteCaptor = ArgumentCaptor.forClass(Resource.class);
    verify(workDeleteMessageSender).accept(deleteCaptor.capture());
    assertEquals(work1.getId(), deleteCaptor.getValue().getId());

    var createCaptor = ArgumentCaptor.forClass(Resource.class);
    verify(workCreateMessageSender).accept(createCaptor.capture());
    var capturedWork = createCaptor.getValue();
    assertEquals(work2.getId(), capturedWork.getId());
    assertEquals(2, extractInstancesFromWork(capturedWork).size());

    // Assert that appropriate events are sent to inventory
    var instanceCaptor = ArgumentCaptor.forClass(Resource.class);
    verify(instanceUpdateMessageSender, times(2)).accept(instanceCaptor.capture());
    instanceCaptor.getAllValues().forEach(instance -> {
      var event = instanceIngressMessageMapper.toInstanceIngressEvent(instance);
      var marcStr = event.getEventPayload().getSourceRecordObject();
      var marcRecord = marcReader.readMarc(marcStr).toList().getFirst();
      assertTrue(isExpectedMarc(marcRecord));
    });
  }

  private String getWorkRequestDto(Long instanceId, String title) {
    return """
      {
        "resource": {
          "http://bibfra.me/vocab/lite/Work": {
            "profileId": 2,
            "http://bibfra.me/vocab/library/title": [
                {
                  "http://bibfra.me/vocab/library/Title": {
                      "http://bibfra.me/vocab/library/mainTitle": [ "%TITLE%" ]
                  }
                }
            ],
            "http://bibfra.me/vocab/library/summary": ["new summary"],
            "_instanceReference": [ { "id": "%INSTANCE_ID%"} ]
          }
        }
      }
      """
      .replace("%INSTANCE_ID%", instanceId.toString())
      .replace("%TITLE%", title);
  }

  private Resource getInstance(Resource work) {
    var titleStr = work.getLabel() + "_instance";
    var titleDoc = """
      {
        "http://bibfra.me/vocab/library/mainTitle": ["%TITLE%"]
      }
      """
      .replace("%TITLE%", titleStr);
    var title = new Resource()
      .addTypes(ResourceTypeDictionary.TITLE)
      .setDoc(TEST_JSON_MAPPER.readTree(titleDoc))
      .setLabel(titleStr);
    var instance = new Resource()
      .addTypes(ResourceTypeDictionary.INSTANCE)
      .setDoc(TEST_JSON_MAPPER.readTree("{}"))
      .setLabel(titleStr);

    instance.addOutgoingEdge(new ResourceEdge(instance, title, TITLE));
    instance.addOutgoingEdge(new ResourceEdge(instance, work, INSTANTIATES));

    title.setIdAndRefreshEdges(hashService.hash(title));
    instance.setIdAndRefreshEdges(hashService.hash(instance));

    return instance;
  }

  private boolean isExpectedMarc(Record marcRecord) {
    var df520 = (DataField) marcRecord.getVariableField("520");
    if (df520 == null) {
      return false;
    }
    var df520SfA = df520.getSubfields('a');
    return df520SfA.size() == 2 &&
      df520SfA.stream()
        .map(Subfield::getData)
        .allMatch(data -> data.equals("simple_work2_summary_note") || data.equals("new summary"));
  }
}
