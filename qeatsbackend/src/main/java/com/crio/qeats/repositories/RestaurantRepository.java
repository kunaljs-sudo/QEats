/*
 *
 * * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositories;

import java.util.List;
import com.crio.qeats.models.RestaurantEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends MongoRepository<RestaurantEntity, String> {

  @Cacheable("findAllRestaurantEntity")
  List<RestaurantEntity> findAll();
}

