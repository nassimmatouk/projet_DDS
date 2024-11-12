package com.example.demo2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo2.model.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    Optional<Contact> findByNomAuteurAndMailAndTelephoneAndDate(String nomA, String mail, String tel, String date);
}
