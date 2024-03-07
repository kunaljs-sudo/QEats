
/*
 *
 * * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.dto;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;


public class Item {

  @Id
  private String id;

  @NotNull
  String itemId;

  @NotNull
  String name;

  @NotNull
  String imageUrl;

  @NotNull
  List<String> attributes = new ArrayList<>();

  @NotNull
  int price;

  public Item() {}

  public Item(String id, @NotNull String itemId, @NotNull String name, @NotNull String imageUrl,
      @NotNull @NotNull int price, @NotNull List<String> attributes) {
    this.id = id;
    this.itemId = itemId;
    this.name = name;
    this.imageUrl = imageUrl;
    this.price = price;
    this.attributes = attributes;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public @NotNull int getPrice() {
    return price;
  }

  public void setPrice(@NotNull int price) {
    this.price = price;
  }

  public List<String> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<String> attributes) {
    this.attributes = attributes;
  }

}
