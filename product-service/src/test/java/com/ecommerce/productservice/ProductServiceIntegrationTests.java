package com.ecommerce.productservice;

import com.ecommerce.productservice.repository.ProductRepository;
import com.ecommerce.productservice.repository.ReviewRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductServiceIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private ReviewRepository reviewRepository;

    @BeforeEach
    void cleanDatabase() {
        productRepository.deleteAll();
    }

    @Test
    void crudFlowWorks() throws Exception {
        Long id = createProduct("Java Book", "Books", "499.00", 8);

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Java Book"));

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Advanced Java", "Books", "599.00", 6)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(599.00));

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void listingSearchAndFiltersWork() throws Exception {
        createProduct("Java Book", "Books", "499.00", 8);
        createProduct("Wireless Mouse", "Electronics", "799.00", 0);
        createProduct("Mechanical Keyboard", "Electronics", "2499.00", 4);

        mockMvc.perform(get("/api/products").param("search", "keyboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Mechanical Keyboard"));

        mockMvc.perform(get("/api/products")
                        .param("category", "electronics")
                        .param("maxPrice", "1000")
                        .param("inStock", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/api/products/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Books"))
                .andExpect(jsonPath("$[1]").value("Electronics"));
    }

    @Test
    void stockCanBeReservedRestoredAndCannotGoNegative() throws Exception {
        Long id = createProduct("Java Book", "Books", "499.00", 3);

        mockMvc.perform(post("/api/products/{id}/stock/decrease", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(1));

        mockMvc.perform(post("/api/products/{id}/stock/decrease", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Only 1 units are available for product " + id));

        mockMvc.perform(post("/api/products/{id}/stock/increase", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(5));
    }

    @Test
    void validationErrorsAreStructured() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("", "", "0", -1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.price").exists())
                .andExpect(jsonPath("$.validationErrors.stock").exists());
    }

    private Long createProduct(String name, String category, String price, int stock) throws Exception {
        String body = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson(name, category, price, stock)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private String productJson(String name, String category, String price, int stock) {
        return """
                {"name":"%s","description":"Useful product","category":"%s",
                 "price":%s,"stock":%d,"imageUrl":"https://example.com/item.jpg","active":true}
                """.formatted(name, category, price, stock);
    }
}
