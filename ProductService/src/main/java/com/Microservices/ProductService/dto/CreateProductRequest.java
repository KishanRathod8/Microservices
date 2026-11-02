package com.Microservices.ProductService.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "Product SKU is mandatory")
        @Size(min = 3, max = 50, message = "SKU must be between 3 and 50 characters")
        @Pattern(regexp = "^[A-Z0-9_-]+$", message = "SKU must contain only uppercase letters, numbers, hyphens, or underscores")
        String sku,

        @NotBlank(message = "Product name is mandatory")
        @Size(max = 100, message = "Product name cannot exceed 100 characters")
        String name,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        @NotNull(message = "Price is mandatory")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        @Digits(integer = 8, fraction = 2, message = "Price format must be up to 8 integer digits and 2 decimal places")
        BigDecimal price
) {}