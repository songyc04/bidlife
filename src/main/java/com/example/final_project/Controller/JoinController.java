package com.example.final_project.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.final_project.DTO.JoinDTO;
import com.example.final_project.Entity.JoinEntity;
import com.example.final_project.Repository.JoinRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/join")
public class JoinController {
   private final JoinRepository joinRepository;

   @GetMapping("/signup")
   public String sign() {
      return "join";
   }



   @PostMapping("/signup")
   public String signup(JoinDTO joinDTO) {
      System.out.println("가입 요청 아이디: " + joinDTO.getUsername());

      JoinEntity joinEntity = new JoinEntity();
      joinEntity.setUsername(joinDTO.getUsername());
      joinEntity.setPassword(joinDTO.getPassword());
      joinEntity.setName(joinDTO.getName());
      joinEntity.setEmail(joinDTO.getEmail());

      joinRepository.save(joinEntity);
      System.out.println("joinDTO=" + joinDTO);
      return "redirect:/";
   }
}
