package com.ecommerce.review;

import com.ecommerce.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/latest")
    public List<Review> latest(@RequestParam(defaultValue = "6") int limit) {
        return reviewService.latest(limit);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review save(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody ReviewRequest request) {
        return reviewService.save(user, request);
    }
}
