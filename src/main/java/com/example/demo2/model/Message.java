package com.example.demo2.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String expéditeur;
    private String destinataire;
    private String contenu;

    @Column(name = "date_envoi")
    private LocalDateTime dateEnvoi;
}
