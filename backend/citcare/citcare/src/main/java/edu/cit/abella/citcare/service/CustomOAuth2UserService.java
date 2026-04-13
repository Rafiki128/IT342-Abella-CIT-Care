package edu.cit.abella.citcare.service;

import edu.cit.abella.citcare.entity.User;
import edu.cit.abella.citcare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String email = oAuth2User.getAttribute("email");
        String fullName = oAuth2User.getAttribute("name");

        if (email != null) {
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isEmpty()) {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setFullName(fullName);
                newUser.setRole("STUDENT"); 
                newUser.setPasswordHash("OAUTH2_PROVIDED"); // No password needed for Google users
                userRepository.save(newUser);
            }
        }
        return oAuth2User;
    }
}