package com.example.bai3.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "shopping_carts")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ShoppingCart {
    @Id
    private String id;
    private String userId;
    private List<CartItem> items;

}
