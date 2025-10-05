package com.example.demo.member.domain;

import com.example.demo.order.domain.Order;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long MemberId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.PERSIST)
    private List<Order> orders = new ArrayList<>();

    public static Member create(String name, String email) {
        return Member.builder().name(name).email(email).build();
    }

    //연관관계 편의 메서드
    public void addOrder(Order order) {
        if (this.orders == null) this.orders = new ArrayList<>();
        this.orders.add(order);
    }
}
