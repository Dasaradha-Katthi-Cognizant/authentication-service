package com.cts.authentication_service.controller;

import com.cts.authentication_service.entity.User;
import com.cts.authentication_service.exception.InvalidCredentialsException;
import com.cts.authentication_service.service.UserService;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@Slf4j
public classUserController {


    @Autowired
    private UserService userService;

    
    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {

        log.info("Registering user: {}", user.getEmail());
        return userService.registerUser(user);
    }

      @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        log.info("User login attempt for email: {}", user.getEmail());
        try {
            String token = userService.loginUser(user.getEmail(), user.getPassword());
            return ResponseEntity.ok(Map.of("token", token)); // Return the token in the response
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }



    @GetMapping("/user/{email}")
    public User getUserByEmail(@PathVariable String email) {

        log.info("Fetching user details for email: {}", email);
        return userService.getUserByEmail(email);
    }

    @GetMapping("/users/{userId}")
    public User getUserById(@PathVariable int userId) {

        log.info("Fetching user details for user ID: {}", userId);
        return userService.getUserById(userId);
    }


//
//    @PutMapping("/updateUser/{email}")
//    public User updateUser(@PathVariable String email, @RequestBody User updatedUser) {
//        log.info("Updating user details for email: {}", email);
//
//        return userService.updateUser(email, updatedUser);
//
//}


    @GetMapping("/users")
    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return userService.getAllUsers();
    }


    // Endpoint to request OTP for password reset
    @PostMapping("/forgotPassword/{email}")
    public String forgotPassword(@PathVariable String email) {
        log.info("Generating OTP for email: {}", email);
        return userService.generateAndSendOtp(email);
    }


    // Endpoint to reset the password, verifying the OTP
    @PutMapping("/resetPassword")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String newPassword,
                                @RequestParam String otp) {
        log.info("Resetting password for email: {} with OTP: {}", email, otp);
        return userService.resetPasswordWithOtp(email, newPassword, otp);
    }


}