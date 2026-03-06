package org.folio.linked.data.model.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

@Data
@Entity
@Table(name = "vocabularies")
@Accessors(chain = true)
public class VocabularyEntity {

  @Id
  @Column(name = "vocabulary_name")
  private String vocabularyName;

  @Type(JsonBinaryType.class)
  @Column(name = "vocabulary_json", columnDefinition = "jsonb", nullable = false)
  private String vocabularyJson;
}
