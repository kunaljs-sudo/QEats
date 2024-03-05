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
import java.util.List;
import javax.inject.Provider;
import com.crio.qeats.configs.RedisConfiguration;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.globals.GlobalConstants;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoLocation;
import com.crio.qeats.utils.GeoUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;


@Service
public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {



	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private RestaurantRepository restaurantRepository;

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



}

