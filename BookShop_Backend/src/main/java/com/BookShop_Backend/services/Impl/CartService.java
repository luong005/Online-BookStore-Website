package com.BookShop_Backend.services.Impl;


import com.BookShop_Backend.DTO.Production.CartCheckOutDTO;
import com.BookShop_Backend.DTO.Production.CartItemDTO;
import com.BookShop_Backend.DTO.Production.CartItemRequestDTO;
import com.BookShop_Backend.DTO.Production.UpdateQuantityCartItemDTO;
import com.BookShop_Backend.DTO.User.MyUserDetail;
import com.BookShop_Backend.converter.MapStruct;
import com.BookShop_Backend.customexceptions.BusinessException;
import com.BookShop_Backend.models.BookEntity;
import com.BookShop_Backend.models.CartEntity;
import com.BookShop_Backend.models.CartItemEntity;
import com.BookShop_Backend.models.UserEntity;
import com.BookShop_Backend.repositories.BookRepository;
import com.BookShop_Backend.repositories.CartItemRepository;
import com.BookShop_Backend.repositories.CartRepository;
import com.BookShop_Backend.repositories.UserRepository;
import com.BookShop_Backend.security.SecurityUtils;
import com.BookShop_Backend.services.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MapStruct mapStruct;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    public List<CartItemDTO> getCart() {
        MyUserDetail myUserDetail = SecurityUtils.getPrincipal();
        if (myUserDetail == null) {
            throw new BusinessException("UNAUTHORIZED", "Vui long dang nhap", HttpStatus.UNAUTHORIZED);
        }

        CartEntity cartEntity = cartRepository.findByUserId(myUserDetail.getId()).orElse(null);
        if (cartEntity == null) {
            UserEntity userEntity = userRepository.findById(myUserDetail.getId())
                    .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Khong tim thay user", HttpStatus.NOT_FOUND));

            cartRepository.save(CartEntity.builder().user(userEntity).build());
            return List.of();
        }

        List<CartItemEntity> cartItemEntities = cartItemRepository.findAllByCart_Id(cartEntity.getId());
        List<CartItemEntity> activeCartItems = cartItemEntities.stream()
                .filter(item -> item.getBook() != null && item.getBook().getStatus() != null && item.getBook().getStatus() == 1)
                .toList();
        return activeCartItems.stream()
                .map(mapStruct::toCartItemDTO)
                .toList();
    }

    @Override
    public void updateQuantity(UpdateQuantityCartItemDTO cartItem) {
        if(!cartItemRepository.existsById(cartItem.getId())){
            throw new BusinessException("CART_NOT-FOUND", "Hang nay khong ton tai");
        }
        CartItemEntity cartItemEntity = cartItemRepository.findById(cartItem.getId()).get();
        cartItemEntity.setQuantity(cartItem.getQuantity());
        BookEntity bookEntity = bookRepository.findActiveById(cartItemEntity.getBook().getId())
                .orElseThrow(() -> new BusinessException("INVALID_BOOK", "Sach khong con hieu luc", HttpStatus.BAD_REQUEST));
        if(cartItem.getQuantity() > bookEntity.getStock()){
            throw new BusinessException("INVALID_CARTITEM", "So luong ton kho khong du", HttpStatus.BAD_REQUEST);
        }
        cartItemRepository.save(cartItemEntity);
    }

    @Override
    public void deleteItem(Long id) {
        CartItemEntity cartItemEntity = cartItemRepository.findById(id).get();
        CartEntity cartEntity = cartRepository.findById(cartItemEntity.getCart().getId()).get();
        UserEntity userEntity = userRepository.findById(cartEntity.getUser().getId()).get();
        MyUserDetail myUserDetail = SecurityUtils.getPrincipal();
        if (!myUserDetail.getId().equals(userEntity.getId())){
            throw new BusinessException("DELETE_FAILED", "Mat hang khong ton tai", HttpStatus.BAD_REQUEST);
        }
        cartItemRepository.deleteById(id);
    }

    @Override
    public void insertItem(CartItemRequestDTO cartItemRequestDTO) {
        // ktra stock con du hang
        Optional<BookEntity> book = bookRepository.findActiveById(cartItemRequestDTO.getBookId());
        if(book.isEmpty()){
            throw new BusinessException("INVALID_BOOK", "Sach khong ton tai", HttpStatus.BAD_REQUEST);
        }
        if(cartItemRequestDTO.getQuantity()>book.get().getStock()){
            throw new BusinessException("INVALID_CARTITEM", "So luong ton kho khong du", HttpStatus.BAD_REQUEST);
        }

        // ktra gio hang user co mat hang nay chua ( ktra table CartItem xem chua user va bookId )
        MyUserDetail myUserDetail = SecurityUtils.getPrincipal();
        CartEntity cartEntity = cartRepository.findByUserId(myUserDetail.getId()).get();
        List<CartItemEntity> cartItemEntitys = cartItemRepository.findAllByCart_Id(cartEntity.getId());
        for (CartItemEntity item : cartItemEntitys) {
            if (item.getBook().getId().equals(cartItemRequestDTO.getBookId())) {
                int newQuantity = item.getQuantity() + cartItemRequestDTO.getQuantity();
                if (newQuantity > book.get().getStock()) {
                    throw new BusinessException("INVALID_CARTITEM", "So luong ton kho khong du", HttpStatus.BAD_REQUEST);
                }
                item.setQuantity(newQuantity);
                cartItemRepository.save(item);
                return;
            }
        }

        CartItemEntity cartItemEntity = CartItemEntity.builder()
                .cart(cartEntity)
                .book(book.get())
                .quantity(cartItemRequestDTO.getQuantity())
                .build();
        cartItemRepository.save(cartItemEntity);
    }

    @Override
    public CartCheckOutDTO checkout() {
        Double total_price = 0.0;
        MyUserDetail myUserDetail = SecurityUtils.getPrincipal();
        CartEntity cartEntity = cartRepository.findByUserId(myUserDetail.getId()).get();
        List<CartItemEntity> cartItemEntitys = cartItemRepository.findAllByCart_Id(cartEntity.getId());
        for(CartItemEntity item : cartItemEntitys){
            BookEntity bookEntity = bookRepository.findActiveById(item.getBook().getId())
                    .orElseThrow(() -> new BusinessException("INVALID_BOOK", "Co sach trong gio hang da bi vo hieu hoa", HttpStatus.BAD_REQUEST));

            total_price+=bookEntity.getPrice()*item.getQuantity();
        }
        List<CartItemDTO> cartItemDTOS = cartItemEntitys.stream()
                .map(mapStruct::toCartItemDTO)
                .toList();
        CartCheckOutDTO cartCheckOutDTO = CartCheckOutDTO.builder()
                .cartItemDTOS(cartItemDTOS)
                .totalPrice(total_price)
                .build();
        return cartCheckOutDTO;
    }
}
