package com.BookShop_Backend.services.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.BookShop_Backend.DTO.Production.OrderPaymentRequestDTO;
import com.BookShop_Backend.DTO.Production.OrderPaymentResponseDTO;
import com.BookShop_Backend.DTO.Production.OrderPaymentStatusDTO;
import com.BookShop_Backend.DTO.User.MyUserDetail;
import com.BookShop_Backend.customexceptions.BusinessException;
import com.BookShop_Backend.enums.PaymentStatus;
import com.BookShop_Backend.models.BookEntity;
import com.BookShop_Backend.models.CartEntity;
import com.BookShop_Backend.models.CartItemEntity;
import com.BookShop_Backend.models.OrderEntity;
import com.BookShop_Backend.models.OrderItemEntity;
import com.BookShop_Backend.models.UserEntity;
import com.BookShop_Backend.repositories.CartItemRepository;
import com.BookShop_Backend.repositories.CartRepository;
import com.BookShop_Backend.repositories.BookRepository;
import com.BookShop_Backend.repositories.OrderItemRepository;
import com.BookShop_Backend.repositories.OrderRepository;
import com.BookShop_Backend.repositories.UserRepository;
import com.BookShop_Backend.security.SecurityUtils;
import com.BookShop_Backend.services.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.v2.paymentRequests.PaymentLinkStatus;
import vn.payos.model.webhooks.WebhookData;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {
    // Bean PayOS da duoc khoi tao tu PayOSConfig voi clientId/apiKey/checksumKey.
    private final PayOS payOS;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrderPaymentResponseDTO createPayment(OrderPaymentRequestDTO requestDTO, String baseUrl) {
        // Flow tao thanh toan:
        // 1. Lay user dang login va gio hang hien tai.
        // 2. Validate ton kho, tinh tong tien, convert CartItem -> OrderItem.
        // 3. Tao Order trong DB voi trang thai UNPAID.
        // 4. Goi PayOS tao payment link va tra QR cho client.
        MyUserDetail principal = getCurrentUser();
        UserEntity user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Khong tim thay user", HttpStatus.NOT_FOUND));

        CartEntity cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("CART_NOT_FOUND", "Khong tim thay gio hang", HttpStatus.NOT_FOUND));

        List<CartItemEntity> cartItems = cartItemRepository.findAllByCart_Id(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BusinessException("EMPTY_CART", "Gio hang dang trong");
        }

        List<PaymentLinkItem> paymentItems = new ArrayList<>();
        List<OrderItemEntity> orderItems = new ArrayList<>();
        double totalPrice = 0D;
        long totalPayOSAmount = 0L;

        for (CartItemEntity cartItem : cartItems) {
            BookEntity book = cartItem.getBook();
            if (book == null) {
                throw new BusinessException("BOOK_NOT_FOUND", "San pham trong gio hang khong hop le", HttpStatus.BAD_REQUEST);
            }
            if (book.getStatus() == null || book.getStatus() != 1) {
                throw new BusinessException("BOOK_NOT_FOUND", "San pham trong gio hang da bi vo hieu hoa", HttpStatus.BAD_REQUEST);
            }
            if (book.getStock() == null || cartItem.getQuantity() > book.getStock()) {
                throw new BusinessException("INVALID_CARTITEM", "So luong ton kho khong du", HttpStatus.BAD_REQUEST);
            }

            long itemPrice = toPayOSAmount(book.getPrice());
            totalPrice += book.getPrice() * cartItem.getQuantity();
            totalPayOSAmount += itemPrice * cartItem.getQuantity();

            // Day la danh sach item gui sang PayOS de hien thi trong payment link.
            paymentItems.add(PaymentLinkItem.builder()
                    .name(book.getName())
                    .quantity(cartItem.getQuantity())
                    .price(itemPrice)
                    .build());
        }

        long orderCode = generateOrderCode();
        // Tao don hang noi bo truoc, sau do moi tao link PayOS de giu duoc lich su UNPAID.
        OrderEntity order = OrderEntity.builder()
                .orderCode(orderCode)
                .user(user)
                .totalPrice(totalPrice)
                .paymentStatus(PaymentStatus.khiUNPAID)
                .address(requestDTO.getShippingAddress().trim())
                .phoneNumber(requestDTO.getPhoneNumber().trim())
                .build();
        orderRepository.save(order);

        for (CartItemEntity cartItem : cartItems) {
            BookEntity book = cartItem.getBook();
            // Chot lai gia tai thoi diem mua de ve sau gia sach co doi thi order van dung.
            orderItems.add(OrderItemEntity.builder()
                    .order(order)
                    .book(book)
                    .quantity(cartItem.getQuantity())
                    .price(book.getPrice())
                    .build());
        }
        orderItemRepository.saveAll(orderItems);

        try {
            // payment link nay la du lieu PayOS dung de sinh QR/checkoutUrl cho client.
            CreatePaymentLinkRequest paymentRequest = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(totalPayOSAmount)
                    .description(buildDescription(orderCode))
                    .returnUrl(resolveReturnUrl(requestDTO.getReturnUrl(), baseUrl))
                    .cancelUrl(resolveCancelUrl(requestDTO.getCancelUrl(), baseUrl))
                    .buyerName(user.getFullName())
                    .buyerPhone(requestDTO.getPhoneNumber().trim())
                    .buyerAddress(requestDTO.getShippingAddress().trim())
                    .items(paymentItems)
                    .build();

            CreatePaymentLinkResponse paymentLink = payOS.paymentRequests().create(paymentRequest);

            return OrderPaymentResponseDTO.builder()
                    .orderId(order.getId())
                    .orderCode(order.getOrderCode())
                    .totalPrice(order.getTotalPrice())
                    .paymentStatus(order.getPaymentStatus().name())
                    .payosStatus(paymentLink.getStatus().getValue())
                    .checkoutUrl(paymentLink.getCheckoutUrl())
                    .qrCode(paymentLink.getQrCode())
                    .message("Tao ma QR thanh cong, don hang dang o trang thai UNPAID")
                    .build();
        } catch (Exception ex) {
            throw new BusinessException("PAYMENT_LINK_CREATE_FAILED", "Khong the tao lien ket thanh toan PayOS", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public OrderPaymentResponseDTO createPayLaterOrder(OrderPaymentRequestDTO requestDTO) {
        MyUserDetail principal = getCurrentUser();
        UserEntity user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Khong tim thay user", HttpStatus.NOT_FOUND));

        CartEntity cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("CART_NOT_FOUND", "Khong tim thay gio hang", HttpStatus.NOT_FOUND));

        List<CartItemEntity> cartItems = cartItemRepository.findAllByCart_Id(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BusinessException("EMPTY_CART", "Gio hang dang trong");
        }

        double totalPrice = 0D;
        for (CartItemEntity cartItem : cartItems) {
            BookEntity book = cartItem.getBook();
            if (book == null) {
                throw new BusinessException("BOOK_NOT_FOUND", "San pham trong gio hang khong hop le", HttpStatus.BAD_REQUEST);
            }
            if (book.getStatus() == null || book.getStatus() != 1) {
                throw new BusinessException("BOOK_NOT_FOUND", "San pham trong gio hang da bi vo hieu hoa", HttpStatus.BAD_REQUEST);
            }
            if (cartItem.getQuantity() == null || cartItem.getQuantity() <= 0) {
                throw new BusinessException("INVALID_CARTITEM", "So luong san pham khong hop le", HttpStatus.BAD_REQUEST);
            }
            if (book.getStock() == null || cartItem.getQuantity() > book.getStock()) {
                throw new BusinessException("INVALID_CARTITEM", "So luong ton kho khong du", HttpStatus.BAD_REQUEST);
            }
            totalPrice += book.getPrice() * cartItem.getQuantity();
        }

        OrderEntity order = OrderEntity.builder()
                .orderCode(generateOrderCode())
                .user(user)
                .totalPrice(totalPrice)
                .paymentStatus(PaymentStatus.PAY_LATER)
                .address(requestDTO.getShippingAddress().trim())
                .phoneNumber(requestDTO.getPhoneNumber().trim())
                .build();
        orderRepository.save(order);

        List<OrderItemEntity> orderItems = new ArrayList<>();
        for (CartItemEntity cartItem : cartItems) {
            BookEntity book = cartItem.getBook();
            orderItems.add(OrderItemEntity.builder()
                    .order(order)
                    .book(book)
                    .quantity(cartItem.getQuantity())
                    .price(book.getPrice())
                    .build());
        }
        orderItemRepository.saveAll(orderItems);

        deductStockForOrder(order);
        removePaidItemsFromCart(order);

        return OrderPaymentResponseDTO.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .totalPrice(order.getTotalPrice())
                .paymentStatus(order.getPaymentStatus().name())
                .payosStatus(PaymentStatus.PAY_LATER.name())
                .message("Tao don hang tra sau thanh cong")
                .build();
    }


    @Override
    public OrderPaymentStatusDTO getPaymentStatus(Long orderCode) {
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Khong tim thay don hang", HttpStatus.NOT_FOUND));

        // Chi chu don hoac admin moi duoc xem trang thai thanh toan.
        ensureCanAccessOrder(order);

        String payOSStatus = null;
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            try {
                // Neu DB chua cap nhat PAID thi hoi lai PayOS de dong bo trang thai.
                PaymentLink paymentLink = payOS.paymentRequests().get(orderCode);
                payOSStatus = paymentLink.getStatus().getValue();
                if (paymentLink.getStatus() == PaymentLinkStatus.PAID) {
                    markOrderAsPaid(order);
                }
            } catch (Exception ignored) {
                payOSStatus = "UNKNOWN";
            }
        }

        if (!StringUtils.hasText(payOSStatus)) {
            payOSStatus = order.getPaymentStatus() == PaymentStatus.PAID ? PaymentLinkStatus.PAID.getValue() : PaymentLinkStatus.PENDING.getValue();
        }

        return buildStatusResponse(order, payOSStatus);
    }

    @Override
    @Transactional
    public OrderPaymentStatusDTO handlePayOSWebhook(Object body) throws JsonProcessingException {
        try {
            // Verify webhook de chac chan callback dung la tu PayOS gui sang.
            WebhookData webhookData = payOS.webhooks().verify(body);
            OrderEntity order = orderRepository.findByOrderCode(webhookData.getOrderCode())
                    .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Khong tim thay don hang tu webhook", HttpStatus.NOT_FOUND));

            // Sau khi verify, query lai PayOS de lay trang thai thanh toan "nguon su that".
            PaymentLink paymentLink = payOS.paymentRequests().get(webhookData.getOrderCode());
            String payOSStatus = paymentLink.getStatus().getValue();
            if (paymentLink.getStatus() == PaymentLinkStatus.PAID) {
                markOrderAsPaid(order);
            }

            return buildStatusResponse(order, payOSStatus);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("PAYMENT_WEBHOOK_FAILED", "Khong the xu ly webhook PayOS", HttpStatus.BAD_REQUEST);
        }
    }

    private void markOrderAsPaid(OrderEntity order) {
        // Webhook co the goi lap lai, nen chi update khi order chua o trang thai PAID.
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.PAID);
            orderRepository.save(order);
            removePaidItemsFromCart(order);
        }
    }

    private void removePaidItemsFromCart(OrderEntity order) {
        CartEntity cart = cartRepository.findByUserId(order.getUser().getId()).orElse(null);
        if (cart == null) {
            return;
        }

        List<CartItemEntity> currentCartItems = cartItemRepository.findAllByCart_Id(cart.getId());
        List<OrderItemEntity> paidOrderItems = orderItemRepository.findAllByOrder_Id(order.getId());

        for (OrderItemEntity orderItem : paidOrderItems) {
            for (CartItemEntity cartItem : currentCartItems) {
                if (!cartItem.getBook().getId().equals(orderItem.getBook().getId())) {
                    continue;
                }

                int remainingQuantity = cartItem.getQuantity() - orderItem.getQuantity();
                if (remainingQuantity <= 0) {
                    cartItemRepository.delete(cartItem);
                } else {
                    cartItem.setQuantity(remainingQuantity);
                    cartItemRepository.save(cartItem);
                }
                break;
            }
        }
    }

    private void deductStockForOrder(OrderEntity order) {
        List<OrderItemEntity> orderItems = orderItemRepository.findAllByOrder_Id(order.getId());
        for (OrderItemEntity orderItem : orderItems) {
            BookEntity book = orderItem.getBook();
            if (book == null) {
                throw new BusinessException("BOOK_NOT_FOUND", "San pham trong don hang khong hop le", HttpStatus.BAD_REQUEST);
            }
            if (book.getStock() == null || book.getStock() < orderItem.getQuantity()) {
                throw new BusinessException("INVALID_CARTITEM", "So luong ton kho khong du", HttpStatus.BAD_REQUEST);
            }
            book.setStock(book.getStock() - orderItem.getQuantity());
            bookRepository.save(book);
        }
    }

    private OrderPaymentStatusDTO buildStatusResponse(OrderEntity order, String payOSStatus) {
        String message = order.getPaymentStatus() == PaymentStatus.PAID
                ? "Thanh toan thanh cong"
                : "Don hang chua thanh toan";

        return OrderPaymentStatusDTO.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .paymentStatus(order.getPaymentStatus().name())
                .payosStatus(payOSStatus)
                .message(message)
                .build();
    }

    private void ensureCanAccessOrder(OrderEntity order) {
        MyUserDetail principal = getCurrentUser();
        boolean isAdmin = SecurityUtils.getAuthorities().contains("ROLE_ADMIN");
        if (!isAdmin && !order.getUser().getId().equals(principal.getId())) {
            throw new BusinessException("ORDER_NOT_FOUND", "Khong tim thay don hang", HttpStatus.NOT_FOUND);
        }
    }

    private MyUserDetail getCurrentUser() {
        MyUserDetail principal = SecurityUtils.getPrincipal();
        if (principal == null) {
            throw new BusinessException("UNAUTHORIZED", "Vui long dang nhap", HttpStatus.UNAUTHORIZED);
        }
        return principal;
    }

    private String resolveReturnUrl(String returnUrl, String baseUrl) {
        if (StringUtils.hasText(returnUrl)) {
            return returnUrl.trim();
        }
        return baseUrl + "/payment/success";
    }

    private String resolveCancelUrl(String cancelUrl, String baseUrl) {
        if (StringUtils.hasText(cancelUrl)) {
            return cancelUrl.trim();
        }
        return baseUrl + "/payment/cancel";
    }

    private String buildDescription(Long orderCode) {
        // Mo ta ngan gon de de truy vet giao dich tren dashboard PayOS.
        return "DH" + orderCode;
    }

    private long generateOrderCode() {
        // orderCode nay duoc dung chung cho DB noi bo va PayOS.
        long candidate = System.currentTimeMillis();
        while (orderRepository.findByOrderCode(candidate).isPresent()) {
            candidate++;
        }
        return candidate;
    }

    private long toPayOSAmount(Double amount) {
        // SDK PayOS nhan amount dang Long, project hien lam tron tu gia Double.
        if (amount == null || amount <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "So tien thanh toan khong hop le", HttpStatus.BAD_REQUEST);
        }
        return Math.round(amount);
    }
}
