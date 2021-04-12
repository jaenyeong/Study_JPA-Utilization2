package com.jaenyeong.jpabook.jpashop.repository.order.query;

import com.jaenyeong.jpabook.jpashop.domain.Address;
import com.jaenyeong.jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderQueryDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;
    private List<OrderItemQueryDto> orderItems;

    public OrderQueryDto(final Long orderId, final String name, final LocalDateTime orderDate, final OrderStatus orderStatus,
                         final Address address) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
    }
}
