package com.example.demo2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo2.model.MessageTroc;

public interface MessageTrocRepository extends JpaRepository<MessageTroc, Long> {
    List<MessageTroc> findByBrouillonTrue(); 

    List<MessageTroc> findByBrouillonFalse();

    List<MessageTroc> findByBrouillonFalseAndEnvoyerFalse();

    List<MessageTroc> findByIdTroqueurAndIdDestinataireAndIdFichierAndDateMessage(
        String idTroqueur, String idDestinataire, String idFichie, String dateMessage);

    //Optional<MessageTroc> findByIdTroqueurAndIdDestinataireAndIdFichierAndDateMessageAndStatut(
    //    String idTroqueur, String idDestinataire, String idFichier, String dateMessage, String statut);
        
    List<MessageTroc> findByIdTroqueurAndIdDestinataireAndIdFichierAndDateMessageAndStatut(
        String idTroqueur, String idDestinataire, String idFichier, String dateMessage, String statut);

    @Query("SELECT m FROM MessageTroc m JOIN FETCH m.objets WHERE m.idTroqueur = :idTroqueur AND m.idDestinataire = :idDestinataire AND m.idFichier = :idFichier AND m.dateMessage = :dateMessage AND m.statut = :statut")
    List<MessageTroc> findByCriteriaWithObjects(
        @Param("idTroqueur") String idTroqueur,
        @Param("idDestinataire") String idDestinataire,
        @Param("idFichier") String idFichier,
        @Param("dateMessage") String dateMessage,
        @Param("statut") String statut
    );

        
}
