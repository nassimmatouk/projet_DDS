package com.example.demo2.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo2.model.MessageTroc;
import com.example.demo2.repository.MessageTrocRepository;
import com.example.demo2.service.MessageTrocService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class APIController {

    @Autowired
    private MessageTrocService messageTrocService;

    @Autowired
    private MessageTrocRepository messageTrocRepository;

    private String generateFileName(String type, String idTroqueur, String idDestinataire) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
        String currentDate = dateFormat.format(new Date());
        return type + "_" + idTroqueur + "_" + idDestinataire + "_" + currentDate + ".json";
    }

    /* Envoi simple de troc/autorisation */

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

            return new ResponseEntity<>("{\"success\": true, \"redirect\": \"" + redirectUrl + "\"}", HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("{\"success\": false, \"message\": \"Erreur lors de l'enregistrement.\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
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

            return new ResponseEntity<>("{\"success\": true, \"redirect\": \"" + redirectUrl + "\"}", HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("{\"success\": false, \"message\": \"Erreur lors de l'enregistrement.\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /* Gestion des brouillons */

    // Enregistrer un brouillon de troc
    @PostMapping("/save-troc-draft")
    public ResponseEntity<String> saveTrocDraft(@RequestBody String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            String idTroqueur = json.getString("idTroqueur");
            String idFichier = json.getString("idFichier");
            String dateFichier = json.getString("dateFichier");
            String idDestinataire = json.getString("idDestinataire");

            if (idDestinataire == null || idDestinataire.isEmpty()) {
                JSONArray messagesArray = json.getJSONArray("messages");
                boolean hasObject = false;

                for (int i = 0; i < messagesArray.length(); i++) {
                    JSONObject messageJson = messagesArray.getJSONObject(i);
                    JSONArray objetsArray = messageJson.getJSONArray("listeObjet");

                    // Vérification si au moins un objet est renseigné
                    for (int j = 0; j < objetsArray.length(); j++) {
                        JSONObject objetJson = objetsArray.getJSONObject(j);
                        if (!objetJson.getString("titre").isEmpty() || !objetJson.getString("description").isEmpty() ||
                                !objetJson.isNull("qualite") || !objetJson.isNull("quantite")) {
                            hasObject = true;
                            break;
                        }
                    }
                    if (hasObject)
                        break;
                }

                // Si aucun destinataire ou objet n'est renseigné, ne pas enregistrer
                if (idDestinataire == null || idDestinataire.isEmpty() && !hasObject) {
                    return new ResponseEntity<>("{\"success\": false, \"message\": \"Le brouillon est vide.\"}",
                            HttpStatus.BAD_REQUEST);
                }
            }

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
                    if (!objetJson.isNull("qualite")) {
                        objetTroc.setQualite(objetJson.getInt("qualite"));
                    }
                    if (!objetJson.isNull("quantite")) {
                        objetTroc.setQuantite(objetJson.getInt("quantite"));
                    }
                    objetsTroc.add(objetTroc);
                }

                messageTroc.setObjets(objetsTroc);
                messageTroc.setBrouillon(true);
                messageTrocService.saveBrouillon(messageTroc);
                System.out.println("Brouillon ajouté à la base de données : " + messageTroc.getId());
            }

            String redirectUrl = "/message-troc";

            return new ResponseEntity<>("{\"success\": true, \"redirect\": \"" + redirectUrl + "\"}", HttpStatus.OK);
        } catch (JSONException e) {
            System.err.println("exception " + e);
            return new ResponseEntity<>("{\"success\": false, \"message\": \"Erreur lors de l'enregistrement.\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/delete-brouillon")
    public ResponseEntity<Void> deleteBrouillon(@RequestParam("idMessage") Long idMessage) {
        messageTrocService.deleteBrouillon(idMessage);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/message-troc")
                .build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateMessage(@PathVariable Long id, @RequestBody String jsonString) {
        Optional<MessageTroc> optionalMessage = messageTrocRepository.findById(id); // Trouver l'entité

        if (optionalMessage.isPresent()) {
            MessageTroc existingMessage = optionalMessage.get();
            try {
                JSONObject json = new JSONObject(jsonString);
                String idDestinataire = json.getString("idDestinataire");

                JSONArray messagesArray = json.getJSONArray("messages");
                for (int i = 0; i < messagesArray.length(); i++) {
                    JSONObject messageJson = messagesArray.getJSONObject(i);
                    existingMessage.setIdDestinataire(idDestinataire);
                    existingMessage.setDateMessage(messageJson.getString("dateMessage"));

                    JSONArray objetsArray = messageJson.getJSONArray("listeObjet");
                    List<MessageTroc.ObjetTroc> objetsTroc = new ArrayList<>();
                    for (int j = 0; j < objetsArray.length(); j++) {
                        JSONObject objetJson = objetsArray.getJSONObject(j);
                        MessageTroc.ObjetTroc objetTroc = new MessageTroc.ObjetTroc();
                        objetTroc.setTitre(objetJson.getString("titre"));
                        objetTroc.setDescription(objetJson.getString("description"));
                        if (!objetJson.isNull("qualite")) {
                            objetTroc.setQualite(objetJson.getInt("qualite"));
                        }
                        if (!objetJson.isNull("quantite")) {
                            objetTroc.setQuantite(objetJson.getInt("quantite"));
                        }
                        objetsTroc.add(objetTroc);
                    }

                    existingMessage.setObjets(objetsTroc);
                    existingMessage.setBrouillon(true);
                    messageTrocRepository.save(existingMessage);
                }

                String redirectUrl = "/message-troc";
                return new ResponseEntity<>("{\"success\": true, \"redirectUrl\": \"" + redirectUrl + "\"}",
                        HttpStatus.OK);
            } catch (JSONException e) {
                System.err.println("exception " + e);
                return new ResponseEntity<>("{\"success\": false, \"message\": \"Erreur lors de l'enregistrement.\"}",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("{\"success\": false, \"message\": \"Message non trouvé.\"}",
                    HttpStatus.NOT_FOUND);
        }
    }
}