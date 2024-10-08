package com.example.demo2.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class JsonController {

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

            // Chemin du dossier local où les fichiers JSON seront enregistrés
            File jsonDir = new File("src/main/resources/json/message_envoi");

            // Assure-toi que le dossier "json" existe, sinon crée-le
            if (!jsonDir.exists()) {
                jsonDir.mkdir();
            }

            String fileName = generateFileName("troc", "g1.1", idDestinataire);

            // Créer un fichier avec un nom unique
            File jsonFile = new File(jsonDir, fileName);

            // Écrire les données dans le fichier
            try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                fileWriter.write(jsonData);
            }

            String redirectUrl = "/troc";

            // Renvoyer une réponse JSON avec l'URL de redirection
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

            // Chemin du dossier local où les fichiers JSON seront enregistrés
            File jsonDir = new File("src/main/resources/json/message_envoi");
            String fileName = generateFileName("autor", "g1.1", idDestinataire);

            // Assure-toi que le dossier "json" existe, sinon crée-le
            if (!jsonDir.exists()) {
                jsonDir.mkdir();
            }

            // Créer un fichier avec un nom unique
            File jsonFile = new File(jsonDir, fileName);

            // Écrire les données dans le fichier
            try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                fileWriter.write(jsonData);
            }

            String redirectUrl = "/autorisation";

            // Renvoyer une réponse JSON avec l'URL de redirection
            return new ResponseEntity<>("{\"success\": true, \"redirect\": \""+ redirectUrl + "\"}", HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("{\"success\": false, \"message\": \"Erreur lors de l'enregistrement.\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}