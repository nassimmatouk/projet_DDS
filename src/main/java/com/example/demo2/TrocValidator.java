package com.example.demo2;

import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class TrocValidator {

    private static final String ID_PATTERN = "^g\\d\\.\\d+$";
    private static final String DATE_PATTERN = "^\\d{2}-\\d{2}-\\d{4}$";
    private static final String STATUS_PATTERN = "^(accepte|valide|annule|refuse|propose)$";

    public boolean validateJson(JSONObject json) {
        if (!json.has("idTroqueur") || !json.has("idDestinataire") || !json.has("idFichier") ||
                !json.has("dateFichier") || !json.has("messages") || !json.has("checksum")) {
            return false; 
        }

        if (!isValidId(json.getString("idTroqueur")) ||
                !isValidId(json.getString("idDestinataire")) ||
                !isValidId(json.getString("idFichier")) ||
                !isValidDate(json.getString("dateFichier"))) {
            return false; 
        }

        return validateMessages(json.getJSONArray("messages"));
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
    

    private boolean validateMessages(JSONArray messages) {
        if (messages.length() < 1) {
            return false;
        }

        for (int i = 0; i < messages.length(); i++) {
            JSONObject message = messages.getJSONObject(i);
            if (!message.has("dateMessage") || !message.has("statut") || !message.has("listeObjet")) {
                return false; 
            }

            if (!isValidDate(message.getString("dateMessage")) ||
                    !Pattern.matches(STATUS_PATTERN, message.getString("statut")) ||
                    !validateListeObjet(message.getJSONArray("listeObjet"))) {
                return false; 
            }
        }
        return true;
    }

    private boolean validateListeObjet(JSONArray listeObjet) {
        for (int i = 0; i < listeObjet.length(); i++) {
            JSONObject objet = listeObjet.getJSONObject(i);
            if (!objet.has("titre") || !objet.has("qualite") || !objet.has("quantite")) {
                return false; 
            }

            if (!objet.getString("titre").isEmpty() &&
                    (objet.getInt("qualite") < 1 || objet.getInt("qualite") > 5) &&
                    (objet.getInt("quantite") < 1)) {
                return false; 
            }
        }
        return true;
    }
}
