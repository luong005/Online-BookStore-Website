package com.BookShop_Backend.converter;

import com.BookShop_Backend.DTO.Production.BookDTO;
import com.BookShop_Backend.DTO.Production.CartItemDTO;
import com.BookShop_Backend.DTO.Production.OrderItemResponseDTO;
import com.BookShop_Backend.DTO.Production.OrderResponseDTO;
import com.BookShop_Backend.DTO.User.UserInfoResponseDTO;
import com.BookShop_Backend.models.BookEntity;
import com.BookShop_Backend.models.CartItemEntity;
import com.BookShop_Backend.models.OrderEntity;
import com.BookShop_Backend.models.OrderItemEntity;
import com.BookShop_Backend.models.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MapStruct {

    @Mapping(target = "roleId", source = "role.id")
    UserInfoResponseDTO toUserInfoResponseDTO(UserEntity user);

    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "authorName", source = "author.name")
    @Mapping(target = "publisher", source = "publisher.name")
    BookDTO toBook(BookEntity bookEntity);

    @Mapping(target = "publisher", ignore = true)
    BookEntity toBookEntity(BookDTO bookDTO);

    @Mapping(target = "cartItemId", source = "id")
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookName", source = "book.name")
    CartItemDTO toCartItemDTO(CartItemEntity cartItemEntity);

    @Mapping(target = "shipping_address", source = "address")
    @Mapping(target = "phone_number", source = "phoneNumber")
    @Mapping(target = "total_price", source = "totalPrice")
    @Mapping(target = "payment_status", source = "paymentStatus")
    @Mapping(target = "order_code", source = "orderCode")
    @Mapping(target = "order_date", source = "createdAt")
    OrderResponseDTO toOrderResponseDTO(OrderEntity orderEntity);

    @Mapping(target = "orderItemId", source = "id")
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookName", source = "book.name")
    OrderItemResponseDTO toOrderItemResponseDTO(OrderItemEntity orderItemEntity);

}
