package com.jaenyeong.jpabook.jpashop.service;

import com.jaenyeong.jpabook.jpashop.domain.Delivery;
import com.jaenyeong.jpabook.jpashop.domain.Member;
import com.jaenyeong.jpabook.jpashop.domain.Order;
import com.jaenyeong.jpabook.jpashop.domain.OrderItem;
import com.jaenyeong.jpabook.jpashop.domain.item.Item;
import com.jaenyeong.jpabook.jpashop.repository.ItemRepository;
import com.jaenyeong.jpabook.jpashop.repository.MemberRepository;
import com.jaenyeong.jpabook.jpashop.repository.OrderRepository;
import com.jaenyeong.jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Long order(final Long memberId, final Long itemId, final int count) {
        final Member member = memberRepository.findOne(memberId);
        final Item item = itemRepository.findOne(itemId);

        final Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        final OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        final Order order = Order.createOrder(member, delivery, orderItem);

        orderRepository.save(order);

        return order.getId();
    }

    @Transactional
    public void cancelOrder(final Long orderId) {
        final Order order = orderRepository.findOne(orderId);
        order.cancel();
    }

    public List<Order> findOrders(final OrderSearch orderSearch) {
        return orderRepository.findAllByCriteria(orderSearch);
    }
}
