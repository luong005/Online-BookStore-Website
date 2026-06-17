package com.BookShop_Backend.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.BookShop_Backend.DTO.Production.OrderPaymentRequestDTO;
import com.BookShop_Backend.DTO.Production.OrderPaymentResponseDTO;
import com.BookShop_Backend.DTO.Production.OrderPaymentStatusDTO;

public interface IPaymentService {
    OrderPaymentResponseDTO createPayment(OrderPaymentRequestDTO requestDTO, String baseUrl);

    OrderPaymentStatusDTO getPaymentStatus(Long orderCode);

    OrderPaymentStatusDTO handlePayOSWebhook(Object body) throws JsonProcessingException;
}
