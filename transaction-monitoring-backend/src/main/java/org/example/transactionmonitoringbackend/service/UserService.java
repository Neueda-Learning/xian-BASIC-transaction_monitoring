package org.example.transactionmonitoringbackend.service;

import org.example.transactionmonitoringbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Check whether a user exists by account id
    public boolean userExists(String accountId) {
        return userRepository.existsByAccountNo(accountId);
    }
}
