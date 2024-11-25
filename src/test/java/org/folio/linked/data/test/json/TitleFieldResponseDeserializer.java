package org.folio.linked.data.test.json;

import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.linked.data.test.TestUtil.EMPTY_EXCEPTION_BUILDER;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.Map;
import org.folio.linked.data.domain.dto.ParallelTitleFieldResponse;
import org.folio.linked.data.domain.dto.PrimaryTitleFieldResponse;
import org.folio.linked.data.domain.dto.TitleFieldResponseTitleInner;
import org.folio.linked.data.domain.dto.VariantTitleFieldResponse;
import org.folio.linked.data.util.DtoDeserializer;

public class TitleFieldResponseDeserializer extends JsonDeserializer<TitleFieldResponseTitleInner> {
  private static final Map<String, Class<? extends TitleFieldResponseTitleInner>> IDENDTITY_MAP = Map.of(
    TITLE.getUri(), PrimaryTitleFieldResponse.class,
    PARALLEL_TITLE.getUri(), ParallelTitleFieldResponse.class,
    VARIANT_TITLE.getUri(), VariantTitleFieldResponse.class
  );
  private final DtoDeserializer<TitleFieldResponseTitleInner> dtoDeserializer =
    new DtoDeserializer<>(TitleFieldResponseTitleInner.class, IDENDTITY_MAP, EMPTY_EXCEPTION_BUILDER);

  @Override
  public TitleFieldResponseTitleInner deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
    return dtoDeserializer.deserialize(jp);
  }
}
