package com.example.demo2.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo2.model.Contact;
import com.example.demo2.model.MessageAutor;
import com.example.demo2.model.MessageTroc;
import com.example.demo2.repository.MessageTrocRepository;
import com.example.demo2.service.ContactService;
import com.example.demo2.service.JsonFileWatcherService;
import com.example.demo2.service.MessageAutorService;
import com.example.demo2.service.MessageTrocService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class JsonController {

    @Autowired
    private MessageTrocService messageTrocService;

    @Autowired
    private MessageTrocRepository messageTrocRepository;

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

    /* Envoi simple de troc/autorisation */

    @PostMapping("/save-troc")
    public ResponseEntity<String> saveTroc(@RequestBody String jsonData,
            @RequestParam(value = "idMessage", required = false) List<Long> idMessages) {
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

            if (idMessages != null && !idMessages.isEmpty()) {
                for (Long idMessage : idMessages) {
                    try {
                        MessageTroc message = messageTrocRepository.findById(idMessage)
                                .orElseThrow(() -> new Exception("Message non trouvé"));
                        message.setBrouillon(false);
                        message.setEnvoyer(true);
                        message.setStatut("propose");
                        messageTrocRepository.save(message);
                    } catch (Exception e) {   
                        System.err.println(e);
                    }
                }
            } else {
                System.out.println("avant de rentrer dans la boucle");
                int i = 1;
                int j = 1;
                for (JsonNode messageNode : rootNode.get("messages")) {
                    System.out.println("message : " + i);
                    MessageTroc newMessage = new MessageTroc();
                    newMessage.setIdDestinataire(idDestinataire);
                    newMessage.setIdTroqueur("g1.1");
                    newMessage.setIdFichier("g1.1");
                    newMessage.setDateFichier(rootNode.get("dateFichier").asText());
                    newMessage.setDateMessage(messageNode.get("dateMessage").asText());
                    newMessage.setStatut("propose");
                    newMessage.setBrouillon(false);
                    newMessage.setEnvoyer(true);

                    // Remplir la liste des objets
                    List<MessageTroc.ObjetTroc> objets = new ArrayList<>();
                    for (JsonNode objetNode : messageNode.get("listeObjet")) {
                        MessageTroc.ObjetTroc objet = new MessageTroc.ObjetTroc();
                        objet.setTitre(objetNode.get("titre").asText());
                        objet.setDescription(objetNode.get("description").asText());
                        objet.setQualite(objetNode.get("qualite").asInt());
                        objet.setQuantite(objetNode.get("quantite").asInt());
                        objets.add(objet);
                        System.out.println("objet : " + j);
                        j++;
                    }
                    newMessage.setObjets(objets);

                    messageTrocRepository.save(newMessage);
                    i++;
                }
            }

            //String redirectUrl = "/message-troc";
            return new ResponseEntity<>("{\"success\": true, \"" + "\"}", HttpStatus.OK);
        } catch (

        IOException e) {
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

    @PostMapping("/delete-selected-brouillon")
    public ResponseEntity<Void> deleteSelectedBrouillons(@RequestParam("idMessages") String idMessages) {
        // Séparer la chaîne des IDs sélectionnés et les convertir en une liste de Long
        List<Long> selectedMessageIds = Arrays.stream(idMessages.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        selectedMessageIds.forEach(messageTrocService::deleteBrouillon);

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

    @PostMapping("/update-statut")
    public ResponseEntity<String> updateStatut(@RequestBody Map<String, String> requestData) {
        String idTroqueur = requestData.get("idTroqueur");
        String idFichier = requestData.get("idFichier");
        String idMessage = requestData.get("idMessage");
        String nouveauStatut = requestData.get("nouveauStatut");

        String msgId = requestData.get("msgId");

        // Mettre à jour le statut dans le fichier JSON
        boolean updateSuccess = jsonFileWatcherService.updateStatutAutorisation(idTroqueur, idFichier, idMessage,
                nouveauStatut);

        if (updateSuccess) {
            // Si le statut est "accepte", ajouter le message dans les contacts
            if ("accepte".equals(nouveauStatut)) {
                MessageAutor m = messageAutorService.getMessageById(Long.valueOf(msgId));

                if (m != null) {
                    Contact contact = new Contact(m.getId(), m.getIdTroqueur(), m.getNomAuteur(), m.getMail(),
                            m.getTelephone(),
                            m.getDate());
                    contactService.ajouterContact(contact); // Ajoute le message dans les contacts
                    messageAutorService.supprimerMessage(Long.valueOf(msgId)); // Supprime le message de la base
                    System.out.println("Message accepté et ajouté aux contacts.");
                }
            } else if ("refuse".equals(nouveauStatut)) {
                messageAutorService.supprimerMessage(Long.valueOf(msgId)); // Supprime le message de la base
                System.out.println("Message refusé et supprimer des msg_autorisations.");
            }
            return new ResponseEntity<>("{\"success\": true, \"message\": \"Statut mis à jour.\"}", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("{\"success\": false, \"message\": \"Échec de la mise à jour du statut.\"}",
                    HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/get-message-info/{messageId}")
    public ResponseEntity<Map<String, Object>> getMessageInfo(@PathVariable Long messageId) {
        try {
            MessageTroc message = messageTrocRepository.findById(messageId)
                    .orElseThrow(() -> new Exception("Message non trouvé"));

            // Préparer les données
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    
    @PostMapping("refuser-troc")
    public ResponseEntity<?> refuserTroc(@RequestBody Map<String, String> requestData) { 
        try {
            // Récupérer les informations du JSON envoyé par le script
            String idTroqueur = requestData.get("idTroqueur");
            String idDestinataire = requestData.get("idDestinataire");
            String dateMessage = requestData.get("dateMessage");
            String description = requestData.get("descriptionObj"); // Récupérer la description du message
            String msgId = requestData.get("msgId");

            // Utiliser la méthode refuserTroc pour déplacer le fichier
            boolean fichierDeplace = jsonFileWatcherService.refuserTroc(idTroqueur, idDestinataire, dateMessage, description);
            
            // Si le fichier a été déplacé avec succès, supprimer le message de la base de données
            if (fichierDeplace) { 
                System.out.println("hereDep  : " + idTroqueur +"__"+ idDestinataire +"__"+ dateMessage +"__"+ msgId);
                boolean messageSupprime = messageTrocService.supprimerMessageParId(msgId);                  
                if (messageSupprime) {
                    return ResponseEntity.ok(Map.of("success", true, "message", "Troc refusé et mis à jour."));
                } else {
                    return ResponseEntity.ok(Map.of("success", false, "message", "Erreur lors de la suppression du message en base de données."));
                }
            } else {  
                return ResponseEntity.ok(Map.of("success", false, "message", "Erreur lors du déplacement du fichier."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Erreur interne : " + e.getMessage()));
        }
    }

    
    @PostMapping("/save-resp")
    public ResponseEntity<String> saveResp(@RequestBody String jsonData,
            @RequestParam(value = "idMessage", required = false) List<Long> idMessages) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonData);
            String idDestinataire = rootNode.get("idDestinataire").asText();

            File jsonDir = new File("src/main/resources/json/messages_reponses");

            if (!jsonDir.exists()) {
                jsonDir.mkdir();
            } 

            String fileName = generateFileName("rep", "g1.1", idDestinataire);

            File jsonFile = new File(jsonDir, fileName);

            try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                fileWriter.write(jsonData);
            }

            if (idMessages != null && !idMessages.isEmpty()) { System.out.println("\n\nINidm...\n\n");
                for (Long idMessage : idMessages) {
                    try {
                        MessageTroc message = messageTrocRepository.findById(idMessage)
                                .orElseThrow(() -> new Exception("Message non trouvé"));
                        message.setBrouillon(false);
                        message.setEnvoyer(true);
                        message.setStatut("accepte");
                        messageTrocRepository.save(message);
                    } catch (Exception e) {   
                        System.err.println(e);
                    }
                }
            } else {
                System.out.println("avant de rentrer dans la boucle");
                int i = 1;
                int j = 1;
                for (JsonNode messageNode : rootNode.get("messages")) {
                    System.out.println("message : " + i);
                    MessageTroc newMessage = new MessageTroc();
                    newMessage.setIdDestinataire(idDestinataire);
                    newMessage.setIdTroqueur("g1.1");
                    newMessage.setIdFichier("g1.1");
                    newMessage.setDateFichier(rootNode.get("dateFichier").asText());
                    newMessage.setDateMessage(messageNode.get("dateMessage").asText());
                    newMessage.setStatut("accepte");
                    newMessage.setBrouillon(false);
                    newMessage.setEnvoyer(true);

                    // Remplir la liste des objets
                    List<MessageTroc.ObjetTroc> objets = new ArrayList<>();
                    for (JsonNode objetNode : messageNode.get("listeObjet")) {
                        MessageTroc.ObjetTroc objet = new MessageTroc.ObjetTroc();
                        objet.setTitre(objetNode.get("titre").asText());
                        objet.setDescription(objetNode.get("description").asText());
                        objet.setQualite(objetNode.get("qualite").asInt());
                        objet.setQuantite(objetNode.get("quantite").asInt());
                        objets.add(objet);
                        System.out.println("objet : " + j);
                        j++;
                    }
                    newMessage.setObjets(objets);

                    messageTrocRepository.save(newMessage);
                    i++;
                }
            }

            String redirectUrl = "/message-troc";
            return new ResponseEntity<>("{\"success\": true, \"redirect\": \"" + redirectUrl + "\"}", HttpStatus.OK);
        } catch (

        IOException e) {
            return new ResponseEntity<>("{\"success\": false, \"message\": \"Erreur lors de l'enregistrement.\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}