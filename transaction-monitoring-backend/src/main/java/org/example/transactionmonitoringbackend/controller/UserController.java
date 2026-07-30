package org.example.transactionmonitoringbackend.controller;

import org.example.transactionmonitoringbackend.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/user")
public class UserController {

    /*
     * Controller for user account registration endpoints.
     * Current endpoint supports adding username + account number pairs.
     */
    private final UserRepository userRepository;

    /*
     * Constructor injection for UserRepository.
     */
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /*
     * Create a user-account record from request parameters.
     * userName is the display name and accountNo is the unique account id.
     */
    @PostMapping("/add/user")
    public String addUser(
            @RequestParam String userName,
            @RequestParam String accountNo
    ) {
        userRepository.addAccountNo(userName, accountNo);
        return "add user successful";
    }
}