package com.example.demo2.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo2.model.Contact;
import com.example.demo2.model.MessageAutor;
import com.example.demo2.service.ContactService;
import com.example.demo2.service.JsonFileWatcherService;
import com.example.demo2.service.MessageAutorService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class JsonController {

    @Autowired
    private JsonFileWatcherService jsonFileWatcherService;

    @Autowired
    private MessageAutorService messageAutorService;

    @Autowired
    private ContactService contactService;

    private String generateFileName(String type, String idTroqueur, String idDestinataire) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
        String currentDate = dateFormat.format(new Date());
        return type + "_" + idTroqueur + "_" + idDestinataire + "_" + currentDate + ".json";
    }

    @PostMapping("/save-troc")
    public ResponseEntity<String> saveTroc(@RequestBody String jsonData) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonData);
            String idDestinataire = rootNode.get("idDestinataire").asText();

            File jsonDir = new File("src/main/resources/json/message_envoi");

            if (!jsonDir.exists()) {
                jsonDir.mkdir();
            }

            String fileName = generateFileName("troc", "g1.1", idDestinataire);

            File jsonFile = new File(jsonDir, fileName);

            try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                fileWriter.write(jsonData);
            }

            String redirectUrl = "/troc";

            return new ResponseEntity<>("{\"success\": true, \"redirect\": \""+ redirectUrl + "\"}", HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("{\"success\": false, \"message\": \"Erreur lors de l'enregistrement.\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/save-autor")
    public ResponseEntity<String> saveAutor(@RequestBody String jsonData) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonData);
            String idDestinataire = rootNode.get("idDestinataire").asText();

            File jsonDir = new File("src/main/resources/json/message_envoi");
            String fileName = generateFileName("autor", "g1.1", idDestinataire);

            if (!jsonDir.exists()) {
                jsonDir.mkdir();
            }

            File jsonFile = new File(jsonDir, fileName);

            try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                fileWriter.write(jsonData);
            }

            String redirectUrl = "/autorisation";

            return new ResponseEntity<>("{\"success\": true, \"redirect\": \""+ redirectUrl + "\"}", HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("{\"success\": false, \"message\": \"Erreur lors de l'enregistrement.\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    
    @PostMapping("/update-statut")
    public ResponseEntity<String> updateStatut(@RequestBody Map<String, String> requestData) { 
        String idTroqueur = requestData.get("idTroqueur");
        String idFichier = requestData.get("idFichier");
        String idMessage = requestData.get("idMessage");
        String nouveauStatut = requestData.get("nouveauStatut");

        String msgId = requestData.get("msgId");

        // Mettre à jour le statut dans le fichier JSON
        boolean updateSuccess = jsonFileWatcherService.updateStatutAutorisation(idTroqueur, idFichier, idMessage, nouveauStatut);
        
        if (updateSuccess) { 
            // Si le statut est "accepte", ajouter le message dans les contacts
            if ("accepte".equals(nouveauStatut)) {
                MessageAutor m = messageAutorService.getMessageById(Long.parseLong(msgId));
                
                if (m != null) { 
                    Contact contact = new Contact(m.getId(), m.getNomAuteur(), m.getMail(), m.getTelephone(), m.getDate());
                    contactService.ajouterContact(contact); // Ajoute le message dans les contacts
                    messageAutorService.supprimerMessage(Long.parseLong(msgId)); // Supprime le message de la base
                    System.out.println("Message accepté et ajouté aux contacts.");
                }
            }
            else if("refuse".equals(nouveauStatut)){
                messageAutorService.supprimerMessage(Long.parseLong(msgId)); // Supprime le message de la base
                System.out.println("Message refusé et supprimer des msg_autorisations.");
            }
            return new ResponseEntity<>("{\"success\": true, \"message\": \"Statut mis à jour.\"}", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("{\"success\": false, \"message\": \"Échec de la mise à jour du statut.\"}", HttpStatus.NOT_FOUND);
        }
    }



}