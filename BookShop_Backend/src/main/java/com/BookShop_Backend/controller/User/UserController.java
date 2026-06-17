package com.BookShop_Backend.controller.User;

import com.BookShop_Backend.DTO.User.*;
import com.BookShop_Backend.services.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @PostMapping("/api/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        userService.register(userLoginDTO);
        return ResponseEntity.ok("Dang ki thanh cong");
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDTO userLoginDTO, HttpServletResponse response) {
        Map<String, String> tokens = userService.login(userLoginDTO.getPhoneNumber(), userLoginDTO.getPassword());

        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokens.get("refreshToken"))
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(86400)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(Map.of("accessToken", tokens.get("accessToken")));
    }

    @PostMapping("/api/update-password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdatePasswordDTO updatePasswordDTO) {
        userService.updatePassword(updatePasswordDTO);
        return ResponseEntity.ok("cap nhat thanh cong");
    }

    @GetMapping("/api/profile")
    public ResponseEntity<?> profile() {
        UserInfoResponseDTO userInfoResponseDTO = userService.getProfile();
        return ResponseEntity.ok(userInfoResponseDTO);
    }

    @PostMapping("/api/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String accessToken = userService.refreshToken(request);
        return ResponseEntity.ok(Map.of("accessToken", accessToken));
    }

    @PostMapping("/api/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        userService.logout(request);
        return ResponseEntity.ok(Map.of("message", "Dang xuat thanh cong!"));
    }

    @PatchMapping("/api/update-info")
    public ResponseEntity<?> updateInfo(@Valid @RequestBody UpdateInfoDTO updateInfoDTO) {
        userService.updateInfo(updateInfoDTO);
        return ResponseEntity.ok("cap nhat thanh cong");
    }
}
