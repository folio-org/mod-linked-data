package org.folio.linked.data.model.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "graphset")
@NoArgsConstructor
@Getter
@Setter
public class Bibframe {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String graphName;

  @Column(unique = true)
  private Integer graphHash;

  @Column(nullable = false, unique = true)
  private String slug;

  @Column(columnDefinition = "json")
  @Type(JsonBinaryType.class)
  private JsonNode configuration;

  @ManyToOne
  @JoinColumn(name = "profile_hash")
  private ResourceType profile;

  @ManyToMany
  @JoinTable(name = "graph_resources",
      joinColumns = @JoinColumn(name = "graph_id"),
      inverseJoinColumns = @JoinColumn(name = "resource_hash"))
  private List<Resource> resources;
}
