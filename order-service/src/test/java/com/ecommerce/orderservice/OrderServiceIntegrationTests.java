package com.ecommerce.orderservice;

import com.ecommerce.orderservice.client.ProductClient;
import com.ecommerce.orderservice.dto.ProductSummary;
import com.ecommerce.orderservice.repository.CartItemRepository;
import com.ecommerce.orderservice.repository.OrderRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderServiceIntegrationTests {

    private static final String SECRET = "OrderServiceIntegrationTestSecretKeyThatIsAtLeast32BytesLong";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private ProductClient productClient;

    private String token;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        token = tokenFor("student@example.com");
        when(productClient.getProduct(10L)).thenReturn(new ProductSummary(10L, "Java Book",
                "Learn Java", "Books", new BigDecimal("499.00"), 5, "book.jpg", true));
    }

    @Test
    void cartRequiresAuthenticationAndIsScopedToJwtUser() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":10,\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productName").value("Java Book"))
                .andExpect(jsonPath("$.items[0].subtotal").value(998.00))
                .andExpect(jsonPath("$.totalAmount").value(998.00));
    }

    @Test
    void checkoutCreatesPaidDemoOrderUpdatesInventoryAndClearsCart() throws Exception {
        addItem();

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"shippingAddress":"University Hostel, Bengaluru",
                                 "paymentMethod":"DEMO_CARD","paymentToken":"DEMO_SUCCESS"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.paymentStatus").value("PAID"))
                .andExpect(jsonPath("$.totalAmount").value(998.00));

        verify(productClient).decreaseStock(10L, 2);

        mockMvc.perform(get("/api/cart").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());

        mockMvc.perform(get("/api/orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].items[0].productId").value(10));
    }

    @Test
    void codOrderCanBeCancelledAndInventoryIsRestored() throws Exception {
        addItem();
        String body = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"shippingAddress":"University Hostel, Bengaluru","paymentMethod":"COD"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentStatus").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        Number orderId = com.jayway.jsonpath.JsonPath.read(body, "$.id");
        mockMvc.perform(post("/api/orders/{id}/cancel", orderId.longValue())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(productClient).increaseStock(10L, 2);
    }

    @Test
    void realCardDataIsNotNeededAndBadDemoTokenIsRejectedBeforeInventoryChange() throws Exception {
        addItem();
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"shippingAddress":"University Hostel, Bengaluru",
                                 "paymentMethod":"DEMO_CARD","paymentToken":"not-valid"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("never send real card")));
    }

    private void addItem() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":10,\"quantity\":2}"))
                .andExpect(status().isOk());
    }

    private String tokenFor(String email) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder().subject(email).issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key).compact();
    }
}
