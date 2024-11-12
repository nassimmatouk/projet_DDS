package com.example.demo2.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo2.AutorisationValidator;
import com.example.demo2.TrocValidator;
import com.example.demo2.model.MessageAutor;
import com.example.demo2.model.MessageTroc;
import com.example.demo2.repository.MessageAutorRepository;
import com.example.demo2.repository.MessageTrocRepository;

import jakarta.annotation.PostConstruct;

@Service
public class JsonFileWatcherService {

    private static final String DIRECTORY_PATH = "src/main/resources/json/message_recu";

    @Autowired
    private TrocValidator trocValidator;

    @Autowired
    private AutorisationValidator autorisationValidator;

    @Autowired
    private MessageTrocRepository trocRepository;

    @Autowired
    private MessageAutorRepository autorisationRepository;

    @PostConstruct
    public void init() {
        checkForJsonFiles();
    }

    public void checkForJsonFiles() {
        File jsonDir = new File(DIRECTORY_PATH);
        File[] files = jsonDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        if (files != null) {
            for (File file : files) {
                System.out.println("Fichier JSON détecté au démarrage : " + file.getName());
                try {
                    String content = new String(Files.readAllBytes(file.toPath()));
                    JSONObject json = new JSONObject(content);
                    if (json.has("messages")) {
                        if (trocValidator.validateJson(json)) {
                            System.out.println("Le JSON " + file.getName() + " est valide selon TrocValidator.");
                            addTrocBDD(json);
                        } else {
                            System.out.println("Le JSON " + file.getName() + " est invalide selon TrocValidator.");
                        }
                    } else if (json.has("MessageDemandeAutorisation")) {
                        if (autorisationValidator.validateJson(json)) {
                            System.out
                                    .println("Le JSON " + file.getName() + " est valide selon AutorisationValidator.");
                            addAutorBDD(json); 
                        } else {
                            System.out.println(
                                    "Le JSON " + file.getName() + " est invalide selon AutorisationValidator.");
                        }
                    } else {
                        System.err.println("Le JSON " + file.getName() + " ne correspond à aucun schéma");
                    }
                } catch (IOException e) {
                    System.err.println("Erreur dans l'accès aux fichiers json");
                } catch (JSONException e) {
                    System.out.println("Erreur lors du traitement du fichier JSON : " + e.getMessage());
                }
            }
        }
    }

    private void addTrocBDD(JSONObject json) {
        try {
            String idTroqueur = json.getString("idTroqueur");
            String idFichier = json.getString("idFichier");
            String dateFichier = json.getString("dateFichier");
    
            JSONArray messagesArray = json.getJSONArray("messages");
    
            for (int i = 0; i < messagesArray.length(); i++) {
                JSONObject messageJson = messagesArray.getJSONObject(i);
    
                MessageTroc messageTroc = new MessageTroc();
                messageTroc.setIdTroqueur(idTroqueur);  
                messageTroc.setIdFichier(idFichier);    
                messageTroc.setDateFichier(dateFichier); 
                messageTroc.setDateMessage(messageJson.getString("dateMessage"));
                messageTroc.setStatut(messageJson.getString("statut"));
    
                JSONArray objetsArray = messageJson.getJSONArray("listeObjet");
                List<MessageTroc.ObjetTroc> objetsTroc = new ArrayList<>();
    
                for (int j = 0; j < objetsArray.length(); j++) {
                    JSONObject objetJson = objetsArray.getJSONObject(j);
                    MessageTroc.ObjetTroc objetTroc = new MessageTroc.ObjetTroc();
    
                    objetTroc.setTitre(objetJson.getString("titre"));
                    objetTroc.setDescription(objetJson.getString("description"));
                    objetTroc.setQualite(objetJson.getInt("qualite"));
                    objetTroc.setQuantite(objetJson.getInt("quantite"));
                    objetsTroc.add(objetTroc);
                }
                messageTroc.setObjets(objetsTroc);
                trocRepository.save(messageTroc);
    
                System.out.println("\nMessage Troc ajouté à la base de données : " + messageTroc.getId()+"\n");
            }
    
        } catch (JSONException e) {
            System.err.println("Erreur lors du traitement du fichier JSON : " + e.getMessage());
        }
    }
    
