package org.folio.linked.data.e2e;

import static java.util.Comparator.comparing;
import static org.folio.linked.data.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.TestUtil.asJsonString;
import static org.folio.linked.data.TestUtil.defaultHeaders;
import static org.folio.linked.data.TestUtil.getBibframeJsonNodeSample;
import static org.folio.linked.data.TestUtil.getBibframeSample;
import static org.folio.linked.data.TestUtil.random;
import static org.folio.linked.data.TestUtil.randomBibframe;
import static org.folio.linked.data.TestUtil.randomBibframeCreateRequest;
import static org.folio.linked.data.TestUtil.randomString;
import static org.folio.linked.data.matcher.IsEqualJson.equalToJson;
import static org.folio.linked.data.model.ErrorCode.ALREADY_EXISTS_ERROR;
import static org.folio.linked.data.model.ErrorCode.NOT_FOUND_ERROR;
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

import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import lombok.SneakyThrows;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeUpdateRequest;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.exception.AlreadyExistsException;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.model.entity.Bibframe;
import org.folio.linked.data.repo.BibframeRepository;
import org.folio.linked.data.util.TextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class BibframeControllerIT {

  public static final String BIBFRAMES_URL = "/bibframes";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private BibframeRepository repo;
  @Value("${folio.environment}")
  private String folioEnv;

  @AfterEach
  public void clean() {
    repo.deleteAll();
  }

  @Test
  @SneakyThrows
  void postBibframe_shouldStoreEntityCorrectly() {
    // given
    var request = random(BibframeCreateRequest.class);
    var requestBuilder = post(BIBFRAMES_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(folioEnv))
      .content(asJsonString(request));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("id", notNullValue()))
      .andExpect(jsonPath("graphName", notNullValue()))
      .andExpect(jsonPath("graphHash", notNullValue()))
      .andExpect(jsonPath("slug", notNullValue()))
      .andExpect(jsonPath("configuration", equalToJson(getBibframeSample())));

    String slug = JsonPath.read(resultActions.andReturn().getResponse().getContentAsString(), "slug");
    var expectedConfiguration = getBibframeJsonNodeSample();
    repo.findBySlug(slug).ifPresentOrElse(e -> {
      assertThat(e.getGraphName(), equalTo(request.getGraphName()));
      assertThat(e.getSlug(), equalTo(TextUtil.slugify(request.getGraphName())));
      assertThat(e.getGraphHash(), equalTo(e.getSlug().hashCode()));
      assertThat(e.getConfiguration(), equalTo(expectedConfiguration));
    }, () -> Assertions.fail("Expected entity wasn't saved into a repo"));
  }

  @Test
  void postBibframe_shouldReturnAlreadyExistsError_ifBibframeWithGivenSlugExists() throws Exception {
    // given
    var existed = randomBibframe();
    repo.save(existed);
    var requestBuilder = post(BIBFRAMES_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(folioEnv))
      .content(asJsonString(randomBibframeCreateRequest(existed.getGraphName())));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isBadRequest())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("errors[0].message", equalTo("Bibframe record with given slug ["
        + existed.getSlug() + "] exists already")))
      .andExpect(jsonPath("errors[0].type", equalTo(AlreadyExistsException.class.getSimpleName())))
      .andExpect(jsonPath("errors[0].code", equalTo(ALREADY_EXISTS_ERROR.getValue())))
      .andExpect(jsonPath("total_records", equalTo(1)));
  }

  @Test
  void getBibframeBySlug_shouldReturnExistedEntity() throws Exception {
    // given
    var existed = repo.save(randomBibframe());
    var requestBuilder = get(BIBFRAMES_URL + "/" + existed.getSlug())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(folioEnv));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("id").isNotEmpty())
      .andExpect(jsonPath("graphName", equalTo(existed.getGraphName())))
      .andExpect(jsonPath("graphHash", equalTo(existed.getGraphHash())))
      .andExpect(jsonPath("slug", equalTo(existed.getSlug())))
      .andExpect(jsonPath("configuration", equalToJson(getBibframeSample())));
  }

  @Test
  void getBibframeBySlug_shouldReturn404_ifNoExistedEntity() throws Exception {
    // given
    var notExistedId = randomString();
    var requestBuilder = get(BIBFRAMES_URL + "/" + notExistedId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(folioEnv));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isNotFound())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("errors[0].message", equalTo("Bibframe record with given slug ["
        + notExistedId + "] is not found")))
      .andExpect(jsonPath("errors[0].type", equalTo(NotFoundException.class.getSimpleName())))
      .andExpect(jsonPath("errors[0].code", equalTo(NOT_FOUND_ERROR.getValue())))
      .andExpect(jsonPath("total_records", equalTo(1)));
  }

  @Test
  void updateBibframeBySlug_shouldReturn404_ifNoExistedEntity() throws Exception {
    // given
    var notExistedId = randomString();
    var requestBuilder = put(BIBFRAMES_URL + "/" + notExistedId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(folioEnv))
      .content(asJsonString(random(BibframeUpdateRequest.class)));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isNotFound())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("errors[0].message", equalTo("Bibframe record with given slug ["
        + notExistedId + "] is not found")))
      .andExpect(jsonPath("errors[0].type", equalTo(NotFoundException.class.getSimpleName())))
      .andExpect(jsonPath("errors[0].code", equalTo(NOT_FOUND_ERROR.getValue())))
      .andExpect(jsonPath("total_records", equalTo(1)));
  }

  @Test
  void updateBibframeBySlug_shouldReturnUpdatedEntity_ifEntityExists() throws Exception {
    // given
    var existed = repo.save(randomBibframe());
    var updatedConfiguration = "{ \"updated\": true }";
    var requestBuilder = put(BIBFRAMES_URL + "/" + existed.getSlug())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(folioEnv))
      .content(asJsonString(new BibframeUpdateRequest(updatedConfiguration)));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("id", equalTo(existed.getId().intValue())))
      .andExpect(jsonPath("graphName", equalTo(existed.getGraphName())))
      .andExpect(jsonPath("graphHash", equalTo(existed.getGraphHash())))
      .andExpect(jsonPath("slug", equalTo(existed.getSlug())))
      .andExpect(jsonPath("configuration", equalToJson(updatedConfiguration)));

    var expectedConfiguration = OBJECT_MAPPER.readTree(updatedConfiguration);
    repo.findBySlug(existed.getSlug()).ifPresentOrElse(e -> {
      assertThat(e.getGraphName(), equalTo(existed.getGraphName()));
      assertThat(e.getSlug(), equalTo(TextUtil.slugify(existed.getGraphName())));
      assertThat(e.getGraphHash(), equalTo(e.getSlug().hashCode()));
      assertThat(e.getConfiguration(), equalTo(expectedConfiguration));
    }, () -> Assertions.fail("Expected entity wasn't saved into a repo"));
  }

  @Test
  void deleteBibframeBySlug_shouldReturn404_ifNoExistedEntity() throws Exception {
    // given
    var notExistedId = randomString();
    var requestBuilder = delete(BIBFRAMES_URL + "/" + notExistedId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(folioEnv));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isNotFound())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("errors[0].message", equalTo("Bibframe record with given slug ["
        + notExistedId + "] is not found")))
      .andExpect(jsonPath("errors[0].type", equalTo(NotFoundException.class.getSimpleName())))
      .andExpect(jsonPath("errors[0].code", equalTo(NOT_FOUND_ERROR.getValue())))
      .andExpect(jsonPath("total_records", equalTo(1)));
  }

  @Test
  void deleteBibframeBySlug_shouldDeleteExistedEntity() throws Exception {
    // given
    var existed = repo.save(randomBibframe());
    var requestBuilder = delete(BIBFRAMES_URL + "/" + existed.getSlug())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(folioEnv));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions.andExpect(status().isNoContent());
    assertThat(repo.existsBySlug(existed.getSlug()), is(false));
  }

  @Test
  void getBibframesShortInfoPage_shouldReturnPageWithExistedEntities() throws Exception {
    // given
    var existed = Lists.newArrayList(
      repo.save(randomBibframe()),
      repo.save(randomBibframe()),
      repo.save(randomBibframe())
    ).stream().sorted(comparing(Bibframe::getGraphName)).toList();
    var requestBuilder = get(BIBFRAMES_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(folioEnv));

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
