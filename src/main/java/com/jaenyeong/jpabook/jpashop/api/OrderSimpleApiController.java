package com.jaenyeong.jpabook.jpashop.api;

import com.jaenyeong.jpabook.jpashop.domain.Address;
import com.jaenyeong.jpabook.jpashop.domain.Order;
import com.jaenyeong.jpabook.jpashop.domain.OrderStatus;
import com.jaenyeong.jpabook.jpashop.repository.OrderRepository;
import com.jaenyeong.jpabook.jpashop.repository.OrderSearch;
import com.jaenyeong.jpabook.jpashop.repository.OrderSimpleQueryDto;
import com.jaenyeong.jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

/**
 * xToOne(ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        final List<Order> allOrders = orderRepository.findAllByCriteria(new OrderSearch());
        for (Order order : allOrders) {
            // 강제로 지연 로딩을 실행
            // getMember(), getDelivery()까지는 쿼리가 날라가지 않음
            // getName(), getAddress()까지 실행해야 쿼리가 날라감
            order.getMember().getName();
            order.getDelivery().getAddress();
        }

        return allOrders;
    }

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        final List<Order> allOrders = orderRepository.findAllByCriteria(new OrderSearch());

        return allOrders.stream()
            .map(SimpleOrderDto::new)
            .collect(toList());
    }

    @Data
    private static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        private SimpleOrderDto(final Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName(); // LAZY 초기화
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress(); // LAZY 초기화
        }
    }

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        final List<Order> allOrders = orderRepository.findAllWithMemberDelivery();

        return allOrders.stream()
            .map(SimpleOrderDto::new)
            .collect(toList());
    }

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findAllToDtos();
    }
}
