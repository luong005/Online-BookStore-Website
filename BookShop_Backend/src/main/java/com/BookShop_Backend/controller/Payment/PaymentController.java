package com.BookShop_Backend.controller.Payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.BookShop_Backend.services.IPaymentService;
import com.BookShop_Backend.type.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final IPaymentService paymentService;

    // Day la URL redirect mac dinh sau khi thanh toan thanh cong neu client khong truyen returnUrl rieng.
    @GetMapping("/success")
    public ResponseEntity<?> success() {
        return ResponseEntity.ok(ApiResponse.success("Redirect thanh cong sau khi thanh toan PayOS"));
    }

    // Day la URL redirect mac dinh khi user huy giao dich tren PayOS.
    @GetMapping("/cancel")
    public ResponseEntity<?> cancel() {
        return ResponseEntity.ok(ApiResponse.success("Thanh toan PayOS da bi huy"));
    }

    // Webhook la diem PayOS callback vao server de thong bao trang thai thanh toan.
    @PostMapping(path = "/payos_transfer_handler")
    public ResponseEntity<?> payosTransferHandler(@RequestBody Object body)
            throws JsonProcessingException, IllegalArgumentException {
        return ResponseEntity.ok(ApiResponse.success("Webhook processed", paymentService.handlePayOSWebhook(body)));
    }
}
