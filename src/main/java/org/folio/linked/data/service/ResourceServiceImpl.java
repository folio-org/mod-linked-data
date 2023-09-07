package org.folio.linked.data.service;

import static java.util.Objects.isNull;
import static org.folio.linked.data.util.Constants.BIBFRAME_PROFILE;
import static org.folio.linked.data.util.Constants.BIBFRAME_WITH_GIVEN_ID;
import static org.folio.linked.data.util.Constants.EXISTS_ALREADY;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.IS_NOT_IN_THE_LIST_OF_SUPPORTED;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.configuration.properties.BibframeProperties;
import org.folio.linked.data.domain.dto.Bibframe2Request;
import org.folio.linked.data.domain.dto.Bibframe2Response;
import org.folio.linked.data.domain.dto.BibframeRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.exception.AlreadyExistsException;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.BibframeMapper;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

  private static final int DEFAULT_PAGE_NUMBER = 0;
  private static final int DEFAULT_PAGE_SIZE = 100;
  private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "resourceHash");
  private final ResourceRepository resourceRepo;
  private final BibframeMapper bibframeMapper;
  private final BibframeProperties bibframeProperties;
  private final KafkaSender kafkaSender;

  @Override
  public BibframeResponse createBibframe(BibframeRequest bibframeRequest) {
    var mapped = bibframeMapper.toEntity(bibframeRequest);
    if (resourceRepo.existsById(mapped.getResourceHash())) {
      throw new AlreadyExistsException(BIBFRAME_WITH_GIVEN_ID + mapped.getResourceHash() + EXISTS_ALREADY);
    }
    var persisted = resourceRepo.save(mapped);
    kafkaSender.sendResourceCreated(bibframeMapper.mapToIndex(persisted));
    return bibframeMapper.toDto(persisted);
  }

  @Override
  public Bibframe2Response createBibframe2(Bibframe2Request bibframeRequest) {
    if (!bibframeProperties.getProfiles().contains(bibframeRequest.getProfile())) {
      throw new NotSupportedException(BIBFRAME_PROFILE + bibframeRequest.getProfile()
        + IS_NOT_IN_THE_LIST_OF_SUPPORTED + String.join(", ", bibframeProperties.getProfiles()));
    }
    var mapped = bibframeMapper.toEntity2(bibframeRequest);
    if (resourceRepo.existsById(mapped.getResourceHash())) {
      throw new AlreadyExistsException(BIBFRAME_WITH_GIVEN_ID + mapped.getResourceHash() + EXISTS_ALREADY);
    }
    var persisted = resourceRepo.save(mapped);
    kafkaSender.sendResourceCreated(bibframeMapper.mapToIndex2(persisted));
    return bibframeMapper.toDto2(persisted);
  }

  @Override
  public BibframeResponse getBibframeById(Long id) {
    var resource = resourceRepo.findById(id).orElseThrow(() ->
      new NotFoundException(BIBFRAME_WITH_GIVEN_ID + id + IS_NOT_FOUND));
    return bibframeMapper.toDto(resource);
  }

  @Override
  public Bibframe2Response getBibframe2ById(Long id) {
    var resource = resourceRepo.findById(id).orElseThrow(() ->
      new NotFoundException(BIBFRAME_WITH_GIVEN_ID + id + IS_NOT_FOUND));
    return bibframeMapper.toDto2(resource);
  }

  @Override
  public BibframeResponse updateBibframe(Long id, BibframeRequest bibframeRequest) {
    if (!resourceRepo.existsById(id)) {
      throw new NotFoundException(BIBFRAME_WITH_GIVEN_ID + id + IS_NOT_FOUND);
    }
    deleteBibframe(id);
    return createBibframe(bibframeRequest);
  }

  @Override
  public Bibframe2Response updateBibframe2(Long id, Bibframe2Request bibframeRequest) {
    if (!resourceRepo.existsById(id)) {
      throw new NotFoundException(BIBFRAME_WITH_GIVEN_ID + id + IS_NOT_FOUND);
    }
    deleteBibframe(id);
    return createBibframe2(bibframeRequest);
  }

  @Override
  public void deleteBibframe(Long id) {
    resourceRepo.deleteById(id);
    kafkaSender.sendResourceDeleted(id);
  }

  @Override
  public BibframeShortInfoPage getBibframeShortInfoPage(String type, Integer pageNumber, Integer pageSize) {
    if (isNull(pageNumber)) {
      pageNumber = DEFAULT_PAGE_NUMBER;
    }
    if (isNull(pageSize)) {
      pageSize = DEFAULT_PAGE_SIZE;
    }
    var page = resourceRepo.findResourcesByType(Set.of(type), PageRequest.of(pageNumber, pageSize, DEFAULT_SORT));
    var pageOfDto = page.map(bibframeMapper::map);
    return bibframeMapper.map(pageOfDto);
  }

  @Override
  public BibframeShortInfoPage getBibframe2ShortInfoPage(Integer pageNumber, Integer pageSize) {
    return getBibframeShortInfoPage(bibframeProperties.getProfiles().iterator().next(), pageNumber, pageSize);
  }

}
