/*
 *
 * * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.exchanges;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonProperty;

// TODO: CRIO_TASK_MODULE_RESTAURANTSAPI
// Implement GetRestaurantsRequest.
// Complete the class such that it is able to deserialize the incoming query params from
// REST API clients.
// For instance, if a REST client calls API
// /qeats/v1/restaurants?latitude=28.4900591&longitude=77.536386&searchFor=tamil,
// this class should be able to deserialize lat/long and optional searchFor from that.
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class GetRestaurantsRequest {

    @NonNull
    @Max(value = 90)
    @Min(value = -90)
    private Double latitude;

    @NonNull
    @Max(value = 180)
    @Min(value = -180)
    private Double longitude;

    @JsonProperty(required = false)
    private String searchFor;




}

