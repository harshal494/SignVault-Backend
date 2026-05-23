package com.harshalkhade.signvault.security;

import com.harshalkhade.signvault.entity.User;
import com.harshalkhade.signvault.enums.AuthProvider;
import com.harshalkhade.signvault.enums.Role;
import com.harshalkhade.signvault.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor

public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .fullName(name)
                            .email(email)
                            .googleId(googleId)
                            .authProvider(AuthProvider.GOOGLE)
                            .role(Role.ROLE_USER)
                            .emailVerified(true)
                            .profileComplete(false)
                            .active(true)
                            .build();
                    return userRepository.save(newUser);
                });

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash() != null ? user.getPasswordHash() : "")
                .authorities(user.getRole().name())
                .build();

        String token = jwtUtil.generateToken(userDetails);
        String redirectUrl = "http://localhost:8080/api/auth/oauth2/success?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
