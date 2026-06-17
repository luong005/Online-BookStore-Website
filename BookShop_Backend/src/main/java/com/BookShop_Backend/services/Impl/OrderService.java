package com.BookShop_Backend.services.Impl;

import com.BookShop_Backend.DTO.Production.BestSellerResponseDTO;
import com.BookShop_Backend.DTO.Production.BookDTO;
import com.BookShop_Backend.DTO.Production.OrderItemResponseDTO;
import com.BookShop_Backend.DTO.Production.OrderResponseDTO;
import com.BookShop_Backend.DTO.User.MyUserDetail;
import com.BookShop_Backend.converter.MapStruct;
import com.BookShop_Backend.customexceptions.BusinessException;
import com.BookShop_Backend.enums.PaymentStatus;
import com.BookShop_Backend.models.BookEntity;
import com.BookShop_Backend.models.OrderEntity;
import com.BookShop_Backend.models.OrderItemEntity;
import com.BookShop_Backend.repositories.BookRepository;
import com.BookShop_Backend.repositories.OrderItemRepository;
import com.BookShop_Backend.repositories.OrderRepository;
import com.BookShop_Backend.security.SecurityUtils;
import com.BookShop_Backend.services.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BookRepository bookRepository;
    private final MapStruct mapStruct;

    @Override
    public Double getRevenue(Integer month) {
        Double revenue = 0.0;
        List<OrderEntity> orderEntities = orderRepository.findAllByMonthAndPaymentStatus(month, PaymentStatus.PAID);
        for (OrderEntity item : orderEntities){
            revenue +=item.getTotalPrice();
        }
        return revenue;
    }

    @Override
    public List<BestSellerResponseDTO> getBestSeller(Integer top) {
        List<Object[]> objects = orderItemRepository.findTop10BestSellingBookIds(PaymentStatus.PAID, PageRequest.of(0, top));
        List<BestSellerResponseDTO> bestSellerResponseDTOList = new ArrayList<>();
        for(Object[] item: objects){
            Long bookId = ((Number) item[0]).longValue();
            Long totalQuantity = ((Number) item[1]).longValue();
            Optional<BookEntity> bookEntity = bookRepository.findActiveById(bookId);
            if (bookEntity.isEmpty()) {
                continue;
            }
            BookEntity book = bookEntity.get();
            BookDTO bookDTO = mapStruct.toBook(book);
            BestSellerResponseDTO bestSellerResponseDTO = BestSellerResponseDTO.builder()
                    .bookDTOS(bookDTO)
                    .total_quantity(totalQuantity)
                    .build();
            bestSellerResponseDTOList.add(bestSellerResponseDTO);
        }
        return bestSellerResponseDTOList;
    }

    @Override
    public List<OrderResponseDTO> getOrder() {
        MyUserDetail myUserDetail = SecurityUtils.getPrincipal();
        List<OrderEntity> orderEntities = orderRepository.findAllByUser_Id(myUserDetail.getId());
        return orderEntities.stream()
                .map(mapStruct::toOrderResponseDTO)
                .toList();
    }

    @Override
    public List<OrderItemResponseDTO> getOrderDetail(Long orderId) {
        List<OrderItemEntity> orderItemEntities = orderItemRepository.findAllByOrder_Id(orderId);
        OrderEntity orderEntity = orderRepository.findById(orderId).get();
        MyUserDetail myUserDetail = SecurityUtils.getPrincipal();
        if(!orderEntity.getUser().getId().equals(myUserDetail.getId())){
            throw new BusinessException("ORDER_NOT_FOUND","Lich su mua hang khong ton tai", HttpStatus.NOT_FOUND);
        }
        return orderItemEntities.stream()
                .map(mapStruct::toOrderItemResponseDTO)
                .toList();
    }

    @Override
    public void deleteOrder(Long orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId).get();
        MyUserDetail myUserDetail = SecurityUtils.getPrincipal();
        if(!orderEntity.getUser().getId().equals(myUserDetail.getId())){
            throw new BusinessException("ORDER_NOT_FOUND","Lich su mua hang khong ton tai", HttpStatus.NOT_FOUND);
        }
        orderRepository.deleteById(orderId);
    }
}
