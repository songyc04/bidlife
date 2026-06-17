package com.example.final_project.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.final_project.Entity.SignupEntity;

import java.util.Optional;

@Repository
public interface SignupRepository extends JpaRepository<SignupEntity, Long> {
    Optional<SignupEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    long count();
}
