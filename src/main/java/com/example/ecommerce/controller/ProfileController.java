package com.example.ecommerce.controller;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String viewProfile(Model model, Authentication authentication) {
        User user = userService.getCurrentUser(authentication.getName());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/edit")
    public String editProfile(@RequestParam String fullName,
                              @RequestParam String email,
                              @RequestParam String phone,
                              @RequestParam String region,
                              Authentication authentication) {
        User user = userService.getCurrentUser(authentication.getName());
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRegion(region);
        userService.saveUser(user);
        return "redirect:/profile?updated";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                  @RequestParam String newPassword,
                                  Authentication authentication) {
        User user = userService.getCurrentUser(authentication.getName());
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return "redirect:/profile?pwerror";
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.saveUser(user);
        return "redirect:/profile?pwchanged";
    }
}
