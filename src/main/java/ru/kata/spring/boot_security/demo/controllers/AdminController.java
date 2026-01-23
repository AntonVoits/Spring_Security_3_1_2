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

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserServiceImpl userService;
    private final RoleServiceImpl roleService;

    @Autowired
    public AdminController(UserServiceImpl userService, RoleServiceImpl roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public String showAllUsers(Model model) {
        model.addAttribute("allUsers", userService.findAllUsers());
        return "admin";
    }

    @GetMapping("add-new-user")
    public String addNewUser(Model model) {
        model.addAttribute("save", new User());
        model.addAttribute("roles", roleService.getAllRoles());
        return "user-info";
    }

    @PostMapping("/save-user")
    public String saveUser(@ModelAttribute("user") User user) {
        userService.saveUser(user);
        return "redirect:/admin";
    }

    @GetMapping("/update-user")
    public String updateUser(@RequestParam(value = "username") String username, Model model) {
        model.addAttribute("updateUserId", userService.loadUserByUsername(username));
        model.addAttribute("roles", roleService.getAllRoles());
        return "update";
    }

    @PostMapping("update-user-by-id")
    public String updateUserById(@ModelAttribute("updateUserId") User user,
                                 @RequestParam(value = "newPassword", required = false) String newPassword,
                                 Principal principal) {
        User updatedUser = userService.findById(user.getId());
        userService.updateUser(user, newPassword);
        if (principal.getName().equals(updatedUser.getUsername())) {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    updatedUser, updatedUser.getPassword(), updatedUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        if (userService.isAdmin(updatedUser) || !principal.getName().equals(updatedUser.getUsername())) {
            return "redirect:/admin";
        } else if (userService.hasRole(updatedUser, "ROLE_USER") && principal.getName().equals(updatedUser.getUsername())) {
            return "redirect:/user";
        } else {
            return "redirect:/login";
        }
    }

    @PostMapping("delete-user")
    public String deleteUser(@RequestParam("id") Long id, Principal principal) {
        if (principal.getName().equals(userService.findById(id).getUsername())) {
            userService.deleteUser(id);
            return "redirect:/login";
        }
        if (id == null || userService.findById(id) == null) {
            throw new IllegalArgumentException("User not found");
        }
        userService.deleteUser(id);
        return "redirect:/admin";
    }
}

