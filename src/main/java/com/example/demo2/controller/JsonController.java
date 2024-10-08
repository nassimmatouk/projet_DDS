package com.example.demo2.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class JsonController {

    @PostMapping("/save-json")
    public ResponseEntity<String> saveJson(@RequestBody String jsonData) {
        try {
            // Chemin du dossier local où les fichiers JSON seront enregistrés
            File jsonDir = new File("src/main/resources/json/message_envoi");

            // Assure-toi que le dossier "json" existe, sinon crée-le
            if (!jsonDir.exists()) {
                jsonDir.mkdir();
            }

            // Créer un fichier avec un nom unique (ou tu peux utiliser un nom basé sur l'utilisateur, etc.)
            File jsonFile = new File(jsonDir, "demande_troc_" + System.currentTimeMillis() + ".json");

            // Écrire les données dans le fichier
            try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                fileWriter.write(jsonData);
            }

            return new ResponseEntity<>("Fichier JSON enregistré avec succès.", HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("Erreur lors de l'enregistrement du fichier JSON.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
