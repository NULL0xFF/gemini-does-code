package com.null0xff.ark.server.service;

import com.null0xff.ark.server.entity.User;
import com.null0xff.ark.server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for managing user profile information and account lifecycle.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Updates the custom nickname for a user.
     *
     * @param userId   The unique identifier of the user
     * @param nickname The new nickname to set (can be null to revert to Discord username)
     */
    @Transactional
    public void updateNickname(UUID userId, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setNickname(nickname);
        userRepository.save(user);
    }

    /**
     * Deletes a user account and all associated data from the system.
     *
     * @param userId The unique identifier of the user to delete
     */
    @Transactional
    public void deleteAccount(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
    }
}
