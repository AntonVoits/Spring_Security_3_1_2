package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.services.RoleServiceImpl;
import ru.kata.spring.boot_security.demo.services.UserServiceImpl;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPageController {
    private final RoleServiceImpl roleServiceImpl;
    private final UserServiceImpl userServiceImpl;

    @Autowired
    public AdminPageController(RoleServiceImpl roleServiceImpl, UserServiceImpl userServiceImpl) {
        this.roleServiceImpl = roleServiceImpl;
        this.userServiceImpl = userServiceImpl;
    }

    @GetMapping("/current-user")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userServiceImpl.findByUserName(username);
        if (currentUser != null) {
            currentUser.setFormattedRoles(roleServiceImpl.formatRoles(currentUser.getRoles()));
            return new ResponseEntity<>(currentUser, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> loadUsers() {
        List<User> users = userServiceImpl.findAllUsers().stream()
                .peek(user -> user.setFormattedRoles(roleServiceImpl.formatRoles(user.getRoles())))
                .collect(Collectors.toList());
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userServiceImpl.findById(id);
        if (user != null) {
            user.setFormattedRoles(roleServiceImpl.formatRoles(user.getRoles()));
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody User updatedUser,
            Principal principal
    ) {
        try {
            User existingUser = userServiceImpl.findById(id);
            if (existingUser == null) {
                return ResponseEntity.notFound().build();
            }

            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setFirstName(updatedUser.getFirstName());
            existingUser.setLastName(updatedUser.getLastName());
            existingUser.setAge(updatedUser.getAge());
            existingUser.setRoles(updatedUser.getRoles());

            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                existingUser.setPassword(updatedUser.getPassword());
            }

            if (updatedUser.getRoles() == null || updatedUser.getRoles().isEmpty()) {
                updatedUser.setRoles(existingUser.getRoles());
            }

            User savedUser = userServiceImpl.updateUser(existingUser, updatedUser.getPassword());

            if (principal.getName().equals(existingUser.getEmail())) {
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        savedUser, savedUser.getPassword(), savedUser.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        if (userServiceImpl.findByUserName(user.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User savedUser = userServiceImpl.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Principal principal) {
        User user = userServiceImpl.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        if (user.getUsername().equals(principal.getName())) {
            userServiceImpl.deleteUser(id);
            SecurityContextHolder.clearContext();
            return ResponseEntity.status(HttpStatus.RESET_CONTENT).build();
        }

        userServiceImpl.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleServiceImpl.getAllRoles();
        return ResponseEntity.ok(roles);
    }
}
