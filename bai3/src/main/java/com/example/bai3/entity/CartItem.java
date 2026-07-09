package com.example.bai3.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CartItem {
    @Id
    private String id;
    @OneToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;
    private int quantity;
}
