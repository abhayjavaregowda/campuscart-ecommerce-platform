package com.ecommerce.productservice.config;

import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class ProductDataSeeder {

    @Bean
    @ConditionalOnProperty(name = "app.seed-data", havingValue = "true", matchIfMissing = true)
    CommandLineRunner seedProducts(ProductRepository repository) {
        return args -> {
            if (repository.count() > 0) return;
            repository.saveAll(List.of(
                    product("Mechanical Keyboard", "Tactile keyboard with hot-swappable switches and white backlight.", "Electronics", "2499.00", 18),
                    product("Wireless Mouse", "Quiet wireless mouse with an ergonomic shape and USB receiver.", "Electronics", "799.00", 30),
                    product("Java Interview Guide", "Practical Java, Spring, SQL, and system-design revision notes.", "Books", "499.00", 45),
                    product("Everyday Backpack", "Water-resistant 22 litre backpack with a padded laptop sleeve.", "Fashion", "1299.00", 12),
                    product("Insulated Bottle", "Reusable 750 ml stainless-steel bottle for long study days.", "Lifestyle", "649.00", 24),
                    product("USB-C Study Lamp", "Adjustable warm-white desk lamp with three brightness levels.", "Electronics", "999.00", 9)
            ));
        };
    }

    private Product product(String name, String description, String category, String price, int stock) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setCategory(category);
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);
        product.setActive(true);
        return product;
    }
}
