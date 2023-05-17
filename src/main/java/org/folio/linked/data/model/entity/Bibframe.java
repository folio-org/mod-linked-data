package org.folio.linked.data.model.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "graphset")
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@Getter
@Setter
public class Bibframe {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NonNull
  @Column(nullable = false, unique = true)
  private String graphName;

  @Column(nullable = false, unique = true)
  private Integer graphHash;

  @Column(nullable = false, unique = true)
  private String slug;

  @NonNull
  @Column(columnDefinition = "json", nullable = false)
  @Type(JsonBinaryType.class)
  private JsonNode configuration;
}
