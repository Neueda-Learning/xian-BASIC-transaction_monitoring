package org.example.transactionmonitoringbackend.controller;

import org.example.transactionmonitoringbackend.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {


    private final UserRepository userRepository;


    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/add/user")
    public String addUser(
            @RequestParam String userName,
            @RequestParam String accountNo
    ) {
        userRepository.addAccountNo(userName, accountNo);
        return "add user successful";
    }
}