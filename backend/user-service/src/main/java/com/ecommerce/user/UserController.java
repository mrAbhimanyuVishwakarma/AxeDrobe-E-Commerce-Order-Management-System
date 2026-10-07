package com.ecommerce.user;

import com.ecommerce.security.AuthUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthUser principal) {
        return UserResponse.from(load(principal));
    }

    @PatchMapping("/me")
    public UserResponse updateProfile(@AuthenticationPrincipal AuthUser principal,
                                      @Valid @RequestBody UpdateProfileRequest request) {
        User user = load(principal);
        user.setName(request.name().trim());
        return UserResponse.from(userRepository.save(user));
    }

    private User load(AuthUser principal) {
        return userRepository.findById(principal.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    public record UpdateProfileRequest(@NotBlank @Size(max = 80) String name) {
    }
}
