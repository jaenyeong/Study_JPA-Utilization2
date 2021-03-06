package com.jaenyeong.jpabook.jpashop.repository.order.simplequery;

import com.jaenyeong.jpabook.jpashop.repository.OrderSimpleQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;

    public List<OrderSimpleQueryDto> findAllToDtos() {
        // μ£Όμ fetch join μλ
        return em.createQuery(
            "select new com.jaenyeong.jpabook.jpashop.repository.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)"
                + " from Order o"
                + " join o.member m"
                + " join o.delivery d",
            OrderSimpleQueryDto.class)
            .getResultList();
    }
}
