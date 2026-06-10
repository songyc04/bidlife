package com.example.final_project.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.final_project.Entity.SignupEntity;

@Repository
public interface SignupRepository extends JpaRepository<SignupEntity, Long> {
    
}
