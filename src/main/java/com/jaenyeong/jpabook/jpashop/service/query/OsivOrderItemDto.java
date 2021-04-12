package com.jaenyeong.jpabook.jpashop.service.query;

import com.jaenyeong.jpabook.jpashop.domain.OrderItem;
import lombok.Data;

@Data
public class OsivOrderItemDto {
    private String itemName;
    private int orderPrice;
    private int count;

    OsivOrderItemDto(final OrderItem orderItem) {
        this.itemName = orderItem.getItem().getName();
        this.orderPrice = orderItem.getOrderPrice();
        this.count = orderItem.getCount();
    }
}
