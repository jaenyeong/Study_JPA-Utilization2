package com.jaenyeong.jpabook.jpashop.service.query;

import com.jaenyeong.jpabook.jpashop.domain.Address;
import com.jaenyeong.jpabook.jpashop.domain.Order;
import com.jaenyeong.jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Data
public class OsivOrderDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;
    private List<OsivOrderItemDto> orderItems;

    OsivOrderDto(final Order order) {
        this.orderId = order.getId();
        this.name = order.getMember().getName();
        this.orderDate = order.getOrderDate();
        this.orderStatus = order.getStatus();
        this.address = order.getDelivery().getAddress();

        // 지연 로딩 강제 초기화
        this.orderItems = order.getOrderItems().stream()
            .map(OsivOrderItemDto::new)
            .collect(toList());
    }
}
