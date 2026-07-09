package com.example.bai3;

import com.example.bai3.entity.CartItem;
import com.example.bai3.entity.Product;
import com.example.bai3.entity.ShoppingCart;
import com.example.bai3.repository.CartRepository;
import com.example.bai3.repository.ProductRepository;
import com.example.bai3.service.ShoppingCartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Profile("dev")
public class ShoppingCartServiceTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private ShoppingCartService shoppingCartService;

    private Product product;
    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        product = new Product(
                "P001",
                "Laptop",
                2000.0,
                10
        );

        cart = new ShoppingCart(
                "C001",
                "USER01",
                new ArrayList<>()
        );
    }

    @Test
    void addProductToExistingCart() {
        when(productRepository.findById("P001"))
                .thenReturn(Optional.of(product));
        when(cartRepository.findByUserId("USER01"))
                .thenReturn(Optional.of(cart));

        shoppingCartService.addProductToCart(
                "USER01",
                "P001",
                2
        );

        ArgumentCaptor<ShoppingCart> captor =
                ArgumentCaptor.forClass(ShoppingCart.class);
        verify(cartRepository).save(captor.capture());
        ShoppingCart savedCart = captor.getValue();
        assertThat(savedCart.getItems()).hasSize(1);
        assertThat(savedCart.getItems().get(0).getQuantity())
                .isEqualTo(2);
    }

    void whenCartNotExists() {
        when(productRepository.findById("P001"))
                .thenReturn(Optional.of(product));

        when(cartRepository.findByUserId("USER01"))
                .thenReturn(Optional.empty());

        shoppingCartService.addProductToCart(
                "USER01",
                "P001",
                1
        );

        verify(cartRepository, times(1))
                .save(any(ShoppingCart.class));
    }

    @Test
    void whenProductNotFound() {
        when(productRepository.findById("P999"))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() ->
                shoppingCartService.addProductToCart(
                        "USER01",
                        "P999",
                        1
                ))
                .isInstanceOf(IllegalArgumentException.class);
        verify(cartRepository, never())
                .save(any());
    }

    @Test
    void whenQuantityInvalid() {
        assertThatThrownBy(() ->
                shoppingCartService.addProductToCart(
                        "USER01",
                        "P001",
                        0
                ))
                .isInstanceOf(IllegalArgumentException.class);

        verify(cartRepository, never())
                .save(any());
    }

    @Test
    void whenStockNotEnough() {
        when(productRepository.findById("P001"))
                .thenReturn(Optional.of(product));

        assertThatThrownBy(() ->
                shoppingCartService.addProductToCart(
                        "USER01",
                        "P001",
                        100
                ))
                .isInstanceOf(IllegalArgumentException.class);

        verify(cartRepository, never())
                .save(any());
    }

    @Test
    void shouldUpdateProductQuantity() {
        CartItem item = new CartItem(UUID.randomUUID().toString(), product, 2);

        cart.getItems().add(item);

        when(productRepository.findById("P001"))
                .thenReturn(Optional.of(product));

        when(cartRepository.findByUserId("USER01"))
                .thenReturn(Optional.of(cart));

        shoppingCartService.updateProductQuantity(
                "USER01",
                "P001",
                5
        );

        assertThat(item.getQuantity()).isEqualTo(5);

        verify(cartRepository).save(cart);
    }

    @Test
    void whenUpdatedQuantityExceedsLatestStock() {
        product.setStock(4);
        CartItem item = new CartItem(UUID.randomUUID().toString(), product, 2);

        cart.getItems().add(item);

        when(productRepository.findById("P001"))
                .thenReturn(Optional.of(product));

        assertThatThrownBy(() ->
                shoppingCartService.updateProductQuantity(
                        "USER01",
                        "P001",
                        7
                ))
                .isInstanceOf(IllegalArgumentException.class);

        verify(cartRepository, never())
                .save(any());
    }

    @Test
    void shouldRemoveProductFromCart() {

        CartItem item = new CartItem(UUID.randomUUID().toString(), product, 2);

        cart.getItems().add(item);

        when(cartRepository.findByUserId("USER01"))
                .thenReturn(Optional.of(cart));

        shoppingCartService.removeProductFromCart(
                "USER01",
                "P001"
        );

        assertThat(cart.getItems()).isEmpty();

        verify(cartRepository).save(cart);
    }

    @Test
    void shouldRemoveProductEvenIfProductDeletedFromSystem() {
        CartItem item = new CartItem(UUID.randomUUID().toString(), product, 1);

        cart.getItems().add(item);

        when(cartRepository.findByUserId("USER01"))
                .thenReturn(Optional.of(cart));

        shoppingCartService.removeProductFromCart(
                "USER01",
                "P001"
        );

        assertThat(cart.getItems()).isEmpty();

        verify(cartRepository).save(cart);
    }
}
