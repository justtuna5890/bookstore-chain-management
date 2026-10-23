package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.OrderLineDTO;
import com.bookstorechain.dto.response.OrderSummaryDTO;
import com.bookstorechain.entity.CustomerOrder;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/** Chuyển CustomerOrder sang DTO. Phải gọi trong transaction vì đọc các quan hệ lazy (chi nhánh, sách). */
@Component
public class OrderMapper {

    public OrderSummaryDTO toSummary(CustomerOrder order) {
        return OrderSummaryDTO.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .storeId(order.getStore().getId())
                .storeName(order.getStore().getName())
                .storeAddress(order.getStore().getAddress())
                .storePhone(order.getStore().getPhone())
                .customerName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .shippingAddress(order.getShippingAddress())
                .note(order.getNote())
                .status(order.getStatus())
                .fulfillmentType(order.getFulfillmentType())
                .paymentMethod(order.getPayment().getMethod())
                .paymentStatus(order.getPayment().getStatus())
                .transactionCode(order.getPayment().getTransactionCode())
                .paidAt(order.getPayment().getPaidAt())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .lines(order.getItems().stream()
                        .map(line -> OrderLineDTO.builder()
                                .copyId(line.getCopy().getId())
                                .bookId(line.getCopy().getBook().getId())
                                .title(line.getTitleSnapshot())
                                .copyCode(line.getCopyCodeSnapshot())
                                .price(line.getPriceSnapshot())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
