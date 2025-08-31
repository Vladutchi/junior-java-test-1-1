package com.example.carins.web.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateClaimRequest(
        @NotNull(message = "Claim date must be provided")
        @PastOrPresent(message = "Claim date cannot be in the future")
        LocalDate claimDate,

        @NotBlank(message = "Description must be provided")
        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        @NotNull(message = "Amount must be provided")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @Digits(integer = 10, fraction = 2, message = "Amount must have at most 2 decimals")
        BigDecimal amount
) {}
