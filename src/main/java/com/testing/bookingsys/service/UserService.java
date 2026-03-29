package com.testing.bookingsys.service;

import com.testing.bookingsys.dto.user.ChangePasswordRequest;
import com.testing.bookingsys.dto.user.ProfileResponse;
import com.testing.bookingsys.entity.AppUser;
import com.testing.bookingsys.exception.ApiException;
import com.testing.bookingsys.repository.UserRepository;
import com.testing.bookingsys.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileResponse getProfile() {
        AppUser user = getCurrentUser();
        return ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .verified(user.isVerified())
                .build();
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        AppUser user = getCurrentUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ApiException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    }

    public AppUser getCurrentUser() {
        return userRepository.findByEmailIgnoreCase(SecurityUtils.currentUsername())
                .orElseThrow(() -> new ApiException("Current user not found"));
    }
}
