package com.example.demo2.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

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
            String idDestinataire = json.getString("idDestinataire");

            JSONArray messagesArray = json.getJSONArray("messages");

            for (int i = 0; i < messagesArray.length(); i++) {
                JSONObject messageJson = messagesArray.getJSONObject(i);

                MessageTroc messageTroc = new MessageTroc();
                messageTroc.setIdTroqueur(idTroqueur);
                messageTroc.setIdDestinataire(idDestinataire);
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
                messageTroc.setBrouillon(false);
                trocRepository.save(messageTroc);

                System.out.println("Message Troc ajouté à la base de données : " + messageTroc.getId());
            }

        } catch (JSONException e) {
            System.err.println("Erreur lors du traitement du fichier JSON : " + e.getMessage());
        }
    }

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
        System.out.println("Autorisation ajoutée à la base de données : " + autorisation);
    }
}
