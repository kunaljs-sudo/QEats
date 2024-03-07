package com.crio.qeats.exceptions;

public class CustomQEatsException extends QEatsException {

  static final int EMPTY_CART = 100;
  static final int ITEM_NOT_FOUND_IN_RESTAURANT_MENU = 101;
  static final int ITEM_NOT_FROM_SAME_RESTAURANT = 102;
  static final int CART_NOT_FOUND = 103;

  public CustomQEatsException(String msg) {
    super(msg);
  }

  public CustomQEatsException() {
    super();
  }

  @Override
  public int getErrorType() {
    return 0;
  }

}
