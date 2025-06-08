package com.example.expensetracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                RedirectAttributes redirectAttributes) {
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", "Неверное имя пользователя или пароль.");
            return "redirect:/login";
        }

        if (logout != null) {
            redirectAttributes.addFlashAttribute("logout", "Вы вышли из системы.");
            return "redirect:/login";
        }

        return "login";
    }
}
