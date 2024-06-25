package org.folio.linked.data.test.json;

import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.Map;
import org.folio.linked.data.domain.dto.ParallelTitleFieldResponse;
import org.folio.linked.data.domain.dto.PrimaryTitleFieldResponse;
import org.folio.linked.data.domain.dto.TitleFieldResponse;
import org.folio.linked.data.domain.dto.VariantTitleFieldResponse;
import org.folio.linked.data.util.DtoDeserializer;

public class TitleFieldResponseDeserializer extends JsonDeserializer<TitleFieldResponse> {
  private static final Map<String, Class<? extends TitleFieldResponse>> IDENDTITY_MAP = Map.of(
    TITLE.getUri(), PrimaryTitleFieldResponse.class,
    PARALLEL_TITLE.getUri(), ParallelTitleFieldResponse.class,
    VARIANT_TITLE.getUri(), VariantTitleFieldResponse.class
  );
  private final DtoDeserializer<TitleFieldResponse> dtoDeserializer = new DtoDeserializer<>();

  @Override
  public TitleFieldResponse deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
    return dtoDeserializer.deserialize(jp, IDENDTITY_MAP, TitleFieldResponse.class);
  }
}
