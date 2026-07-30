package org.example.transactionmonitoringbackend.service;

import org.example.transactionmonitoringbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    /*
     * Service layer for user-account existence checks.
     * This service is used by rule evaluation to validate trusted payee accounts.
     */
    private final UserRepository userRepository;

    /*
     * Constructor injection for UserRepository.
     */
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /*
     * Return true when the given account id exists in user storage.
     * Return false when no matching account is found.
     */
    public boolean userExists(String accountId) {
        return userRepository.existsByAccountNo(accountId);
    }
}
