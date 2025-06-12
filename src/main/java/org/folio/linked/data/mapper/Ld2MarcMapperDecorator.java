package org.folio.linked.data.mapper;

import org.folio.ld.dictionary.model.RawMarc;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.repo.RawMarcRepository;
import org.folio.marc4ld.enums.UnmappedMarcHandling;
import org.folio.marc4ld.service.ld2marc.Ld2MarcMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class Ld2MarcMapperDecorator implements Ld2MarcMapper {
  private final Ld2MarcMapper delegate;
  private final RawMarcRepository rawMarcRepository;

  public Ld2MarcMapperDecorator(@Qualifier("ld2MarcMapperImpl") Ld2MarcMapper delegate,
                                RawMarcRepository rawMarcRepository) {
    this.delegate = delegate;
    this.rawMarcRepository = rawMarcRepository;
  }

  @Override
  public String toMarcJson(Resource resource) {
    enrichWithRawMarc(resource);
    return delegate.toMarcJson(resource);
  }

  @Override
  public String toMarcJson(Resource resource, UnmappedMarcHandling marcHandling) {
    enrichWithRawMarc(resource);
    return delegate.toMarcJson(resource, marcHandling);
  }

  private void enrichWithRawMarc(Resource resource) {
    rawMarcRepository.findById(resource.getId())
      .map(org.folio.linked.data.model.entity.RawMarc::getContent)
      .map(rawMarcStr -> new RawMarc().setContent(rawMarcStr))
      .ifPresent(resource::setUnmappedMarc);
  }
}
