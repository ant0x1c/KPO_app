package com.example.expensetracker.controller;

import com.example.expensetracker.model.User;
import com.example.expensetracker.service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Показать страницу регистрации
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    // Обработать форму регистрации
    @PostMapping("/register")
    public String handleRegister(@RequestParam String username,
                                 @RequestParam String email,
                                 @RequestParam String password,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.registerUser(username, email, password);

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Имя пользователя уже занято.");
            return "redirect:/users/register";
        }

        redirectAttributes.addFlashAttribute("success", "Регистрация прошла успешно. Войдите в систему.");
        return "redirect:/login";
    }
}
