package com.example.demo2.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    private static final String DIRECTORY_PATH_INVALIDES = "src/main/resources/json/messages_invalides";
    private static final String DIRECTORY_PATH_TROC_VALIDES = "src/main/resources/json/troc_valides";
    private static final String DIRECTORY_PATH_AUTOR_VALIDES = "src/main/resources/json/autor_valides";

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
                                if (isChecksumValid(json)) {
                                    String dateFichier = json.getString("dateFichier");
                                    if (isMessageExpired(dateFichier)) {
                                        System.err.println("Fichier rejeté : périmé (date : " + dateFichier + ").");
                                        moveFileToDirectory(file, DIRECTORY_PATH_INVALIDES);
                                    } else {
                                        addTrocBDD(json);
                                        moveFileToDirectory(file, DIRECTORY_PATH_TROC_VALIDES);
                                    }
                                } else {
                                    System.err.println("Checksum invalide pour le fichier " + file.getName());
                                    moveFileToDirectory(file, DIRECTORY_PATH_INVALIDES);
                                }
                            } else {
                                System.out.println("Le JSON " + file.getName() + " est invalide selon TrocValidator.");
                                moveFileToDirectory(file, DIRECTORY_PATH_INVALIDES);
                            }
                        } else if (json.has("MessageDemandeAutorisation")) {
                            if (autorisationValidator.validateJson(json)) {
                                System.out.println(
                                        "Le JSON " + file.getName() + " est valide selon AutorisationValidator.");
                                String dateFichier = json.getString("dateFichier");
                                if (isMessageExpired(dateFichier)) {
                                    System.err.println("Fichier rejeté : périmé (date : " + dateFichier + ").");
                                } else {
                                    addAutorBDD(json);
                                    moveFileToDirectory(file, DIRECTORY_PATH_AUTOR_VALIDES);
                                }
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

                if (!isMessageValid(messageJson)) {
                    System.err.println("Message rejeté.");
                    continue;
                }

                if (!isAscii(messageJson)) {
                    System.err.println("Message rejeté : contient des caractères non-ASCII.");
                    continue;
                }

                String dateMessage = messageJson.getString("dateMessage");
                if (isMessageExpired(dateMessage)) {
                    System.err.println("Message rejeté : périmé (date : " + dateMessage + ").");
                    continue;
                }

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
            // statut = "acceptORref"; // ce que le msg à été accepté ou réfusé
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

    /*----------------------------------Tests sur les messages----------------------------------*/

    private boolean isChecksumValid(JSONObject json) {
        try {
            int fileChecksum = json.getInt("checksum");

            JSONArray messagesArray = json.getJSONArray("messages");
            int calculatedChecksum = messagesArray.length();

            return fileChecksum == calculatedChecksum;
        } catch (JSONException e) {
            System.err.println("Erreur lors de la vérification du checksum : " + e.getMessage());
            return false;
        }
    }

    private boolean isMessageValid(JSONObject messageJson) {
        final int MAX_DESCRIPTION_LENGTH = 255;
        final int MAX_TITLE_LENGTH = 255;
        final int MAX_MESSAGE_LENGTH = 1000;

        // Vérification de la longueur totale du message
        if (messageJson.toString().length() > MAX_MESSAGE_LENGTH) {
            System.err.println("Message rejeté : dépassement de 1000 caractères.");
            return false;
        }

        // Vérification des objets dans listeObjet
        JSONArray objetsArray = messageJson.getJSONArray("listeObjet");
        for (int i = 0; i < objetsArray.length(); i++) {
            JSONObject objetJson = objetsArray.getJSONObject(i);

            // Vérification de la longueur du titre
            String titre = objetJson.optString("titre", "");
            if (titre.length() > MAX_TITLE_LENGTH) {
                System.err.println("Objet rejeté : titre trop long (>255 caractères). Titre = " + titre);
                return false;
            }

            // Vérification de la longueur de la description
            String description = objetJson.optString("description", "");
            if (description.length() > MAX_DESCRIPTION_LENGTH) {
                System.err.println(
                        "Objet rejeté : description trop longue (>255 caractères). Description = " + description);
                return false;
            }
        }

        return true; // Le message est valide
    }

    private boolean isAscii(JSONObject json) {
        for (String key : json.keySet()) {
            Object value = json.get(key);

            switch (value) {
                case String string -> {
                    // Vérification si la chaîne contient uniquement des caractères ASCII ou des
                    // accents spécifiques
                    if (!string.matches("[\\x00-\\x7Fàâäèéêëìíîïòóôöùúûüÿ]*")) {
                        System.err.println(
                                "Champ non-ASCII ou accent non autorisé détecté : clé=" + key + ", valeur=" + value);
                        return false;
                    }
                }
                case JSONObject jSONObject -> {
                    // Appel récursif pour les objets imbriqués
                    if (!isAscii(jSONObject)) {
                        return false;
                    }
                }
                case JSONArray array -> {
                    for (int i = 0; i < array.length(); i++) {
                        Object arrayElement = array.get(i);
                        switch (arrayElement) {
                            case JSONObject jSONObject -> {
                                if (!isAscii(jSONObject)) {
                                    return false;
                                }
                            }
                            case String string -> {
                                if (!string.matches("[\\x00-\\x7Fàâäèéêëìíîïòóôöùúûüÿ]*")) {
                                    System.err.println(
                                            "Élément non-ASCII ou accent non autorisé détecté dans un tableau : valeur="
                                                    + arrayElement);
                                    return false;
                                }
                            }
                            default -> {
                            }
                        }
                    }
                }
                default -> {
                }
            }
        }
        return true;
    }

    private boolean isMessageExpired(String dateMessage) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate messageDate = LocalDate.parse(dateMessage, formatter);
            LocalDate currentDate = LocalDate.now();
            return messageDate.plusMonths(3).isBefore(currentDate);
        } catch (DateTimeParseException e) {
            System.err.println("Format de date invalide : " + dateMessage);
            return true; // Rejeter si la date est invalide
        }
    }

}
