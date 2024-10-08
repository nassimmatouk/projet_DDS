package com.example.demo2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo2.model.MessageAutor;

public interface MessageAutorRepository extends JpaRepository<MessageAutor, Long> {
}
