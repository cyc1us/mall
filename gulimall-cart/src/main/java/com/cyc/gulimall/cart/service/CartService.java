package com.cyc.gulimall.cart.service;

import com.cyc.gulimall.cart.vo.Cart;
import com.cyc.gulimall.cart.vo.CartItem;

import java.util.List;

public interface CartService {

    CartItem addToCart(Long skuId, Integer num);

    CartItem getCartItem(Long skuId);

    Cart getCart();

    void clearCart(String cartKey);

    /*
    * 勾选购物项*/
    void checkItem(Long skuId, Integer check);

    void changeItemCount(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItem> getUserCartItems();
}
