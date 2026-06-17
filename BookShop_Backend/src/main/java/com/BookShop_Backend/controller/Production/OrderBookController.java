package com.BookShop_Backend.controller.Production;

import com.BookShop_Backend.DTO.Production.OrderPaymentRequestDTO;
import com.BookShop_Backend.services.IPaymentService;
import com.BookShop_Backend.services.IOrderService;
import com.BookShop_Backend.type.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
public class OrderBookController {
    private final IOrderService orderService;
    private final IPaymentService paymentService;

    // Client gui shipping info vao endpoint nay de bat dau flow thanh toan.
    // Controller khong xu ly business, chi truyen request xuong PaymentService.
    @PostMapping("/order/payment")
    public ResponseEntity<?> payment(@Valid @RequestBody OrderPaymentRequestDTO requestDTO,
                                     HttpServletRequest request){
        // Dung domain hien tai de sinh returnUrl/cancelUrl mac dinh neu client khong gui len.
        String baseUrl = getBaseUrl(request);
        return ResponseEntity.ok(ApiResponse.success(paymentService.createPayment(requestDTO, baseUrl)));
    }

    // Frontend goi lai endpoint nay de poll trang thai sau khi user quet QR.
    @GetMapping("/order/payment-status/{orderCode}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long orderCode) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentStatus(orderCode)));
    }

    // xem lai lich su da mua
    @GetMapping("/orders")
    public ResponseEntity<?> getOrder(){
        return ResponseEntity.ok(orderService.getOrder());
    }

    @GetMapping("/order-{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable("id") Long id){
        return ResponseEntity.ok(orderService.getOrderDetail(id));
    }

//    @DeleteMapping("/order-{id}")
//    public ResponseEntity<?> deleteOrder(@PathVariable Long id){
//        orderService.deleteOrder(id);
//        return ResponseEntity.ok("Xoa thanh cong");
//    }

    // Helper nay ghep protocol + host + port thanh base URL cua request hien tai.
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        String url = scheme + "://" + serverName;
        if ((scheme.equals("http") && serverPort != 80)
                || (scheme.equals("https") && serverPort != 443)) {
            url += ":" + serverPort;
        }
        return url + contextPath;
    }
}
