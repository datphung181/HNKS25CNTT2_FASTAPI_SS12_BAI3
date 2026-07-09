package com.example.bai3.service;

import com.example.bai3.entity.CartItem;
import com.example.bai3.entity.Product;
import com.example.bai3.entity.ShoppingCart;
import com.example.bai3.repository.CartRepository;
import com.example.bai3.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ShoppingCartService {
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    public void addProductToCart(
            String userId,
            String productId,
            int quantity
    ) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than 0");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found"));

        if (quantity > product.getStock()) {
            throw new IllegalArgumentException(
                    "Insufficient stock");
        }

        ShoppingCart cart = cartRepository
                .findByUserId(userId)
                .orElse(new ShoppingCart(
                        UUID.randomUUID().toString(),
                        userId,
                        new ArrayList<>()));

        Optional<CartItem> existingItem =
                cart.getItems()
                        .stream()
                        .filter(i ->
                                i.getProduct()
                                        .getId()
                                        .equals(productId))
                        .findFirst();

        if (existingItem.isPresent()) {

            CartItem item = existingItem.get();

            int newQuantity =
                    item.getQuantity() + quantity;

            if (newQuantity > product.getStock()) {
                throw new IllegalArgumentException(
                        "Insufficient stock");
            }

            item.setQuantity(newQuantity);

        } else {

            cart.getItems()
                    .add(new CartItem(null, product, quantity));
        }

        cartRepository.save(cart);
    }

    public void updateProductQuantity(
            String userId,
            String productId,
            int quantity
    ) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than 0");
        }

        Product product = productRepository
                .findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found"));

        if (quantity > product.getStock()) {
            throw new IllegalArgumentException(
                    "Insufficient stock");
        }

        ShoppingCart cart = cartRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cart not found"));

        CartItem item = cart.getItems()
                .stream()
                .filter(i ->
                        i.getProduct()
                                .getId()
                                .equals(productId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found in cart"));

        item.setQuantity(quantity);

        cartRepository.save(cart);
    }

    public void removeProductFromCart(
            String userId,
            String productId
    ) {

        ShoppingCart cart = cartRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cart not found"));
        cart.getItems()
                .removeIf(i ->
                        i.getProduct()
                                .getId()
                                .equals(productId));
        cartRepository.save(cart);
    }
}
