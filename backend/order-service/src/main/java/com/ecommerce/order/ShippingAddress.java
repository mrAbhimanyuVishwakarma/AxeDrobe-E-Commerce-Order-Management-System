package com.ecommerce.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShippingAddress(
        @NotBlank(message = "Enter the recipient's name") @Size(max = 80) String fullName,
        @NotBlank(message = "Enter a mobile number") @Pattern(regexp = "[6-9][0-9]{9}", message = "Enter a valid 10-digit mobile number") String phone,
        @NotBlank(message = "Enter the address") @Size(max = 200) String line1,
        @Size(max = 200) String line2,
        @NotBlank(message = "Enter the city") @Size(max = 60) String city,
        @NotBlank(message = "Enter the state") @Size(max = 60) String state,
        @NotBlank(message = "Enter the PIN code") @Pattern(regexp = "[1-9][0-9]{5}", message = "Enter a valid 6-digit PIN code") String pincode) {
}
