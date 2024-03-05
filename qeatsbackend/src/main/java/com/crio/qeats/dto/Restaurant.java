
/*
 *
 * * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// TODO: CRIO_TASK_MODULE_SERIALIZATION
// Implement Restaurant class.
// Complete the class such that it produces the following JSON during serialization.
// {
// "restaurantId": "10",
// "name": "A2B",
// "city": "Hsr Layout",
// "imageUrl": "www.google.com",
// "latitude": 20.027,
// "longitude": 30.0,
// "opensAt": "18:00",
// "closesAt": "23:00",
// "attributes": [
// "Tamil",
// "South Indian"
// ]
// }

@JsonIgnoreProperties(ignoreUnknown = true)
public class Restaurant {
    private String restaurantId;
    private String name;
    private String city;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private String opensAt;
    private String closesAt;
    private ArrayList<String> attributes;

    public Restaurant() {}


    public Restaurant(String restaurantId, String name, String city, String imageUrl,
            Double latitude, Double longitude, String opensAt, String closesAt,
            ArrayList<String> attributes) {
        this.restaurantId = restaurantId;
        this.name = name;
        this.city = city;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
        this.attributes = attributes;
    }


    public String getRestaurantId() {
        return restaurantId;
    }


    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getCity() {
        return city;
    }


    public void setCity(String city) {
        this.city = city;
    }


    public String getImageUrl() {
        return imageUrl;
    }


    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public Double getLatitude() {
        return latitude;
    }


    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }


    public Double getLongitude() {
        return longitude;
    }


    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }


    public String getOpensAt() {
        return opensAt;
    }


    public void setOpensAt(String opensAt) {
        this.opensAt = opensAt;
    }


    public String getClosesAt() {
        return closesAt;
    }


    public void setClosesAt(String closesAt) {
        this.closesAt = closesAt;
    }


    public ArrayList<String> getAttributes() {
        return attributes;
    }


    public void setAttributes(ArrayList<String> attributes) {
        this.attributes = attributes;
    }


    @Override
    public String toString() {
        return "Restaurant [restaurantId=" + restaurantId + ", name=" + name + ", city=" + city
                + ", imageUrl=" + imageUrl + ", latitude=" + latitude + ", longitude=" + longitude
                + ", opensAt=" + opensAt + ", closesAt=" + closesAt + ", attributes=" + attributes
                + "]";
    }

    



}

