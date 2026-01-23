package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.services.UserServiceImpl;

import java.security.Principal;


@Controller
@RequestMapping("/user")
public class UserController {
    private UserServiceImpl userServiceImp;

    @Autowired
    public UserController(UserServiceImpl userServiceImp) {
        this.userServiceImp = userServiceImp;
    }

    @GetMapping
    public String showUser(Principal principal, Model model) {
        User user = userServiceImp.findByUserName(principal.getName());
        model.addAttribute("users", user);
        return "user";
    }
}
