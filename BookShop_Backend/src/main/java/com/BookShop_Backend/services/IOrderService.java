package com.BookShop_Backend.services;


import com.BookShop_Backend.DTO.Production.BestSellerResponseDTO;
import com.BookShop_Backend.DTO.Production.OrderItemResponseDTO;
import com.BookShop_Backend.DTO.Production.OrderResponseDTO;

import java.util.List;

public interface IOrderService {
    Double getRevenue(Integer month);

    List<BestSellerResponseDTO> getBestSeller(Integer top);

    List<OrderResponseDTO> getOrder();

    List<OrderItemResponseDTO> getOrderDetail(Long id);

    void deleteOrder(Long id);
}
