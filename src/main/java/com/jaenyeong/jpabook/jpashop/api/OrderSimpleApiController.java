package com.jaenyeong.jpabook.jpashop.api;

import com.jaenyeong.jpabook.jpashop.domain.Order;
import com.jaenyeong.jpabook.jpashop.repository.OrderRepository;
import com.jaenyeong.jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
