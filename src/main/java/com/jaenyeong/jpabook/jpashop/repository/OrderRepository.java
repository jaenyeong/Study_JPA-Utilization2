package com.jaenyeong.jpabook.jpashop.repository;

import com.jaenyeong.jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(final Order order) {
        em.persist(order);
    }

    public Order findOne(final Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAll(final OrderSearch orderSearch) {
        // 동적 쿼리가 필요
        final TypedQuery<Order> orderTypedQuery = em.createQuery("select o from Order o join o.member m"
                + " where o.status = :status"
                + " and m.name like :name",
            Order.class)
            .setParameter("status", orderSearch.getOrderStatus())
            .setParameter("name", orderSearch.getMemberName())
//            .setFirstResult(100)
            .setMaxResults(1_000);

        return orderTypedQuery.getResultList();
    }

    public List<Order> findAllByCriteria(final OrderSearch orderSearch) {
        final CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        final CriteriaQuery<Order> criteriaQuery = criteriaBuilder.createQuery(Order.class);
        final Root<Order> o = criteriaQuery.from(Order.class);
        final Join<Object, Object> m = o.join("member", JoinType.INNER);

        final List<Predicate> criteria = new ArrayList<>();

        // 주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            final Predicate status = criteriaBuilder.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }

        // 회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            final Predicate name = criteriaBuilder.like(m.get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        criteriaQuery.where(criteriaBuilder.and(criteria.toArray(new Predicate[criteria.size()])));
        final TypedQuery<Order> resultQuery = em.createQuery(criteriaQuery).setMaxResults(1_000);

        return resultQuery.getResultList();
    }

    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
            "select o from Order o"
                + " join fetch o.member m"
                + " join fetch o.delivery d",
            Order.class)
            .getResultList();
    }

    public List<Order> findAllWithItem() {
        return em.createQuery(
            "select distinct o from Order o"
                + " join fetch o.member m"
                + " join fetch o.delivery d"
                + " join fetch o.orderItems oi"
                + " join fetch oi.item i", Order.class)
            // 페이징 명령
            .setFirstResult(1)
            .setMaxResults(100)
            .getResultList();
    }

    public List<Order> findAllWithMemberDelivery(final int offset, final int limit) {
        return em.createQuery(
            "select o from Order o"
                // default_batch_fetch_size 옵션으로 멤버와 딜리버리 생략해도 IN 쿼리로 가져오게 됨
                // 하지만 DB 쿼리 호출 수 때문에 *ToOne 연관 관계는 join fetch로 가져오는 것이 효율적
                + " join fetch o.member m"
                + " join fetch o.delivery d",
            Order.class)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
    }
}
