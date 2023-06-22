package org.folio.linked.data.mapper.resource.inner.sub;

public interface SubResourceMapperResolver {

  <T> SubResourceMapper<T> getMapper(String typeOrPredicate, Class<T> destinationDtoClass, boolean strict);

}
