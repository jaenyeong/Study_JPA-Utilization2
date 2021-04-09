package com.jaenyeong.jpabook.jpashop.repository;

import com.jaenyeong.jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    // 해당 메서드는 식별자 자동 생성 전략(@GeneratedValue)이 적용되어 있어야만 정상 작동
    // @Id만 선언된 상태로 호출하면 식별자가 없는 상태로 persist를 호출하게 되면서 예외 발생
    public void save(final Item item) {
        if (item.getId() == null) {
            em.persist(item);
            return;
        }

        em.merge(item);
    }

    public Item findOne(final Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
            .getResultList();
    }
}
