package org.folio.linked.data.mapper;

import org.folio.ld.dictionary.model.RawMarc;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.service.resource.marc.RawMarcService;
import org.folio.marc4ld.enums.UnmappedMarcHandling;
import org.folio.marc4ld.service.ld2marc.Ld2MarcMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class Ld2MarcMapperDecorator implements Ld2MarcMapper {
  private final Ld2MarcMapper delegate;
  private final RawMarcService rawMarcService;

  public Ld2MarcMapperDecorator(@Qualifier("ld2MarcMapperImpl") Ld2MarcMapper delegate,
                                RawMarcService rawMarcService) {
    this.delegate = delegate;
    this.rawMarcService = rawMarcService;
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
    rawMarcService.getRawMarc(resource.getId())
      .map(rawMarcStr -> new RawMarc().setContent(rawMarcStr))
      .ifPresent(resource::setUnmappedMarc);
  }
}
