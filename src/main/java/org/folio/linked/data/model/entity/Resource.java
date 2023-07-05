package org.folio.linked.data.model.entity;

import static jakarta.persistence.CascadeType.DETACH;
import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REFRESH;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

@Entity
@Data
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "resources")
@EqualsAndHashCode(of = "resourceHash")
public class Resource {

  @Id
  private Long resourceHash;

  @NonNull
  @Column(nullable = false)
  private String label;

  @Column(columnDefinition = "json")
  @Type(JsonBinaryType.class)
  private JsonNode doc;

  @OrderBy
  @ToString.Exclude
  @OneToMany(mappedBy = "source", cascade = {PERSIST, MERGE, REFRESH, DETACH}, fetch = FetchType.EAGER)
  private Set<ResourceEdge> outgoingEdges = new LinkedHashSet<>();

  @NonNull
  @ManyToOne
  @JoinColumn(name = "type_hash", nullable = false)
  private ResourceType type;

}
