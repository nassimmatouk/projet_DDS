package com.example.demo2.repository;

import java.util.Optional; // Optional : générique, représente une valeur qui peut être présente ou absente

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo2.model.MessageAutor;

public interface MessageAutorRepository extends JpaRepository<MessageAutor, Long> {
    // JPA utilise la convention pro1Andprop2... pour générer des requètes
    Optional<MessageAutor> findByIdTroqueurAndIdFichierAndIdMessage(String idTroqueur, String idFichier, String idMessage);
    
}
