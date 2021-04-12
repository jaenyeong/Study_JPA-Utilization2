package com.jaenyeong.jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos() {
        final List<OrderQueryDto> orders = findOrders();

        orders.forEach(o -> o.setOrderItems(findOrderItems(o.getOrderId())));

        return orders;
    }

    // 이 방식은 쿼리가 총 2번 전송됨
    public List<OrderQueryDto> findAllByDto_optimization() {
        final List<OrderQueryDto> orders = findOrders();

        final Map<Long, List<OrderItemQueryDto>> orderItemsMap = findOrderItemsMap(toOrderIds(orders));

        orders.forEach(o -> o.setOrderItems(orderItemsMap.get(o.getOrderId())));

        return orders;
    }

    private List<Long> toOrderIds(final List<OrderQueryDto> orders) {
        return orders.stream()
            .map(OrderQueryDto::getOrderId)
            .collect(Collectors.toList());
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemsMap(final List<Long> orderIds) {
        final List<OrderItemQueryDto> orderItems = em.createQuery(
            "select new com.jaenyeong.jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"
                + " from OrderItem oi"
                + " join oi.item i"
                + " where oi.order.id in :orderIds"
            , OrderItemQueryDto.class)
            .setParameter("orderIds", orderIds)
            .getResultList();

        // 맵을 사용하여 메모리에서 핸들링
        final Map<Long, List<OrderItemQueryDto>> orderItemsMap = orderItems.stream()
            .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));
        return orderItemsMap;
    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
            "select new com.jaenyeong.jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)"
                + " from Order o"
                + " join o.member m"
                + " join o.delivery d", OrderQueryDto.class)
            .getResultList();
    }

    private List<OrderItemQueryDto> findOrderItems(final Long orderId) {
        return em.createQuery(
            "select new com.jaenyeong.jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"
                + " from OrderItem oi"
                + " join oi.item i"
                + " where oi.order.id = :orderId"
            , OrderItemQueryDto.class)
            .setParameter("orderId", orderId)
            .getResultList();
    }
}
