package com.jaenyeong.jpabook.jpashop.repository;

import com.jaenyeong.jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    // EntityManager는 @Autowired 애너테이션으로 주입이 안되나 스프링 부트가 주입 해줌
    // 원래는 @PersistenceContext 사용해야 함
    // Spring Data JPA가 생성자 주입을 해주기 때문에 @PersistenceContext 애너테이션 주석 처리
//    @PersistenceContext
    private final EntityManager em;

    // 팩토리 매니저를 직접 주입 받는 것도 가능
//    @PersistenceUnit
//    private EntityManagerFactory emf;

    public void save(final Member member) {
        em.persist(member);
    }

    public Member findOne(final Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
            .getResultList();
    }

    public List<Member> findByName(final String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
            .setParameter("name", name)
            .getResultList();
    }
}
