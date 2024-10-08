package com.example.demo2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo2.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
