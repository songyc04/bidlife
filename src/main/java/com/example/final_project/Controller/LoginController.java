package com.example.final_project.Controller;

import com.example.final_project.DTO.LoginDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
public class LoginController {
    @GetMapping("")
    public String login() {
        return "login";
    }

    @PostMapping("")
    public String loginSubmit(LoginDTO loginDTO) {
        System.out.println("loginDTO=" + loginDTO);
        return "redirect:/";
    }
}