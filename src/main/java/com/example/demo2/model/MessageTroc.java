package com.example.demo2.model;

import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "messages_troc")
public class MessageTroc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idTroqueur;
    private String idFichier;
    private String statut;
    private String dateFichier;
    private String dateMessage; 

    @ElementCollection
    private List<ObjetTroc> objets;
    
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
    
    public String getStatut() {
        return statut;
    }
    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getDateFichier() {
        return dateFichier;
    }

    public void setDateFichier(String dateFichier) {
        this.dateFichier = dateFichier;
    }

    public String getDateMessage() {
        return dateMessage;
    }

    public void setDateMessage(String dateMessage) {
        this.dateMessage = dateMessage;
    }

    public List<ObjetTroc> getObjets() {
        return objets;
    }

    public void setObjets(List<ObjetTroc> objets) {
        this.objets = objets;
    }

    @Embeddable
    public static class ObjetTroc {
        private String titre;
        private String description;
        private int qualite;
        private int quantite;

        public String getTitre() {
            return titre;
        }
        public void setTitre(String titre) {
            this.titre = titre;
        }
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
        public int getQualite() {
            return qualite;
        }
        public void setQualite(int qualite) {
            this.qualite = qualite;
        }
        public int getQuantite() {
            return quantite;
        }
        public void setQuantite(int quantite) {
            this.quantite = quantite;
        }
    }
    
}
