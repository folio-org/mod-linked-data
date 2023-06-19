package org.folio.linked.data.model.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "graphset")
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@Getter
@Setter
@Accessors(chain = true)
public class Bibframe {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NonNull
  @Column(nullable = false, unique = true)
  private String graphName;

  @NonNull
  @Column(nullable = false, unique = true)
  private Integer graphHash;

  @NonNull
  @Column(nullable = false, unique = true)
  private String slug;

  @Transient
  private JsonNode configuration;
}
