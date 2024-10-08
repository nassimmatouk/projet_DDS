package com.example.demo2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo2.model.MessageTroc;

public interface MessageTrocRepository extends JpaRepository<MessageTroc, Long> {
}
