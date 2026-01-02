package com.cts.authentication_service.service;

import com.cts.authentication_service.client.NotificationServiceClient;
import com.cts.authentication_service.entity.User;
import com.cts.authentication_service.exception.EmailAlreadyExistsException;
import com.cts.authentication_service.exception.InvalidCredentialsException;
import com.cts.authentication_service.exception.InvalidOtpException;
import com.cts.authentication_service.exception.UserNotFoundException;
import com.cts.authentication_service.repository.UserRepository;
import com.cts.authentication_service.util.JwtUtil;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private NotificationServiceClient notificationServiceClient;

    @Autowired
    private JwtUtil jwtUtil;


    public User registerUser(User user) {
        log.info("Registering user with email: {}", user.getEmail());

        if (userRepository.findByEmail(user.getEmail()) != null) {
            log.error("Email already exists: {}", user.getEmail());
            throw new EmailAlreadyExistsException("Email already exists: " + user.getEmail());
        }

        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

        //  //Trigger welcome email
        //  try {
        //      notificationServiceClient.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName());
        //      log.info("Welcome email sent successfully to {}", savedUser.getEmail());
        //  } catch (Exception ex) {
        //      log.error("Failed to send welcome email to {}: {}", savedUser.getEmail(), ex.getMessage());
        //  }

        return savedUser;
    }





    public String loginUser(String email, String password) {
        log.info("User login attempt for email: {}", email);

        User user = userRepository.findByEmail(email);

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            log.info("Login successful for user: {}", user.getName());
            return jwtUtil.generateToken(email); // Return JWT token
        }

        log.error("Invalid credentials for email: {}", email);
        throw new InvalidCredentialsException("Invalid credentials for email: " + email);
    }




    public User getUserByEmail(String email) {
        log.info("Fetching user details for email: {}", email);

        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.error("User not found for email: {}", email);
            throw new UserNotFoundException("User not found for email: " + email);
        }

        log.info("User details fetched successfully for email: {}", email);
        return user;
    }



    public User getUserById(int userId) {
        log.info("Fetching user details for user ID: {}", userId);

        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            log.error("User not found for ID: {}", userId);
            throw new UserNotFoundException("User not found for ID: " + userId);
        }
        User user = optUser.get();
        log.info("User details fetched successfully for ID: {}", userId);
        return user;
    }



   
//    public User updateUser(String email, User updatedUser) {
//        log.info("Updating user details for email: {}", email);
//
//        User user = userRepository.findByEmail(email);
//        if (user == null) {
//            log.error("User not found for email: {}", email);
//            throw new UserNotFoundException("User not found for email: " + email);
//        }
//
//        user.setName(updatedUser.getName());
//        user.setEmail(updatedUser.getEmail());
//
//        // Hash the password if it is being updated
//        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
//            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
//        }
//
//        user.setPhone(updatedUser.getPhone());
//
//        log.info("User details updated successfully for email: {}", email);
//        return userRepository.save(user);
//    }





    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll();
    }

    // Temporary in-memory store for OTPs
    private Map<String, String> otpStore = new ConcurrentHashMap<>();

    // Generate and send OTP for forgot password
    public String generateAndSendOtp(String email) {
        User user = userRepository.findByEmail(email);
        if(user == null) {
            throw new UserNotFoundException("User not found for email: " + email);
        }
        // Generate random 6-digit OTP
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        otpStore.put(email, otp);
        
        // Use the NotificationServiceClient to send OTP via email
        notificationServiceClient.sendOtpEmail(email, otp);
        return "OTP sent to " + email;
    }

    // Verify OTP, update password and remove OTP from temporary store
    public String resetPasswordWithOtp(String email, String newPassword, String otp) {
        if (!otpStore.containsKey(email) || !otpStore.get(email).equals(otp)) {
            throw new InvalidOtpException("Invalid OTP provided.");
        }
        // Remove the OTP after successful verification
        otpStore.remove(email);
        
        User user = userRepository.findByEmail(email);
        if(user == null) {
            throw new UserNotFoundException("User not found for email: " + email);
        }
        
        // Hash the new password and update
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "Password reset successful for user: " + user.getName();
    }


}

