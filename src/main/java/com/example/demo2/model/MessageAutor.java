package com.example.demo2.model;

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
    private String dateFichier;

    // Champs pour l'objet MessageDemandeAutorisation
    private String statutAutorisation; 
    private String date; 
    private String idMessage; 

    // Champs pour les coordonnées
    private String mail; 
    private String telephone; 
    private String nomAuteur; 

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdTroqueur() {
        return idTroqueur;
    }

    public void setIdTroqueur(String idTroqueur) {
        this.idTroqueur = idTroqueur;
    }

    public String getIdFichier() {
        return idFichier;
    }

    public void setIdFichier(String idFichier) {
        this.idFichier = idFichier;
    }

    public String getDateFichier() {
        return dateFichier;
    }

    public void setDateFichier(String dateFichier) {
        this.dateFichier = dateFichier;
    }

    public String getStatutAutorisation() {
        return statutAutorisation;
    }

    public void setStatutAutorisation(String statutAutorisation) {
        this.statutAutorisation = statutAutorisation;
    }

    public String getIdMessage() {
        return idMessage;
    }

    public void setIdMessage(String idMessage) {
        this.idMessage = idMessage;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getNomAuteur() {
        return nomAuteur;
    }

    public void setNomAuteur(String nomAuteur) {
        this.nomAuteur = nomAuteur;
    }

    // Constructeur par défaut
    public MessageAutor() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
