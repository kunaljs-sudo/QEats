
/*
 *
 * * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.models;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import com.crio.qeats.dto.Item;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "menus")
public class MenuEntity implements Serializable {

  @Id
  private String id;

  @NotNull
  private String restaurantId;

  @NotNull
  private List<Item> items;



  public MenuEntity(String id, @NotNull String restaurantId, @NotNull List<Item> items) {
    this.id = id;
    this.restaurantId = restaurantId;
    this.items = items;
  }

  public MenuEntity() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getRestaurantId() {
    return restaurantId;
  }

  public void setRestaurantId(String restaurantId) {
    this.restaurantId = restaurantId;
  }

  public List<Item> getItems() {
    return items;
  }

  public void setItems(List<Item> items) {
    this.items = items;
  }



}
