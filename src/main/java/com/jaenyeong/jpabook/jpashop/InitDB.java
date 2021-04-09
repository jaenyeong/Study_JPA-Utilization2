package com.jaenyeong.jpabook.jpashop;

import com.jaenyeong.jpabook.jpashop.domain.*;
import com.jaenyeong.jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

@Component
@RequiredArgsConstructor
public class InitDB {

    private final InitService initService;

    @PostConstruct
    public void postConstruct() {
        initService.dbInit1();
        initService.dbInit2();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {
        private final EntityManager em;

        // InitDB 클래스의 postConstruct 메서드 안에 위치 시키면 제대로 실행되지 않을 수 있음
        // 스프링의 빈(팩토리) 라이프 사이클로 인해 트랜잭션 처리가 꼬일 수 있음
        public void dbInit1() {
            final Member member = createMember("서울", "강서로", "07777");

            final Book book1 = createBook("JPA 1 Book", 10_000, 100);
            final Book book2 = createBook("JPA 2 Book", 20_000, 120);

            final OrderItem orderItem1 = OrderItem.createOrderItem(book1, book1.getPrice(), 1);
            final OrderItem orderItem2 = OrderItem.createOrderItem(book2, book2.getPrice(), 2);

            final Delivery delivery = createDelivery(member);

            final Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }

        public void dbInit2() {
            final Member member = createMember("김포", "애기봉로", "10011");

            final Book book1 = createBook("Spring 1 Book", 20_000, 200);
            final Book book2 = createBook("Spring 2 Book", 40_000, 300);

            final OrderItem orderItem1 = OrderItem.createOrderItem(book1, book1.getPrice(), 3);
            final OrderItem orderItem2 = OrderItem.createOrderItem(book2, book2.getPrice(), 4);

            final Delivery delivery = createDelivery(member);

            final Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }

        private Delivery createDelivery(final Member member) {
            final Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            return delivery;
        }

        private Member createMember(final String city, final String street, final String zipcode) {
            final Member member = new Member();
            member.setName("User A");
            member.setAddress(new Address(city, street, zipcode));
            em.persist(member);
            return member;
        }

        private Book createBook(final String name, final int price, final int stockQuantity) {
            final Book book = new Book();
            book.setName(name);
            book.setPrice(price);
            book.setStockQuantity(stockQuantity);
            em.persist(book);

            return book;
        }
    }
}
