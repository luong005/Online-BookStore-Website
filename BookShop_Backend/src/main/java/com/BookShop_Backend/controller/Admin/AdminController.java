package com.BookShop_Backend.controller.Admin;

import com.BookShop_Backend.DTO.Production.BestSellerResponseDTO;
import com.BookShop_Backend.DTO.User.UserInfoResponseDTO;
import com.BookShop_Backend.DTO.User.UserRequestDTO;
import com.BookShop_Backend.DTO.User.UserResponeDTO;
import com.BookShop_Backend.services.IOrderService;
import com.BookShop_Backend.services.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/admin/dashboard")
@RequiredArgsConstructor
public class AdminController {
    private final IOrderService orderService;
    private final IUserService userService;

    // tong doanh thu, tong user, tong so don hang(da thanh toan/ chua thanh toan)

    // tong doanh thu theo thang
    @GetMapping("/revenue-{month}")
    public ResponseEntity<?> getRevenue(@PathVariable Integer month){
        Double revenue = orderService.getRevenue(month);
        return ResponseEntity.ok(revenue);
    }

    // tra ra book ban nhieu nhat top 10
    @GetMapping("/best-selling-books-{top}")
    public ResponseEntity<?> bestSeller(@PathVariable Integer top){
        List<BestSellerResponseDTO> bestSellerResponseDTOList = orderService.getBestSeller(top);
        return ResponseEntity.ok(bestSellerResponseDTOList);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUsers(
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "role_id", required = false) Long roleId
    ) {
        UserRequestDTO userRequestDTO = UserRequestDTO.builder()
                .address(address)
                .role_id(roleId)
                .build();

        List<UserInfoResponseDTO> userInfoResponseDTOS = userService.getUsers(userRequestDTO);
        UserResponeDTO userResponeDTO = UserResponeDTO.builder()
                .userInfoResponseDTOS(userInfoResponseDTOS)
                .numberOfUsers(Long.valueOf(userInfoResponseDTOS.size()))
                .build();
        return ResponseEntity.ok(userResponeDTO);
    }
}
