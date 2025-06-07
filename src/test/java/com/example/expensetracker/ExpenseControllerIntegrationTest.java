package com.example.expensetracker;

import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@Transactional
public class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        if (!userRepository.existsByUsername("testuser")) {
            User user = new User();
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            user.setPasswordHash(passwordEncoder.encode("testpass"));
            userRepository.save(user);
        }
    }

    @Test
    void fullExpenseFlow() throws Exception {
        // ➤ Добавляем расход
        mockMvc.perform(post("/expenses/add")
                        .with(user("testuser").password("testpass"))
                        .with(csrf())
                        .param("title", "Test expense")
                        .param("amount", "123.45")
                        .param("currency", "RUB")
                        .param("date", "2024-05-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expenses/list"));

        // ➤ Проверяем, что расход отображается на странице
        mockMvc.perform(get("/expenses/list")
                        .with(user("testuser").password("testpass")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Test expense")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("123.45")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("RUB")));
    }
}
