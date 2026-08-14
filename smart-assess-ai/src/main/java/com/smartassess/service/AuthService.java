package com.smartassess.service;

import com.smartassess.dto.AuthDTOs;
import com.smartassess.entity.User;
import com.smartassess.exception.APIException;
import com.smartassess.repository.UserRepository;
import com.smartassess.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthDTOs.AuthResponse register(AuthDTOs.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Email address is already registered", "EMAIL_EXISTS");
        }

        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole(),
                request.getCollegeId()
        );

        userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return new AuthDTOs.AuthResponse(jwt, user.getId(), user.getName(), user.getEmail(), user.getRole().name(), user.getCollegeId());
    }

    public AuthDTOs.AuthResponse login(AuthDTOs.LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception ex) {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid email or password", "INVALID_CREDENTIALS");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "User not found", "USER_NOT_FOUND"));

        return new AuthDTOs.AuthResponse(jwt, user.getId(), user.getName(), user.getEmail(), user.getRole().name(), user.getCollegeId());
    }
}
