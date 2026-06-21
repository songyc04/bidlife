package com.example.final_project.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class SignupEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false, length = 255)
    private String password;
    
    @Column(nullable = false, unique = true, length = 50)
    private String nickname;
    
    @Column(name = "profile_image", length = 255)
    private String profileImage;

    @Column(name = "profile_gradient", length = 50)
    private String profileGradient;

    @Column(name = "is_temporary_password", nullable = false)
    private boolean isTemporaryPassword = false;
}
