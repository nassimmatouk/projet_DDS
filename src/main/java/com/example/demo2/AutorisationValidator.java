package com.example.demo2;

import java.util.regex.Pattern;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class AutorisationValidator {

    // Regex patterns pour les validations
    private static final String ID_PATTERN = "^g\\d\\.\\d+$";
    private static final String DATE_PATTERN = "^\\d{2}-\\d{2}-\\d{4}$";
    private static final String STATUT_AUTORISATION_PATTERN = "^(accepte|refuse|demande)$";
    private static final String MAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String TELEPHONE_PATTERN = "^\\+?[0-9]{10,15}$";

    public boolean validateJson(JSONObject json) {
        // Vérification des champs requis
        if (!json.has("idTroqueur") || !json.has("idDestinataire") || !json.has("idFichier") ||
                !json.has("dateFichier") || !json.has("MessageDemandeAutorisation") || !json.has("checksum")) {
            return false; // Un ou plusieurs champs requis manquent
        }

        // Validation des champs
        if (!isValidId(json.getString("idTroqueur")) ||
                !isValidId(json.getString("idDestinataire")) ||
                !isValidId(json.getString("idFichier")) ||
                !isValidDate(json.getString("dateFichier"))) {
            return false; // Un ou plusieurs champs ont un format invalide
        }

        // Validation de MessageDemandeAutorisation
        JSONObject messageDemandeAutorisation = json.getJSONObject("MessageDemandeAutorisation");
        // JSON valide
        return validateMessageDemandeAutorisation(messageDemandeAutorisation);
    }

    private boolean isValidId(String id) {
        return Pattern.matches(ID_PATTERN, id);
    }

    private boolean isValidDate(String date) {
        if (!Pattern.matches(DATE_PATTERN, date)) {
            return false;
        }
    
        String[] parts = date.split("-");
        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
    
        if (month < 1 || month > 12 || day < 1 || day > 31) {
            return false;
        }
    
        if (month == 2 && day > 29) { 
            return false;
        }
    
        return !((month == 4 || month == 6 || month == 9 || month == 11) && day > 30); 
    }

    private boolean validateMessageDemandeAutorisation(JSONObject message) {
        // Vérification des champs requis
        if (!message.has("statutAutorisation") || !message.has("date") || !message.has("idMessage")) {
            return false; // Un ou plusieurs champs requis manquent
        }

        // Validation des champs dans MessageDemandeAutorisation
        if (!Pattern.matches(STATUT_AUTORISATION_PATTERN, message.getString("statutAutorisation")) ||
                !isValidDate(message.getString("date"))) {
            return false; // Un ou plusieurs champs ont un format invalide
        }

        // Vérification des coordonnées si elles existent
        if (message.has("coordonnees")) {
            JSONObject coordonnees = message.getJSONObject("coordonnees");
            if (!validateCoordonnees(coordonnees)) {
                return false;
            }
        }

        return true;
    }

    private boolean validateCoordonnees(JSONObject coordonnees) {
        boolean valid = true;

        if (coordonnees.has("mail")) {
            String mail = coordonnees.getString("mail");
            valid = valid && Pattern.matches(MAIL_PATTERN, mail);
        }

        if (coordonnees.has("telephone")) {
            String telephone = coordonnees.getString("telephone");
            valid = valid && Pattern.matches(TELEPHONE_PATTERN, telephone);
        }

        if (coordonnees.has("nomAuteur")) {
            String nomAuteur = coordonnees.getString("nomAuteur");
            valid = valid && !nomAuteur.trim().isEmpty();
        }

        return valid;
    }
}
