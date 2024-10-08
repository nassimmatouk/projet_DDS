package com.example.demo2.model;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "messages_autorisations")
public class MessageAutor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idTroqueur;
    private String idFichier;
    private String statut;
    private Date date;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getIdFichier() {
        return idFichier;
    }
    public void setIdFichier(String idFichier) {
        this.idFichier = idFichier;
    }

    public String getIdTroqueur() {
        return idTroqueur;
    }
    public void setIdTroqueur(String idTroqueur) {
        this.idTroqueur = idTroqueur;
    }

    public MessageAutor() {
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStatut() {
        return statut;
    }
    public void setStatut(String statut) {
        this.statut = statut;
    }
}
