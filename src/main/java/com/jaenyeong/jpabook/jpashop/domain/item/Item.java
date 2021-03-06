package com.jaenyeong.jpabook.jpashop.domain.item;

import com.jaenyeong.jpabook.jpashop.domain.Category;
import com.jaenyeong.jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

// application.yml 파일에서 한 번에 설정
//@BatchSize(size = 1000)
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter
@Setter
public abstract class Item {
    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;

    private int price;

    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private final List<Category> categories = new ArrayList<>();

    public void addStock(final int quantity) {
        this.stockQuantity += quantity;
    }

    public void removeStock(final int quantity) {
        final int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {
            throw new NotEnoughStockException("need more stock");
        }

        this.stockQuantity = restStock;
    }
}
