package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.services.RoleServiceImpl;
import ru.kata.spring.boot_security.demo.services.UserServiceImpl;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AdminPageController {
    private final RoleServiceImpl roleServiceImpl;
    private final UserServiceImpl userServiceImpl;

    @Autowired
    public AdminPageController(RoleServiceImpl roleServiceImpl, UserServiceImpl userServiceImpl) {
        this.roleServiceImpl = roleServiceImpl;
        this.userServiceImpl = userServiceImpl;
    }

    @GetMapping("/login-page")
    public String loginPage() {
        return "login-page";
    }

    @GetMapping("/user")
    public String userPanel(Principal principal, Model model) {
        User user = userServiceImpl.findByUserName(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("formattedRoles", roleServiceImpl.formatRoles(user.getRoles()));
        return "user";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminPanel(Principal principal, Model model) {
        User currentUser = userServiceImpl.findByUserName(principal.getName());
        List<User> allUsers = userServiceImpl.findAllUsers().stream()
                .peek(u -> u.setFormattedRoles(roleServiceImpl.formatRoles(u.getRoles())))
                .collect(Collectors.toList());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("formattedRoles", roleServiceImpl.formatRoles(currentUser.getRoles()));
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("allRoles", roleServiceImpl.getAllRoles());
        return "admin";
    }

    @PostMapping("/admin/save-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveUser(@ModelAttribute("user") User user) {
        userServiceImpl.saveUser(user);
        return "redirect:/admin";
    }

    @PostMapping("/admin/update-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUserById(@ModelAttribute("updatedUser") User user,
                                 @RequestParam(value = "password", required = false) String newPassword,
                                 Principal principal) {
        User updatedUser = userServiceImpl.findById(user.getId());
        userServiceImpl.updateUser(user, newPassword);
        if (principal.getName().equals(updatedUser.getUsername())) {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    updatedUser, updatedUser.getPassword(), updatedUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        if (userServiceImpl.isAdmin(updatedUser) || !principal.getName().equals(updatedUser.getUsername())) {
            return "redirect:/admin";
        } else if (userServiceImpl.hasRole(updatedUser, "ROLE_USER") && principal.getName().equals(updatedUser.getUsername())) {
            return "redirect:/user";
        } else {
            return "redirect:/login";
        }
    }

    @PostMapping("/admin/delete-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@ModelAttribute("updatedUser") User user, Principal principal) {
        if (principal.getName().equals(userServiceImpl.findById(user.getId()).getUsername())) {
            userServiceImpl.deleteUser(user.getId());
            return "redirect:/login-page";
        }

        userServiceImpl.deleteUser(user.getId());
        return "redirect:/admin";
    }
}
