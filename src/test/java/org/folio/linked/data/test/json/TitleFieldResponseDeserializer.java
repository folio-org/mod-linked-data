package org.folio.linked.data.test.json;

import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;

import java.util.Map;
import org.folio.linked.data.domain.dto.ParallelTitleFieldResponse;
import org.folio.linked.data.domain.dto.PrimaryTitleFieldResponse;
import org.folio.linked.data.domain.dto.TitleFieldResponseTitleInner;
import org.folio.linked.data.domain.dto.VariantTitleFieldResponse;
import org.folio.linked.data.util.DtoDeserializer;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class TitleFieldResponseDeserializer extends ValueDeserializer<TitleFieldResponseTitleInner> {
  private static final Map<String, Class<? extends TitleFieldResponseTitleInner>> IDENDTITY_MAP = Map.of(
    TITLE.getUri(), PrimaryTitleFieldResponse.class,
    PARALLEL_TITLE.getUri(), ParallelTitleFieldResponse.class,
    VARIANT_TITLE.getUri(), VariantTitleFieldResponse.class
  );
  private final DtoDeserializer<TitleFieldResponseTitleInner> dtoDeserializer =
    new DtoDeserializer<>(TitleFieldResponseTitleInner.class, IDENDTITY_MAP);

  @Override
  public TitleFieldResponseTitleInner deserialize(JsonParser jp, DeserializationContext dc) {
    return dtoDeserializer.deserialize(jp, dc);
  }
}
