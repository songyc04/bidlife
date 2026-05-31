package com.example.final_project.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.final_project.Entity.JoinEntity;

@Repository
public interface JoinRepository extends JpaRepository<JoinEntity, Long> {
   
}