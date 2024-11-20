package com.example.demo2.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
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
    private static final String DIRECTORY_PATH_INVALIDES = "src/main/resources/json/messages_invalides";
    private static final String DIRECTORY_PATH_TROC_VALIDES = "src/main/resources/json/troc_valides";
    private static final String DIRECTORY_PATH_AUTOR_VALIDES = "src/main/resources/json/autor_valides";

    private static final String DIRECTORY_TROC_ACCEPTES = "src/main/resources/json/message_accepter";
    private static final String DIRECTORY_TROC_REFUSES = "src/main/resources/json/message_refuses";

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
        //refuserTroc("troc_g1.1_g1.6_20241023_1658.json");
    }

    public void checkForJsonFiles() {
        File jsonDir = new File(DIRECTORY_PATH);
        File[] files = jsonDir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (!file.getName().toLowerCase().endsWith(".json")) {
                    System.out.println("Le fichier " + file.getName()
                            + " ne se termine pas par .json");
                    moveFileToDirectory(file, DIRECTORY_PATH_INVALIDES);
                } else if (file.length() > 2048) {
                    System.out.println("Le fichier " + file.getName() + " est trop grand (> 2 Ko)");
                    moveFileToDirectory(file, DIRECTORY_PATH_INVALIDES);
                } else {
                    System.out.println("Fichier JSON détecté au démarrage : " + file.getName());
                    try {
                        String content = new String(Files.readAllBytes(file.toPath()));
                        JSONObject json = new JSONObject(content);
                        if (json.has("messages")) {
                            if (trocValidator.validateJson(json)) {
                                System.out.println("Le JSON " + file.getName() + " est valide selon TrocValidator.");
                                addTrocBDD(json);
                                moveFileToDirectory(file, DIRECTORY_PATH_TROC_VALIDES);
                            } else {
                                System.out.println("Le JSON " + file.getName() + " est invalide selon TrocValidator.");
                                moveFileToDirectory(file, DIRECTORY_PATH_INVALIDES);
                            }
                        } else if (json.has("MessageDemandeAutorisation")) {
                            if (autorisationValidator.validateJson(json)) {
                                System.out
                                        .println("Le JSON " + file.getName()
                                                + " est valide selon AutorisationValidator.");
                                addAutorBDD(json);
                                moveFileToDirectory(file, DIRECTORY_PATH_AUTOR_VALIDES);
                            } else {
                                System.out.println(
                                        "Le JSON " + file.getName() + " est invalide selon AutorisationValidator.");
                                moveFileToDirectory(file, DIRECTORY_PATH_INVALIDES);
                            }
                        } else {
                            System.err.println("Le JSON " + file.getName() + " ne correspond à aucun schéma");
                            moveFileToDirectory(file, DIRECTORY_PATH_INVALIDES);
                        }
                    } catch (IOException e) {
                        System.err.println("Erreur dans l'accès aux fichiers json");
                    } catch (JSONException e) {
                        System.out.println("Erreur lors du traitement du fichier JSON : " + e.getMessage());
                    }
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
                messageTroc.setEnvoyer(false);
                trocRepository.save(messageTroc);

                System.out.println("\nMessage Troc ajouté à la base de données : " + messageTroc.getId() + "\n");
            }

        } catch (JSONException e) {
            System.err.println("Erreur lors du traitement du fichier JSON : " + e.getMessage());
        }
    }
    /*
    private void addTrocBDD(JSONObject json) {
        try {
            String idTroqueur = json.getString("idTroqueur");
            String idFichier = json.getString("idFichier");
            String dateFichier = json.getString("dateFichier");
            String idDestinataire = json.getString("idDestinataire");
    
            JSONArray messagesArray = json.getJSONArray("messages");
    
            for (int i = 0; i < messagesArray.length(); i++) {
                JSONObject messageJson = messagesArray.getJSONObject(i);
    
                String dateMessage = messageJson.getString("dateMessage");
                String statut = messageJson.getString("statut");
    
                // Vérification de l'existence dans la base de données
                Optional<MessageTroc> existingMessageTroc = trocRepository
                    .findByIdTroqueurAndIdDestinataireAndIdFichierAndDateMessage(
                        idTroqueur, idDestinataire, idFichier, dateMessage);
    
                if (existingMessageTroc.isPresent()) {
                    System.out.println("\nMessage Troc déjà présent dans la base de données : " 
                                       + existingMessageTroc.get().getId());
                    continue; // Passer à l'itération suivante pour éviter la duplication
                }
    
                // Création d'un nouvel enregistrement si aucune correspondance n'est trouvée
                MessageTroc messageTroc = new MessageTroc();
                messageTroc.setIdTroqueur(idTroqueur);
                messageTroc.setIdDestinataire(idDestinataire);
                messageTroc.setIdFichier(idFichier);
                messageTroc.setDateFichier(dateFichier);
                messageTroc.setDateMessage(dateMessage);
                messageTroc.setStatut(statut);
    
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
                messageTroc.setEnvoyer(false);
    
                // Enregistrement dans la base de données
                trocRepository.save(messageTroc);
    
                System.out.println("\nMessage Troc ajouté à la base de données : " + messageTroc.getId() + "\n");
            }
    
        } catch (JSONException e) {
            System.err.println("Erreur lors du traitement du fichier JSON : " + e.getMessage());
        }
    }*/ 
       
    private void addAutorBDD(JSONObject json) {

        String idTroqueur = json.getString("idTroqueur");
        String idFichier = json.getString("idFichier");
        String idMessage = json.getJSONObject("MessageDemandeAutorisation").getString("idMessage");

        // Vérification de l'existence dans la base de données
        Optional<MessageAutor> existingAutor = autorisationRepository
                .findByIdTroqueurAndIdFichierAndIdMessage(idTroqueur, idFichier, idMessage);

        if (existingAutor.isPresent()) {
            System.out.println("\nAutorisation déjà présente dans la base de données : " + existingAutor.get());
            return; // Sortir de la méthode pour éviter une duplication
        }

        // Création d'un nouvel enregistrement si aucune correspondance n'est trouvée
        MessageAutor autorisation = new MessageAutor();
        autorisation.setIdTroqueur(idTroqueur);
        autorisation.setIdFichier(idFichier);
        autorisation.setDateFichier(json.getString("dateFichier"));
        autorisation.setStatutAutorisation(
                json.getJSONObject("MessageDemandeAutorisation").getString("statutAutorisation"));
        autorisation.setDate(json.getJSONObject("MessageDemandeAutorisation").getString("date"));
        autorisation.setIdMessage(idMessage);

        if (json.getJSONObject("MessageDemandeAutorisation").has("coordonnees")) {
            JSONObject coordonnees = json.getJSONObject("MessageDemandeAutorisation").getJSONObject("coordonnees");
            autorisation.setMail(coordonnees.optString("mail", null));
            autorisation.setTelephone(coordonnees.optString("telephone", null));
            autorisation.setNomAuteur(coordonnees.optString("nomAuteur", null));
        }

        autorisationRepository.save(autorisation);
        System.out.println(
                "\nAutorisation ajoutée à la base de données : " + autorisation.getStatutAutorisation() + "\n");
    }

    public boolean updateStatutAutorisation(String idTroqueur, String idFichier, String idMessage,
            String nouveauStatut) {
        File jsonDir = new File(DIRECTORY_PATH_AUTOR_VALIDES);
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

                // Vérifier si le JSON contient une demande d'autorisation et si les
                // identifiants correspondent
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

    // deplacer les fichiers pour eviter de les reafficher a chaque fois
    private void moveFileToDirectory(File file, String targetDirectory) { 
        File targetDir = new File(targetDirectory);
        if (!targetDir.exists()) {
            targetDir.mkdirs(); // Créer le dossier s'il n'existe pas encore
        }

        File targetFile = new File(targetDir, file.getName());
        if (file.renameTo(targetFile)) {
            System.out.println("Fichier déplacé vers : " + targetFile.getAbsolutePath() + "\n");
        } else {
            System.err.println("Échec du déplacement du fichier : " + file.getName() + "\n");
        }

    }




    public void refuserTrocOld(String fileName) {
        File sourceFile = new File(DIRECTORY_PATH_TROC_VALIDES, fileName);
        File targetFile = new File(DIRECTORY_TROC_REFUSES, fileName);
    
        if (!sourceFile.exists()) {
            System.err.println("Le fichier n'existe pas dans le répertoire des trocs validés : " + fileName);
            return;
        }
    
        try {
            // Lire le contenu du fichier JSON
            String content = new String(Files.readAllBytes(sourceFile.toPath()));
            JSONObject json = new JSONObject(content);
    
            // Modifier le statut des messages à "refuse"
            if (json.has("messages")) {
                JSONArray messagesArray = json.getJSONArray("messages");
                for (int i = 0; i < messagesArray.length(); i++) {
                    JSONObject messageJson = messagesArray.getJSONObject(i);
                    messageJson.put("statut", "refuse");
                }
            }
    
            // Créer le répertoire cible s'il n'existe pas encore
            File targetDir = new File(DIRECTORY_TROC_REFUSES);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
    
            // Sauvegarder le fichier modifié dans le répertoire cible
            try (FileWriter fileWriter = new FileWriter(targetFile)) {
                fileWriter.write(json.toString(4)); // Sauvegarde avec une indentation de 4 espaces
            }
    
            // Supprimer le fichier d'origine
            if (sourceFile.delete()) {
                System.out.println("Fichier déplacé et mis à jour avec le statut 'refuse' : " + targetFile.getAbsolutePath());
            } else {
                System.err.println("Erreur lors de la suppression du fichier source : " + fileName);
            }
    
        } catch (IOException | JSONException e) {
            System.err.println("Erreur lors de la modification ou du déplacement du fichier JSON : " + e.getMessage());
        }
    }
    
    public boolean refuserTroc(String idTroqueur, String idDestinataire, String dateMessage) {
        // 1. Convertir la date de jj-mm-aaaa à aaaaMMjj
        String dateFormatee = convertirDate(dateMessage);
        if (dateFormatee == null) {
            System.err.println("Format de date incorrect : " + dateMessage);
            return false;
        }

        // 2. Construire le préfixe pour la recherche de fichier
        String prefix = "troc_" + idTroqueur + "_" + idDestinataire + "_" + dateFormatee;
        System.out.println("prefix : " + prefix);

        // 3. Rechercher le fichier JSON correspondant
        File fichierSource = rechercherFichierJson(prefix);
        if (fichierSource == null) {
            System.err.println("Aucun fichier ne correspond au préfixe : " + prefix);
            return false;
        }

        try {
            // 4. Lire et parser le contenu JSON
            JSONObject json = lireFichierJson(fichierSource);

            // 5. Mettre à jour le statut du JSON
            JSONObject jsonModifie = mettreAJourStatut(json);

            // 6. Formater et afficher le JSON mis à jour
            String jsonFormate = formaterJson(jsonModifie);

            // Afficher le JSON reconstruit
            System.out.println("\n\n\nJsonModifie : \n");
            System.out.println(jsonFormate);
            System.out.println("\n\n\n");

            // 7. Sauvegarder les modifications dans le fichier original
            sauvegarderJson(fichierSource, jsonFormate);

            // 8. Déplacer le fichier vers le répertoire des messages refusés
            moveFileToDirectory(fichierSource, DIRECTORY_TROC_REFUSES);

            System.out.println("Fichier déplacé vers les refusés : " + fichierSource.getName());
            return true;

        } catch (IOException | JSONException e) {
            System.err.println("Erreur lors du traitement du fichier JSON : " + e.getMessage());
            return false;
        }
    }

    // 1. Convertir la date de jj-mm-aaaa à aaaaMMjj
    private String convertirDate(String dateMessage) {
        String[] dateParts = dateMessage.split("-");
        if (dateParts.length != 3) {
            return null;
        }
        return dateParts[2] + dateParts[1] + dateParts[0]; // aaaaMMjj
    }

    // 2. Rechercher le fichier JSON correspondant
    private File rechercherFichierJson(String prefix) {
        File directory = new File(DIRECTORY_PATH_TROC_VALIDES);
        File[] matchingFiles = directory.listFiles((dir, name) -> name.startsWith(prefix) && name.endsWith(".json"));

        if (matchingFiles == null || matchingFiles.length == 0) {
            return null;
        }
        return matchingFiles[0]; // Supposons qu'il n'y a qu'un seul fichier qui correspond
    }

    // 3. Lire le contenu du fichier JSON
    private JSONObject lireFichierJson(File fichier) throws IOException, JSONException {
        String contenuJson = new String(Files.readAllBytes(fichier.toPath()));
        return new JSONObject(contenuJson);
    }

    // 4. Mettre à jour le statut des messages dans le JSON
    private JSONObject mettreAJourStatut(JSONObject json) {
        // Créer un nouvel objet JSON pour maintenir l'ordre des propriétés
        Map<String, Object> jsonModifieMap = new LinkedHashMap<>();
        jsonModifieMap.put("idTroqueur", json.getString("idTroqueur"));
        jsonModifieMap.put("idDestinataire", json.getString("idDestinataire"));
        jsonModifieMap.put("idFichier", json.getString("idFichier"));
        jsonModifieMap.put("dateFichier", json.getString("dateFichier"));
        jsonModifieMap.put("checksum", json.getString("checksum"));

        // Accéder au tableau "messages" et mettre à jour le statut
        if (json.has("messages")) {
            JSONArray messagesArray = json.getJSONArray("messages");
            JSONArray messagesModifies = new JSONArray();

            for (int i = 0; i < messagesArray.length(); i++) {
                JSONObject message = messagesArray.getJSONObject(i);

                // Créer un nouvel objet message pour conserver l'ordre
                Map<String, Object> messageModifieMap = new LinkedHashMap<>();
                messageModifieMap.put("dateMessage", message.getString("dateMessage")); // Garder "dateMessage" avant
                messageModifieMap.put("statut", "refuse"); // Mettre à jour le statut

                // Copier et structurer le tableau "listeObjet"
                if (message.has("listeObjet")) {
                    JSONArray listeObjetArray = message.getJSONArray("listeObjet");
                    JSONArray listeObjetModifiee = new JSONArray();

                    for (int j = 0; j < listeObjetArray.length(); j++) {
                        JSONObject objet = listeObjetArray.getJSONObject(j);

                        // Structurer chaque objet de la liste
                        Map<String, Object> objetMap = new LinkedHashMap<>();
                        objetMap.put("titre", objet.getString("titre"));
                        objetMap.put("description", objet.getString("description"));
                        objetMap.put("qualite", objet.getInt("qualite"));
                        objetMap.put("quantite", objet.getInt("quantite"));

                        // Ajouter l'objet structuré à la liste
                        listeObjetModifiee.put(new JSONObject(objetMap));
                    }

                    // Ajouter la liste des objets structurés au message
                    messageModifieMap.put("listeObjet", listeObjetModifiee);
                }

                // Convertir le map en JSONObject et l'ajouter à la liste des messages modifiés
                messagesModifies.put(new JSONObject(messageModifieMap));
            }

            // Ajouter le tableau de messages modifiés au nouvel objet JSON
            jsonModifieMap.put("messages", messagesModifies);  
        }
        
        return new JSONObject(jsonModifieMap);
    }

    // 5. Formater et afficher JSON structuré
    private String formaterJson(JSONObject jsonModifie) {
        return jsonModifie.toString(4); // Formattage avec indentation de 4 espaces
    }

    // 6. Sauvegarder le JSON mis à jour dans un fichier
    private void sauvegarderJson(File fichier, String contenuJson) throws IOException {
        try (FileWriter fileWriter = new FileWriter(fichier)) {
            fileWriter.write(contenuJson); // Sauvegarder avec l'ordre respecté
        }
    }
    /*
    // 8. Déplacer le fichier vers un autre répertoire
    private void moveFileToDirectory(File file, String targetDirectory) throws IOException {
        File targetDir = new File(targetDirectory);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        Files.move(file.toPath(), new File(targetDir, file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
    }*/  

    
    
    
    
                
    
    
    
}