    /*
    private void addAutorBDD(JSONObject json) {

        MessageAutor autorisation = new MessageAutor();
        autorisation.setIdTroqueur(json.getString("idTroqueur"));
        autorisation.setIdFichier(json.getString("idFichier"));
        autorisation.setDateFichier(json.getString("dateFichier"));
        autorisation.setStatutAutorisation(
                json.getJSONObject("MessageDemandeAutorisation").getString("statutAutorisation"));
        autorisation.setDate(json.getJSONObject("MessageDemandeAutorisation").getString("date"));
        autorisation.setIdMessage(json.getJSONObject("MessageDemandeAutorisation").getString("idMessage"));

        if (json.getJSONObject("MessageDemandeAutorisation").has("coordonnees")) {
            JSONObject coordonnees = json.getJSONObject("MessageDemandeAutorisation").getJSONObject("coordonnees");
            autorisation.setMail(coordonnees.optString("mail", null));
            autorisation.setTelephone(coordonnees.optString("telephone", null));
            autorisation.setNomAuteur(coordonnees.optString("nomAuteur", null));
        }

        autorisationRepository.save(autorisation);
        System.out.println("\nAutorisation ajoutée à la base de données : " + autorisation+"\n");
    }*/
    
    private void addAutorBDD(JSONObject json) {

        String idTroqueur = json.getString("idTroqueur");
        String idFichier = json.getString("idFichier");
        String idMessage = json.getJSONObject("MessageDemandeAutorisation").getString("idMessage");
        
        String statut = "";
        // Vérification de l'existence dans la base de données
        Optional<MessageAutor> existingAutor = autorisationRepository.findByIdTroqueurAndIdFichierAndIdMessage(idTroqueur, idFichier, idMessage);
        
        if (existingAutor.isPresent()) {
            System.out.println("\nAutorisation déjà présente dans la base de données : " + existingAutor.get());
            return; // Sortir de la méthode pour éviter une duplication
            //statut = "acceptORref"; // ce que le msg à été accepté ou réfusé
        }
        
        statut = (statut=="") ? (json.getJSONObject("MessageDemandeAutorisation").getString("statutAutorisation")) : statut;
        // Création d'un nouvel enregistrement si aucune correspondance n'est trouvée
        MessageAutor autorisation = new MessageAutor();
        autorisation.setIdTroqueur(idTroqueur);
        autorisation.setIdFichier(idFichier); 
        autorisation.setDateFichier(json.getString("dateFichier"));
        autorisation.setStatutAutorisation(json.getJSONObject("MessageDemandeAutorisation").getString("statutAutorisation"));
        //autorisation.setStatutAutorisation(statut);
        autorisation.setDate(json.getJSONObject("MessageDemandeAutorisation").getString("date"));
        autorisation.setIdMessage(idMessage);
    
        if (json.getJSONObject("MessageDemandeAutorisation").has("coordonnees")) {
            JSONObject coordonnees = json.getJSONObject("MessageDemandeAutorisation").getJSONObject("coordonnees");
            autorisation.setMail(coordonnees.optString("mail", null));
            autorisation.setTelephone(coordonnees.optString("telephone", null));
            autorisation.setNomAuteur(coordonnees.optString("nomAuteur", null));
        }
    
        autorisationRepository.save(autorisation); 
        System.out.println("\nAutorisation ajoutée à la base de données : " + autorisation.getStatutAutorisation() + "\n");
    }
    
    public boolean updateStatutAutorisation(String idTroqueur, String idFichier, String idMessage, String nouveauStatut) {
        File jsonDir = new File(DIRECTORY_PATH);
        File[] files = jsonDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        
        if (files == null) {
            System.err.println("Aucun fichier JSON trouvé dans le répertoire : " + DIRECTORY_PATH);
            return false;
        }
    
        for (File file : files) {
            try {
                // Lire le contenu du fichier JSON
                String content = new String(Files.readAllBytes(file.toPath()));
                JSONObject json = new JSONObject(content);
    
                // Vérifier si le JSON contient une demande d'autorisation et si les identifiants correspondent
                if (json.has("MessageDemandeAutorisation")) {
                    JSONObject messageJson = json.getJSONObject("MessageDemandeAutorisation");
                    
                    // Comparer les identifiants pour trouver le bon fichier
                    if (json.getString("idTroqueur").equals(idTroqueur) &&
                        json.getString("idFichier").equals(idFichier) &&
                        messageJson.getString("idMessage").equals(idMessage)) {
    
                        // Mettre à jour le statutAutorisation
                        messageJson.put("statutAutorisation", nouveauStatut);
    
                        // Sauvegarder les modifications dans le fichier
                        try (FileWriter fileWriter = new FileWriter(file)) {
                            fileWriter.write(json.toString(4)); // Sauvegarde avec une indentation de 4 espaces
                        }
                        
                        System.out.println("Statut autorisation mis à jour dans le fichier : " + file.getName());
                        return true; // Modification réussie
                    }
                }
            } catch (IOException | JSONException e) {
                System.err.println("Erreur lors de la modification du fichier JSON : " + e.getMessage());
            }
        }
        System.out.println("Fichier ou message non trouvé pour la mise à jour du statut.");
        return false; // Modification non effectuée
    }
    

}
