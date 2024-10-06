package com.example.demo2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonValidator {
	
	/*Vérifie la validité d'un fichier JSON */
    public static void jsonFileValidity(String chemin) {
        File file = new File(chemin);
        ObjectMapper mapper = new ObjectMapper();

        boolean valid = false;
        try {
            // Lire et vérifier la validité du fichier JSON
            JsonNode jsonNode = mapper.readTree(file);
            valid = true;
        } catch (IOException e) {
            // Le JSON est invalide ou une erreur s'est produite lors de la lecture
            valid = false;
        }

        // Afficher un message du résultat
        String nomFichier = file.getName();  // Extraire le nom du fichier
        if (valid) {
            System.out.println("Le fichier " + nomFichier + " contient un format JSON valide.");
        } else {
            System.out.println("Le fichier " + nomFichier + " ne contient pas un format JSON valide.");
        }
    }
	/* Vérifie la validité de tous les fichiers json dans un repertoire donnée */
    public static void RepoJsonFilesValidity(String dossierChemin) {
        File dossier = new File(dossierChemin);
        
        // Vérifier si le chemin est un répertoire
        if (dossier.isDirectory()) {
            File[] fichiers = dossier.listFiles();
            if (fichiers != null) {
                for (File fichier : fichiers) {
                    if (fichier.isFile() && fichier.getName().endsWith(".json")) {
                        jsonFileValidity(fichier.getAbsolutePath());
                    }
                }
            }
            else {
                System.out.println("Le repertoire est vide");
            }
        } else {
            System.out.println("Le chemin spécifié n'est pas un dossier.");
        }
    }

	/* Vérifie la validité d'un fichier JSON par rapport à un JSchema donnée */
    public static boolean isValidJsonAgainstSchema(File jsonFile, File schemaFile) {
        try (FileInputStream jsonFis = new FileInputStream(jsonFile);
             FileInputStream schemaFis = new FileInputStream(schemaFile)) {

            // Charger le JSON et le schéma
            JSONObject jsonObject = new JSONObject(new JSONTokener(jsonFis));
            JSONObject jsonSchema = new JSONObject(new JSONTokener(schemaFis));

            // Charger et appliquer le schéma
            Schema schema = SchemaLoader.load(jsonSchema);
            schema.validate(jsonObject); // Valide le JSON contre le schéma

            return true; // Le JSON est conforme au schéma

        } catch (Exception e) {
            // Le JSON est invalide ou non conforme au schéma
            System.out.println("Erreur de validation : " + e.getMessage());
            return false;
        }
    }


    public static void main(String[] args) {
        String chemin = "C:/Users/userlocal/Desktop/M1/DS/jsonFiles/file1.json";
        String cheminDossier = "C:/Users/userlocal/Desktop/M1/DS/jsonFiles"; 
        
        System.out.println();   // ajouter une ligne pour bien voir le résultat
        jsonFileValidity(chemin);				// vérifier un fichier json	
        RepoJsonFilesValidity(cheminDossier);	// vérifier les fichiers json d'un rep


        File jsonFile = new File("C:/Users/userlocal/Desktop/M1/DS/schemaFiles/schemaMessAuto.json");
        File schemaFile = new File("C:/Users/userlocal/Desktop/M1/DS/jsonFiles/jsonMessAuto1.json");

        boolean isValid = isValidJsonAgainstSchema(jsonFile, schemaFile);	// vérifier la validité JSchema <=> JSON

        System.out.println();  
        if (isValid) {
            System.out.println("Le fichier JSON est valide selon le schéma.");
        } else {
            System.out.println("Le fichier JSON n'est pas valide selon le schéma.");
        }
        System.out.println();

    }
}


