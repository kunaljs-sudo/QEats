/*
 *
 * * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import ch.hsr.geohash.GeoHash;
import redis.clients.jedis.Jedis;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Provider;
import com.crio.qeats.configs.RedisConfiguration;
import com.crio.qeats.dto.Item;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.globals.GlobalConstants;
import com.crio.qeats.models.ItemEntity;
import com.crio.qeats.models.MenuEntity;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.ItemRepository;
import com.crio.qeats.repositories.MenuRepository;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoLocation;
import com.crio.qeats.utils.GeoUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;


@Service
public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {



  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private RestaurantRepository restaurantRepository;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private MenuRepository menuRepository;

  @Autowired
  private Provider<ModelMapper> modelMapperProvider;

  @Autowired
  private RedisConfiguration redisConfiguration;


  // :TODO: I might remove this in future
  // private final RedisConfiguration redisConfiguration;

  // @Autowired
  // public RestaurantRepositoryServiceImpl(RedisConfiguration redisConfiguration) {
  // this.redisConfiguration = redisConfiguration;
  // }

  private boolean isOpenNow(LocalTime time, RestaurantEntity res) {
    LocalTime openingTime = LocalTime.parse(res.getOpensAt());
    LocalTime closingTime = LocalTime.parse(res.getClosesAt());

    return time.isAfter(openingTime) && time.isBefore(closingTime);
  }

  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objectives:
  // 1. Implement findAllRestaurantsCloseby.
  // 2. Remember to keep the precision of GeoHash in mind while using it as a key.
  // Check RestaurantRepositoryService.java file for the interface contract.
  public List<Restaurant> findAllRestaurantsCloseBy(Double latitude, Double longitude,
      LocalTime currentTime, Double servingRadiusInKms) {

    List<Restaurant> restaurants = new ArrayList<>();
    if (redisConfiguration.isCacheAvailable()) {
      restaurants =
          findAllRestaurantsCloseByFromCache(latitude, longitude, currentTime, servingRadiusInKms);
    } else {
      restaurants =
          findAllRestaurantsCloseFromDb(latitude, longitude, currentTime, servingRadiusInKms);
    }

    return restaurants;
  }



  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objective:
  // 1. Check if a restaurant is nearby and open. If so, it is a candidate to be returned.
  // NOTE: How far exactly is "nearby"?

  /**
   * Utility method to check if a restaurant is within the serving radius at a given time.
   * 
   * @return boolean True if restaurant falls within serving radius and is open, false otherwise
   */
  private boolean isRestaurantCloseByAndOpen(RestaurantEntity restaurantEntity,
      LocalTime currentTime, Double latitude, Double longitude, Double servingRadiusInKms) {
    if (isOpenNow(currentTime, restaurantEntity)) {
      return GeoUtils.findDistanceInKm(latitude, longitude, restaurantEntity.getLatitude(),
          restaurantEntity.getLongitude()) < servingRadiusInKms;
    }

    return false;
  }


  private List<Restaurant> findAllRestaurantsCloseFromDb(Double latitude, Double longitude,
      LocalTime currentTime, Double servingRadiusInKms) {
    ModelMapper modelMapper = modelMapperProvider.get();
    List<RestaurantEntity> restaurantEntities = restaurantRepository.findAll();
    List<Restaurant> restaurants = new ArrayList<Restaurant>();
    for (RestaurantEntity restaurantEntity : restaurantEntities) {
      if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude, longitude,
          servingRadiusInKms)) {
        restaurants.add(modelMapper.map(restaurantEntity, Restaurant.class));
      }
    }
    return restaurants;
  }

  private List<Restaurant> findAllRestaurantsCloseByFromCache(Double latitude, Double longitude,
      LocalTime currentTime, Double servingRadiusInKms) {
    List<Restaurant> restaurantList = new ArrayList<>();

    GeoLocation geoLocation = new GeoLocation(latitude, longitude);
    GeoHash geoHash =
        GeoHash.withCharacterPrecision(geoLocation.getLatitude(), geoLocation.getLongitude(), 7);

    try (Jedis jedis = redisConfiguration.getJedisPool().getResource()) {
      String jsonStringFromCache = jedis.get(geoHash.toBase32());

      if (jsonStringFromCache == null) {
        // Cache needs to be updated.
        String createdJsonString = "";
        try {
          restaurantList = findAllRestaurantsCloseFromDb(geoLocation.getLatitude(),
              geoLocation.getLongitude(), currentTime, servingRadiusInKms);
          createdJsonString = new ObjectMapper().writeValueAsString(restaurantList);
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }
        // Do operations with jedis resource
        jedis.setex(geoHash.toBase32(), GlobalConstants.REDIS_ENTRY_EXPIRY_IN_SECONDS,
            createdJsonString);
      } else {
        try {
          restaurantList = new ObjectMapper().readValue(jsonStringFromCache,
              new TypeReference<List<Restaurant>>() {});
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return restaurantList;
  }


  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants whose names have an exact or partial match with the search query.
  @Override
  public List<Restaurant> findRestaurantsByName(Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {

    List<Restaurant> restaurants;
    if (redisConfiguration.isCacheAvailable()) {
      restaurants =
          findAllRestaurantsCloseByFromCache(latitude, longitude, currentTime, servingRadiusInKms);
    } else {
      restaurants =
          findAllRestaurantsCloseFromDb(latitude, longitude, currentTime, servingRadiusInKms);
    }

    List<Restaurant> restaurantsByName = new ArrayList<>();
    List<Restaurant> allCloseByRestaurants =
        findAllRestaurantsCloseBy(latitude, longitude, currentTime, servingRadiusInKms);
    for (Restaurant restaurant : allCloseByRestaurants) {
      if (restaurant.getName().contains(searchString)) {
        restaurantsByName.add(restaurant);
      }
    }

    return restaurantsByName;


  }


  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants whose attributes (cuisines) intersect with the search query.
  @Override
  public List<Restaurant> findRestaurantsByAttributes(Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {
    List<Restaurant> restaurants = new ArrayList<>();
    if (redisConfiguration.isCacheAvailable()) {
      restaurants =
          findAllRestaurantsCloseByFromCache(latitude, longitude, currentTime, servingRadiusInKms);
    } else {
      restaurants =
          findAllRestaurantsCloseFromDb(latitude, longitude, currentTime, servingRadiusInKms);
    }

    List<Restaurant> restaurantsByAttributes = new ArrayList<>();
    List<Restaurant> allCloseByRestaurants =
        findAllRestaurantsCloseBy(latitude, longitude, currentTime, servingRadiusInKms);
    for (Restaurant restaurant : allCloseByRestaurants) {
      boolean found = restaurant.getAttributes().stream().anyMatch(s -> s.contains(searchString));
      if (found) {
        restaurantsByAttributes.add(restaurant);
      }
    }

    return restaurantsByAttributes;
  }



  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants which serve food items whose names form a complete or partial match
  // with the search query.

  @Override
  public List<Restaurant> findRestaurantsByItemName(Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {
    // get all Items
    List<ItemEntity> allItems = itemRepository.findAll();

    // get all ItemIds matching the Item name
    List<String> itemIdList =
        allItems.stream().filter(item -> item.getName().contains(searchString))
            .map(ItemEntity::getItemId).collect(Collectors.toList());


    // get all menus containig those items
    List<MenuEntity> menus = menuRepository.findMenusByItemsItemIdIn(itemIdList).get();

    if (menus == null || menus.isEmpty()) {
      return Collections.emptyList();
    }

    // get all RestaurantIds from menus
    List<String> restaurantIds =
        menus.stream().map(MenuEntity::getRestaurantId).collect(Collectors.toList());

    // get all restaurantsCloseBy and Open
    List<Restaurant> allRestaurants =
        findAllRestaurantsCloseBy(latitude, longitude, currentTime, servingRadiusInKms);

    Map<String, Restaurant> restIdRestMap = new HashMap<>();

    for (Restaurant restaurant : allRestaurants) {
      restIdRestMap.put(restaurant.getRestaurantId(), restaurant);
    }

    List<Restaurant> restaurantsByItemName = new ArrayList<>();

    // storing the restaurants which contain that item
    for (String restaurantId : restaurantIds) {
      if (restIdRestMap.containsKey(restaurantId)) {
        restaurantsByItemName.add(restIdRestMap.get(restaurantId));
      }
    }

    return restaurantsByItemName;
  }

  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants which serve food items whose attributes intersect with the search query.
  @Override
  public List<Restaurant> findRestaurantsByItemAttributes(Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {

    // get all Items contains ItemAttribute searchedfor
    List<ItemEntity> allItems = itemRepository.findAll();

    // get all ItemIds matching the Item name
    List<String> itemIdList =
        allItems.stream().filter(item -> item.getAttributes().contains(searchString))
            .map(ItemEntity::getItemId).collect(Collectors.toList());


    // get all menus containig those items
    List<MenuEntity> menus = menuRepository.findMenusByItemsItemIdIn(itemIdList).get();

    if (menus == null || menus.isEmpty()) {
      return Collections.emptyList();
    }

    // get all RestaurantIds from menus
    List<String> restaurantIds =
        menus.stream().map(MenuEntity::getRestaurantId).collect(Collectors.toList());

    // get all restaurantsCloseBy and Open
    List<Restaurant> allRestaurants =
        findAllRestaurantsCloseBy(latitude, longitude, currentTime, servingRadiusInKms);

    Map<String, Restaurant> restIdRestMap = new HashMap<>();

    for (Restaurant restaurant : allRestaurants) {
      restIdRestMap.put(restaurant.getRestaurantId(), restaurant);
    }

    List<Restaurant> restaurantsByItemAttribute = new ArrayList<>();

    // storing the restaurants which contain that item
    for (String restaurantId : restaurantIds) {
      if (restIdRestMap.containsKey(restaurantId)) {
        restaurantsByItemAttribute.add(restIdRestMap.get(restaurantId));
      }
    }

    return restaurantsByItemAttribute;
  }



}

