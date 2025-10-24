package com.example.oauth2app.service;

import java.util.List;
import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.oauth2app.entity.AuthProvider;
import com.example.oauth2app.entity.User;
import com.example.oauth2app.repository.AuthProviderRepository;
import com.example.oauth2app.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final AuthProviderRepository authProviderRepository;

    public CustomOAuth2UserService(UserRepository userRepository, AuthProviderRepository authProviderRepository) {
        this.userRepository = userRepository;
        this.authProviderRepository = authProviderRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = super.loadUser(request);
        String provider = request.getClientRegistration().getRegistrationId().toUpperCase();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = null;
        String name = null;
        String picture = null;

        switch (provider) {
            case "GOOGLE":
                email = (String) attributes.get("email");
                name = (String) attributes.get("name");
                picture = (String) attributes.get("picture");
                break;

            case "GITHUB":
                name = (String) attributes.get("login");
                picture = (String) attributes.get("avatar_url");
                email = (String) attributes.get("email");

                if (email == null) {
                    email = fetchGitHubPrimaryEmail(request.getAccessToken().getTokenValue());
                }
                break;

            default:
                throw new IllegalStateException("Unsupported provider: " + provider);
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

            AuthProvider auth = new AuthProvider();
            auth.setUser(user);
            auth.setProvider(provider);
            auth.setProviderEmail(email);
            authProviderRepository.save(auth);
        } else {

            user.setDisplayName(name);
            user.setAvatarUrl(picture);
            userRepository.save(user);
        }

        return oAuth2User;
    }

    private String fetchGitHubPrimaryEmail(String accessToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.github.com/user/emails";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.add("Authorization", "token " + accessToken);

            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>("", headers);
            org.springframework.http.ResponseEntity<List> response = restTemplate.exchange(
                    url, org.springframework.http.HttpMethod.GET, entity, List.class);

            List<Map<String, Object>> emails = response.getBody();
            if (emails != null) {
                for (Map<String, Object> e : emails) {
                    if (Boolean.TRUE.equals(e.get("primary"))) {
                        return (String) e.get("email");
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
