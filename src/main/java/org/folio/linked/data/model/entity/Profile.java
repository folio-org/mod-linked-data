package org.folio.linked.data.model.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Setter
@Table(name = "profiles")
@Accessors(chain = true)
public class Profile {
  @Id
  private Long id;

  @Column(nullable = false)
  private String name;

  @ManyToOne
  @JoinColumn(name = "resource_type", nullable = false)
  private ResourceTypeEntity resourceType;

  @Type(JsonBinaryType.class)
  private String value;
}
