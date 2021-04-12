package com.jaenyeong.jpabook.jpashop.api;

import com.jaenyeong.jpabook.jpashop.domain.Address;
import com.jaenyeong.jpabook.jpashop.domain.Order;
import com.jaenyeong.jpabook.jpashop.domain.OrderItem;
import com.jaenyeong.jpabook.jpashop.domain.OrderStatus;
import com.jaenyeong.jpabook.jpashop.repository.OrderRepository;
import com.jaenyeong.jpabook.jpashop.repository.OrderSearch;
import com.jaenyeong.jpabook.jpashop.repository.order.query.OrderFlatDto;
import com.jaenyeong.jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import com.jaenyeong.jpabook.jpashop.repository.order.query.OrderQueryDto;
import com.jaenyeong.jpabook.jpashop.repository.order.query.OrderQueryRepository;
import com.jaenyeong.jpabook.jpashop.service.query.OrderQueryService;
import com.jaenyeong.jpabook.jpashop.service.query.OsivOrderDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
    private final OrderQueryService orderQueryService;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        final List<Order> allOrders = orderRepository.findAllByCriteria(new OrderSearch());
        for (Order order : allOrders) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            order.getOrderItems().forEach(o -> o.getItem().getName());
        }

        return allOrders;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        final List<Order> allOrders = orderRepository.findAllByCriteria(new OrderSearch());

        return allOrders.stream()
            .map(OrderDto::new)
            .collect(toList());
    }

    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        OrderDto(final Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();

            // 지연 로딩 강제 초기화
//            order.getOrderItems().forEach(o -> o.getItem().getName());
            this.orderItems = order.getOrderItems().stream()
                .map(OrderItemDto::new)
                .collect(toList());
        }
    }

    @Data
    static class OrderItemDto {
        private String itemName;
        private int orderPrice;
        private int count;

        OrderItemDto(final OrderItem orderItem) {
            this.itemName = orderItem.getItem().getName();
            this.orderPrice = orderItem.getOrderPrice();
            this.count = orderItem.getCount();
        }
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        final List<Order> allOrdersWithItem = orderRepository.findAllWithItem();

        return allOrdersWithItem.stream()
            .map(OrderDto::new)
            .collect(toList());
    }

    @GetMapping("/api/v3-1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") final int offset,
                                        @RequestParam(value = "limit", defaultValue = "100") final int limit) {

        final List<Order> allOrders = orderRepository.findAllWithMemberDelivery(offset, limit);

        return allOrders.stream()
            .map(OrderDto::new)
            .collect(toList());
    }

    @GetMapping("/api/v3-osiv/orders")
    public List<OsivOrderDto> ordersV3Osiv() {
        return orderQueryService.ordersV3();
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        final List<OrderFlatDto> allOrders = orderQueryRepository.findAllByDto_flat();

        return allOrders.stream()
            .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())))
            .entrySet().stream()
            .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                e.getKey().getAddress(), e.getValue()))
            .collect(toList());
    }
}
