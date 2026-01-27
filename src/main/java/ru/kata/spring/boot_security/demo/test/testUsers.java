package ru.kata.spring.boot_security.demo.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.services.RoleService;
import ru.kata.spring.boot_security.demo.services.UserService;

import java.util.Set;

@Component
public class testUsers implements CommandLineRunner {
    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public testUsers(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (roleService.getAllRoles().isEmpty()) {
            Role roleAdmin = new Role("ROLE_ADMIN");
            Role roleUser = new Role("ROLE_USER");
            roleService.saveRole(roleAdmin);
            roleService.saveRole(roleUser);
        }
        if (userService.findAllUsers().isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@mail.ru");
            admin.setPassword("admin");
            admin.setFirstName("Dave");
            admin.setLastName("Mustaine");
            admin.setAge(64);
            admin.setRoles(Set.of(roleService.findByName("ROLE_ADMIN"), roleService.findByName("ROLE_USER")));
            userService.saveUser(admin);

            User user = new User();
            user.setEmail("user@mail.ru");
            user.setPassword("user");
            user.setFirstName("James");
            user.setLastName("Hetfield");
            user.setAge(62);
            user.setRoles(Set.of(roleService.findByName("ROLE_USER")));
            userService.saveUser(user);
        }
    }
}