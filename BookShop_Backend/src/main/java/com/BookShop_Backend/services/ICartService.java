package com.BookShop_Backend.services;

import com.BookShop_Backend.DTO.Production.CartCheckOutDTO;
import com.BookShop_Backend.DTO.Production.CartItemDTO;
import com.BookShop_Backend.DTO.Production.CartItemRequestDTO;
import com.BookShop_Backend.DTO.Production.UpdateQuantityCartItemDTO;

import java.util.List;

public interface ICartService {
    List<CartItemDTO> getCart();

    void updateQuantity(UpdateQuantityCartItemDTO cartItem);

    void deleteItem(Long id);

    void insertItem(CartItemRequestDTO cartItemRequestDTO);

    CartCheckOutDTO checkout();
}
