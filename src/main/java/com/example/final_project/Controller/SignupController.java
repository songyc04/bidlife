package com.example.final_project.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.final_project.DTO.JoinDTO;
import com.example.final_project.Entity.SignupEntity;
import com.example.final_project.Repository.SignupRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/signup")
public class SignupController {
    private final SignupRepository signupRepository;

    @GetMapping("")
    public String signup() {
        return "signup";
    }

    @GetMapping("/complete")
    public String complete() {
        return "join_complete";
    }

    @PostMapping("")
    public String signupSubmit(JoinDTO joinDTO) {
        System.out.println("가입 요청 아이디: " + joinDTO.getUsername());

        SignupEntity signupEntity = new SignupEntity();
        signupEntity.setUsername(joinDTO.getUsername());
        signupEntity.setPassword(joinDTO.getPassword());
        signupEntity.setName(joinDTO.getName());
        signupEntity.setEmail(joinDTO.getEmail());

        signupRepository.save(signupEntity);
        System.out.println("joinDTO=" + joinDTO);
        return "redirect:/signup/complete";
    }
}
