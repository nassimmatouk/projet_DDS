package com.example.demo2.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contacts")
@Data // Génère les getters, setters, toString, equals, hashCode
@NoArgsConstructor         // Génère le constructeur sans arguments
@AllArgsConstructor        // Génère le constructeur avec tous les arguments
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomAuteur;
    private String mail;
    private String telephone;
    private String date;

}
