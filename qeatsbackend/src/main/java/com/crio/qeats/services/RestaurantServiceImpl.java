
/*
 *
 * * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.services;

import lombok.extern.log4j.Log4j2;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exceptions.CustomQEatsException;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.repositoryservices.RestaurantRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RestaurantServiceImpl implements RestaurantService {

  private final Double peakHoursServingRadiusInKms = 3.0;
  private final Double normalHoursServingRadiusInKms = 5.0;
  @Autowired
  private RestaurantRepositoryService restaurantRepositoryService;


  // TODO: CRIO_TASK_MODULE_RESTAURANTSAPI - Implement findAllRestaurantsCloseby.
  // Check RestaurantService.java file for the interface contract.
  @Override
  public GetRestaurantsResponse findAllRestaurantsCloseBy(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {

    int h = currentTime.getHour();
    int m = currentTime.getMinute();

    List<Restaurant> restaurants;

    if ((h >= 8 && h <= 9) || (h == 10 && m == 0) || (h == 13) || (h == 14 && m == 0)
        || (h >= 19 && h < 21) || (h == 21 && m == 0)) {
      restaurants =
          restaurantRepositoryService.findAllRestaurantsCloseBy(getRestaurantsRequest.getLatitude(),
              getRestaurantsRequest.getLongitude(), currentTime, peakHoursServingRadiusInKms);
    } else {
      restaurants =
          restaurantRepositoryService.findAllRestaurantsCloseBy(getRestaurantsRequest.getLatitude(),
              getRestaurantsRequest.getLongitude(), currentTime, normalHoursServingRadiusInKms);
    }

    for (Restaurant restaurant : restaurants) {
      String sanitizedName = restaurant.getName().replaceAll("[Â©éí]", "e");
      restaurant.setName(sanitizedName);
    }

    GetRestaurantsResponse getRestaurantsResponse = new GetRestaurantsResponse(restaurants);
    log.info(getRestaurantsResponse);

    return getRestaurantsResponse;
  }



  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Implement findRestaurantsBySearchQuery. The request object has the search string.
  // We have to combine results from multiple sources:
  // 1. Restaurants by name (exact and inexact)
  // 2. Restaurants by cuisines (also called attributes)
  // 3. Restaurants by food items it serves
  // 4. Restaurants by food item attributes (spicy, sweet, etc)
  // Remember, a restaurant must be present only once in the resulting list.
  // Check RestaurantService.java file for the interface contract.
  @Override
  public GetRestaurantsResponse findRestaurantsBySearchQuery(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
    int h = currentTime.getHour();
    int m = currentTime.getMinute();

    List<Restaurant> restaurantsByName;
    List<Restaurant> restaurantsByAttribute;
    List<Restaurant> restaurantsByItemName;
    List<Restaurant> restaurantsByItemAttribute;
    Double servingRadius;
    if ((h >= 8 && h <= 9) || (h == 10 && m == 0) || (h == 13) || (h == 14 && m == 0)
        || (h >= 19 && h < 21) || (h == 21 && m == 0)) {
      servingRadius = peakHoursServingRadiusInKms;
    } else {
      servingRadius = normalHoursServingRadiusInKms;
    }
    if (getRestaurantsRequest.getSearchFor() == null
        || getRestaurantsRequest.getSearchFor().isEmpty()) {
      return findAllRestaurantsCloseBy(getRestaurantsRequest, currentTime);
    }



    restaurantsByName = restaurantRepositoryService.findRestaurantsByName(
        getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(),
        getRestaurantsRequest.getSearchFor(), currentTime, servingRadius);

    restaurantsByAttribute = restaurantRepositoryService.findRestaurantsByAttributes(
        getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(),
        getRestaurantsRequest.getSearchFor(), currentTime, servingRadius);

    restaurantsByItemName = restaurantRepositoryService.findRestaurantsByItemName(
        getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(),
        getRestaurantsRequest.getSearchFor(), currentTime, servingRadius);

    restaurantsByItemAttribute = restaurantRepositoryService.findRestaurantsByItemAttributes(
        getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(),
        getRestaurantsRequest.getSearchFor(), currentTime, servingRadius);

    List<Restaurant> restaurants = new ArrayList<>();

    restaurants = Stream
        .of(restaurantsByName, restaurantsByAttribute, restaurantsByItemName,
            restaurantsByItemAttribute)
        .filter(list -> list != null && !list.isEmpty()).flatMap(List::stream)
        .collect(Collectors.toList());

    for (Restaurant restaurant : restaurants) {
      String sanitizedName = restaurant.getName().replaceAll("[Â©éí]", "e");
      restaurant.setName(sanitizedName);
    }

    GetRestaurantsResponse getRestaurantsResponse = new GetRestaurantsResponse(restaurants);
    log.info(getRestaurantsResponse);

    return getRestaurantsResponse;
  }



  // TODO: CRIO_TASK_MODULE_MULTITHREADING
  // Implement multi-threaded version of RestaurantSearch.
  // Implement variant of findRestaurantsBySearchQuery which is at least 1.5x time faster than
  // findRestaurantsBySearchQuery.
  @Override
  public GetRestaurantsResponse findRestaurantsBySearchQueryMt(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
    int h = currentTime.getHour();
    int m = currentTime.getMinute();

    // List<Restaurant> restaurantsByName;
    // List<Restaurant> restaurantsByAttribute;
    // List<Restaurant> restaurantsByItemName;
    // List<Restaurant> restaurantsByItemAttribute;

    Double servingRadius;
    if ((h >= 8 && h <= 9) || (h == 10 && m == 0) || (h == 13) || (h == 14 && m == 0)
        || (h >= 19 && h < 21) || (h == 21 && m == 0)) {
      servingRadius = peakHoursServingRadiusInKms;
    } else {
      servingRadius = normalHoursServingRadiusInKms;
    }
    if (getRestaurantsRequest.getSearchFor() == null
        || getRestaurantsRequest.getSearchFor().isEmpty()) {
      return findAllRestaurantsCloseBy(getRestaurantsRequest, currentTime);
    }

    // going to use parallelly call all these methods to gettrespoonse faster
    int threadPoolSize = Runtime.getRuntime().availableProcessors(); // Adjust the thread pool size
    // based on your requirements
    ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

    // List of methods to run in parallel
    List<Callable<List<Restaurant>>> tasks = new ArrayList<>();
    tasks.add(() -> restaurantRepositoryService.findRestaurantsByName(
        getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(),
        getRestaurantsRequest.getSearchFor(), currentTime, servingRadius));
    tasks.add(() -> restaurantRepositoryService.findRestaurantsByAttributes(
        getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(),
        getRestaurantsRequest.getSearchFor(), currentTime, servingRadius));
    tasks.add(() -> restaurantRepositoryService.findRestaurantsByItemName(
        getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(),
        getRestaurantsRequest.getSearchFor(), currentTime, servingRadius));
    tasks.add(() -> restaurantRepositoryService.findRestaurantsByItemAttributes(
        getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(),
        getRestaurantsRequest.getSearchFor(), currentTime, servingRadius));



    List<Restaurant> restaurants = new ArrayList<>();

    // Invoke all tasks in parallel
    List<Future<List<Restaurant>>> futures;
    try {
      futures = executorService.invokeAll(tasks);

      // Collect results
      List<List<Restaurant>> results = new ArrayList<>();
      for (Future<List<Restaurant>> future : futures) {
        results.add(future.get());
      }
      // Process the combined results as needed
      if (results.size() == 4) {
        restaurants = Stream.of(results.get(0), results.get(1), results.get(2), results.get(3))
            .filter(list -> list != null && !list.isEmpty()).flatMap(List::stream)
            .collect(Collectors.toList());
      }

    } catch (InterruptedException | ExecutionException e) {
      log.error(e.getMessage());
      throw new CustomQEatsException(e.getMessage());
    } finally {
      // Shutdown the executor service
      executorService.shutdown();
    }



    for (Restaurant restaurant : restaurants) {
      String sanitizedName = restaurant.getName().replaceAll("[Â©éí]", "e");
      restaurant.setName(sanitizedName);
    }

    GetRestaurantsResponse getRestaurantsResponse = new GetRestaurantsResponse(restaurants);
    log.info(getRestaurantsResponse);

    return getRestaurantsResponse;

  }
}


