package org.folio.linked.data.e2e;

import static java.util.Comparator.comparing;
import static org.folio.linked.data.TestUtil.asJsonString;
import static org.folio.linked.data.TestUtil.defaultHeaders;
import static org.folio.linked.data.TestUtil.getOkapiMockUrl;
import static org.folio.linked.data.TestUtil.hash;
import static org.folio.linked.data.TestUtil.random;
import static org.folio.linked.data.TestUtil.randomBibframe;
import static org.folio.linked.data.TestUtil.randomBibframeCreateRequest;
import static org.folio.linked.data.TestUtil.randomInt;
import static org.folio.linked.data.model.ErrorCode.NOT_FOUND_ERROR;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_URL;
import static org.folio.linked.data.util.BibframeConstants.ITEM_URL;
import static org.folio.linked.data.util.BibframeConstants.MONOGRAPH;
import static org.folio.linked.data.util.BibframeConstants.MONOGRAPH_URL;
import static org.folio.linked.data.util.BibframeConstants.WORK_URL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import java.io.UnsupportedEncodingException;
import lombok.SneakyThrows;
import org.folio.linked.data.domain.dto.BibframeUpdateRequest;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.mapper.BibframeMapper;
import org.folio.linked.data.model.entity.Bibframe;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.repo.BibframeRepository;
import org.folio.linked.data.repo.ResourceTypeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class BibframeControllerIT {

  public static final String BIBFRAMES_URL = "/bibframes";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private BibframeRepository bibframeRepo;
  @Autowired
  private ResourceTypeRepository typeRepo;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private BibframeMapper bibframeMapper;

  @BeforeEach
  public void setup() {
    var profile = new ResourceType();
    profile.setTypeUri(MONOGRAPH_URL);
    profile.setSimpleLabel(MONOGRAPH);
    profile.setTypeHash(hash(MONOGRAPH));
    typeRepo.save(profile);
  }

  @AfterEach
  public void clean() {
    bibframeRepo.deleteAll();
  }

  @Test
  @SneakyThrows
  void postBibframe_shouldStoreEntityCorrectly() {
    // given
    var request = randomBibframeCreateRequest();
    var requestBuilder = post(BIBFRAMES_URL)
        .contentType(APPLICATION_JSON)
        .headers(defaultHeaders(getOkapiMockUrl()))
        .content(asJsonString(request));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("id", notNullValue()))
        .andExpect(jsonPath("$.['" + WORK_URL + "']", notNullValue()))
        .andExpect(jsonPath("$.['" + INSTANCE_URL + "']", notNullValue()))
        .andExpect(jsonPath("$.['" + ITEM_URL + "']", notNullValue()));
  }

  private Long extractId(ResultActions resultActions) throws UnsupportedEncodingException {
    Object id = JsonPath.read(resultActions.andReturn().getResponse().getContentAsString(), "id");
    if (id instanceof Integer idInt) {
      return idInt.longValue();
    } else {
      return (Long) id;
    }
  }

  @Test
  void getBibframeById_shouldReturnExistedEntity() throws Exception {
    // given
    var existed = bibframeRepo.save(randomBibframe(defaultProfile()));
    var requestBuilder = get(BIBFRAMES_URL + "/" + existed.getId())
        .contentType(APPLICATION_JSON)
        .headers(defaultHeaders(getOkapiMockUrl()));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("id", notNullValue()))
        .andExpect(jsonPath("$.['" + WORK_URL + "']", notNullValue()))
        .andExpect(jsonPath("$.['" + INSTANCE_URL + "']", notNullValue()))
        .andExpect(jsonPath("$.['" + ITEM_URL + "']", notNullValue()));
  }

  private ResourceType defaultProfile() {
    return typeRepo.findBySimpleLabel(MONOGRAPH).orElseThrow();
  }

  @Test
  void getBibframeById_shouldReturn404_ifNoExistedEntity() throws Exception {
    // given
    var notExistedId = randomInt();
    var requestBuilder = get(BIBFRAMES_URL + "/" + notExistedId)
        .contentType(APPLICATION_JSON)
        .headers(defaultHeaders(getOkapiMockUrl()));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("errors[0].message", equalTo("Bibframe record with given id ["
            + notExistedId + "] is not found")))
        .andExpect(jsonPath("errors[0].type", equalTo(NotFoundException.class.getSimpleName())))
        .andExpect(jsonPath("errors[0].code", equalTo(NOT_FOUND_ERROR.getValue())))
        .andExpect(jsonPath("total_records", equalTo(1)));
  }

  @Test
  void updateBibframeById_shouldReturn404_ifNoExistedEntity() throws Exception {
    // given
    var notExistedId = randomInt();
    var requestBuilder = put(BIBFRAMES_URL + "/" + notExistedId)
        .contentType(APPLICATION_JSON)
        .headers(defaultHeaders(getOkapiMockUrl()))
        .content(asJsonString(random(BibframeUpdateRequest.class)));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("errors[0].message", equalTo("Bibframe record with given id ["
            + notExistedId + "] is not found")))
        .andExpect(jsonPath("errors[0].type", equalTo(NotFoundException.class.getSimpleName())))
        .andExpect(jsonPath("errors[0].code", equalTo(NOT_FOUND_ERROR.getValue())))
        .andExpect(jsonPath("total_records", equalTo(1)));
  }

  @Test
  void updateBibframeById_shouldReturnUpdatedEntity_ifEntityExists() throws Exception {
    // given
    var existed = bibframeRepo.save(randomBibframe(defaultProfile()));
    var request = randomBibframeCreateRequest();
    var requestBuilder = put(BIBFRAMES_URL + "/" + existed.getId())
        .contentType(APPLICATION_JSON)
        .headers(defaultHeaders(getOkapiMockUrl()))
        .content(asJsonString(request));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("id", equalTo(existed.getId().intValue())))
        .andExpect(jsonPath("$.['" + WORK_URL + "']", notNullValue()))
        .andExpect(jsonPath("$.['" + INSTANCE_URL + "']", notNullValue()))
        .andExpect(jsonPath("$.['" + ITEM_URL + "']", notNullValue()));
  }

  @Test
  void deleteBibframeById_shouldReturn404_ifNoExistedEntity() throws Exception {
    // given
    var notExistedId = randomInt();
    var requestBuilder = delete(BIBFRAMES_URL + "/" + notExistedId)
        .contentType(APPLICATION_JSON)
        .headers(defaultHeaders(getOkapiMockUrl()));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("errors[0].message", equalTo("Bibframe record with given id ["
            + notExistedId + "] is not found")))
        .andExpect(jsonPath("errors[0].type", equalTo(NotFoundException.class.getSimpleName())))
        .andExpect(jsonPath("errors[0].code", equalTo(NOT_FOUND_ERROR.getValue())))
        .andExpect(jsonPath("total_records", equalTo(1)));
  }

  @Test
  void deleteBibframeById_shouldDeleteExistedEntity() throws Exception {
    // given
    var existed = bibframeRepo.save(randomBibframe(defaultProfile()));
    var requestBuilder = delete(BIBFRAMES_URL + "/" + existed.getId())
        .contentType(APPLICATION_JSON)
        .headers(defaultHeaders(getOkapiMockUrl()));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions.andExpect(status().isNoContent());
    assertThat(bibframeRepo.existsById(existed.getId()), is(false));
  }

  @Test
  void getBibframesShortInfoPage_shouldReturnPageWithExistedEntities() throws Exception {
    // given
    var existed = Lists.newArrayList(
        bibframeRepo.save(randomBibframe(defaultProfile())),
        bibframeRepo.save(randomBibframe(defaultProfile())),
        bibframeRepo.save(randomBibframe(defaultProfile()))
    ).stream().sorted(comparing(Bibframe::getGraphName)).toList();
    var requestBuilder = get(BIBFRAMES_URL)
        .contentType(APPLICATION_JSON)
        .headers(defaultHeaders(getOkapiMockUrl()));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("number", equalTo(0)))
        .andExpect(jsonPath("total_pages", equalTo(1)))
        .andExpect(jsonPath("total_elements", equalTo(3)))
        .andExpect(jsonPath("content", hasSize(3)))
        .andExpect(jsonPath("content[0].id", equalTo(existed.get(0).getId().intValue())))
        .andExpect(jsonPath("content[0].graphName", equalTo(existed.get(0).getGraphName())))
        .andExpect(jsonPath("content[1].id", equalTo(existed.get(1).getId().intValue())))
        .andExpect(jsonPath("content[1].graphName", equalTo(existed.get(1).getGraphName())))
        .andExpect(jsonPath("content[2].id", equalTo(existed.get(2).getId().intValue())))
        .andExpect(jsonPath("content[2].graphName", equalTo(existed.get(2).getGraphName())));
  }
}
