package com.BookShop_Backend.controller.Production;

import com.BookShop_Backend.DTO.Production.CartCheckOutDTO;
import com.BookShop_Backend.DTO.Production.CartItemDTO;
import com.BookShop_Backend.DTO.Production.CartItemRequestDTO;
import com.BookShop_Backend.DTO.Production.UpdateQuantityCartItemDTO;
import com.BookShop_Backend.services.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;

    @PostMapping("/cart/items")
    public ResponseEntity<?> insertItems(@RequestBody CartItemRequestDTO cartItemRequestDTO){
        cartService.insertItem(cartItemRequestDTO);
        return ResponseEntity.ok("Them thanh cong");
    }

    @GetMapping("/cart")
    public ResponseEntity<?> getCart(){
        List<CartItemDTO> cartItemDTOS = cartService.getCart();
        return ResponseEntity.ok(cartItemDTOS);
    }

    @PutMapping("/cart/item")
    public ResponseEntity<?> updateQuantity(@RequestBody UpdateQuantityCartItemDTO cartItem){
        cartService.updateQuantity(cartItem);
        return ResponseEntity.ok("update thanh cong");
    }

    @DeleteMapping("/cart/item-{id}")
    public ResponseEntity<?> deleteItems(@PathVariable Long id){
        cartService.deleteItem(id);
        return ResponseEntity.ok("Xoa thanh cong");
    }

    // bam thanh toan -> hien gia va form nhap info
    @GetMapping("/cart/checkout")
    public ResponseEntity<?> checkout(){
        CartCheckOutDTO cartCheckOutDTO = cartService.checkout();
        return ResponseEntity.ok(cartCheckOutDTO);
    }
}
