package com.ecommerce.product;

import jakarta.validation.constraints.NotNull;

/**
 * @param delta negative to take stock out (an order), positive to put it back (a cancellation or restock)
 */
public record StockAdjustment(@NotNull Integer delta) {
}
