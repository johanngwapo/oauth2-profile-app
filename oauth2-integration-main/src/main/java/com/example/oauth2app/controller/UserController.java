package com.example.oauth2app.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.oauth2app.entity.User;
import com.example.oauth2app.repository.UserRepository;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/user")
    public User getUser(@AuthenticationPrincipal OAuth2User oAuth2User,
            OAuth2AuthenticationToken authentication) {
        if (oAuth2User == null)
            return null;

        String provider = authentication.getAuthorizedClientRegistrationId().toUpperCase();

        String email = null;
        String name = null;
        String picture = null;

        if ("GOOGLE".equals(provider)) {
            email = (String) oAuth2User.getAttributes().get("email");
            name = (String) oAuth2User.getAttributes().get("name");
            picture = (String) oAuth2User.getAttributes().get("picture");

        } else if ("GITHUB".equals(provider)) {
            email = (String) oAuth2User.getAttributes().get("email");
            name = (String) oAuth2User.getAttributes().get("login");
            picture = (String) oAuth2User.getAttributes().get("avatar_url");
        }

        if (email == null) {
            email = "unknown-" + provider.toLowerCase() + "@example.com";
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setDisplayName(name != null ? name : "Unnamed User");
            user.setAvatarUrl(picture);
            userRepository.save(user);
        } else {
            user.setDisplayName(name);
            user.setAvatarUrl(picture);
            userRepository.save(user);
        }

        return user;
    }

    @PostMapping("/user/update")
    public User updateProfile(@AuthenticationPrincipal OAuth2User oAuth2User,
            @RequestParam String displayName,
            @RequestParam(required = false) String bio) {
        if (oAuth2User == null)
            return null;

        String email = (String) oAuth2User.getAttributes().get("email");
        if (email == null)
            return null;

        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setDisplayName(displayName);
            user.setBio(bio);
            userRepository.save(user);
        }

        return user;
    }

    @GetMapping("/logout")
    public void logout() {
    }
}
