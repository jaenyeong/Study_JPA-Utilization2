package com.jaenyeong.jpabook.jpashop.service.query;

import com.jaenyeong.jpabook.jpashop.domain.Order;
import com.jaenyeong.jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;

    public List<OsivOrderDto> ordersV3() {
        final List<Order> allOrdersWithItem = orderRepository.findAllWithItem();

        return allOrdersWithItem.stream()
            .map(OsivOrderDto::new)
            .collect(toList());
    }
}
