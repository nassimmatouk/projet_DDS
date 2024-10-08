function addMessage() {
    const messagesContainer = document.getElementById("messagesContainer");
    const newMessage = document.querySelector(".message").cloneNode(true);

    newMessage.querySelector("input[name='titre']").value = '';
    newMessage.querySelector("input[name='description']").value = '';
    newMessage.querySelector("input[name='qualite']").value = '';
    newMessage.querySelector("input[name='quantite']").value = '';

    messagesContainer.appendChild(newMessage);
}

function generateChecksum(data) {
    // Logique de génération du checksum ici
    return "3";
}

function formatDate(date) {
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();

    return `${day}-${month}-${year}`;
}


//Création du json de troc
function handleSubmit(event) {
    event.preventDefault();

    // Récupération des données du formulaire
    const form = document.getElementById('jsonForm');
    const formData = new FormData(form);
    const data = {
        idTroqueur: "g1.1",
        idDestinataire: formData.get('idDestinataire'),
        idFichier: formData.get('idFichier'),
        dateFichier: formatDate(new Date()),
        checksum: generateChecksum(formData),
        messages: []
    };

    // Récupération des messages
    const messagesContainer = document.getElementById('messagesContainer');
    const messages = messagesContainer.getElementsByClassName('message');

    for (let message of messages) {
        const titre = message.querySelector('input[name="titre"]').value;
        const description = message.querySelector('input[name="description"]').value;
        const qualite = message.querySelector('input[name="qualite"]').value;
        const quantite = message.querySelector('input[name="quantite"]').value;

        // Ajout du message dans le format attendu
        data.messages.push({
            dateMessage: formatDate(new Date()), // Date du message au format jj-mm-aaaa
            statut: "propose",
            listeObjet: [
                {
                    titre: titre,
                    description: description,
                    qualite: parseInt(qualite), // Assure que c'est un nombre
                    quantite: parseInt(quantite) // Assure que c'est un nombre
                }
            ]
        });
    }

    // Conversion en JSON et sauvegarde
    const jsonString = JSON.stringify(data, null, 2);
    saveJsonToFile(jsonString);
}


//Création du json d'autorisation
function handleSubmitA(event) {
    event.preventDefault(); // Empêche la soumission du formulaire

    const formData = new FormData(event.target);
    const data = {
        idTroqueur: "g1.1",
        idDestinataire: formData.get("idDestinataire"),
        idFichier: formData.get("idFichier"),
        dateFichier: formatDate(new Date()),
        checksum: generateChecksum(formData),
        MessageDemandeAutorisation: {
            statutAutorisation: "demande",
            date: formatDate(new Date()),
            idMessage: "msg_" + Date.now(),
            coordonnees: {}
        }
    };
    
    const email = formData.get("email");
    const telephone = formData.get("telephone");
    const nomAuteur = formData.get("nomAuteur");

    if (email) {
        data.MessageDemandeAutorisation.coordonnees.mail = email;
    }
    if (telephone) {
        data.MessageDemandeAutorisation.coordonnees.telephone = telephone;
    }
    if (nomAuteur) {
        data.MessageDemandeAutorisation.coordonnees.nomAuteur = nomAuteur;
    }
    const jsonString = JSON.stringify(data, null, 2);
    saveJsonToFileA(jsonString);
}


//Enregistrement du json dans le dossier

//le troc
function saveJsonToFile(jsonString) {
    fetch('/api/save-troc', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: jsonString
    })
        .then(response => response.json())  // Convertir la réponse en JSON
        .then(data => {
            if (data.success) {
                alert('Fichier JSON enregistré avec succès.');
                window.location.href = data.redirect;  // Rediriger vers l'URL fournie
            } else {
                alert('Erreur lors de l\'enregistrement du fichier : ' + data.message);
            }
        })
        .catch(error => {
            console.error('Erreur:', error);
            alert('Une erreur est survenue lors de la tentative d\'enregistrement du fichier.');
        });
}


//l'autorisation
function saveJsonToFileA(jsonString) {
    fetch('/api/save-autor', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: jsonString
    })
        .then(response => response.json())  // Convertir la réponse en JSON
        .then(data => {
            if (data.success) {
                alert('Fichier JSON enregistré avec succès.');
                window.location.href = data.redirect;  // Rediriger vers l'URL fournie
            } else {
                alert('Erreur lors de l\'enregistrement du fichier : ' + data.message);
            }
        })
        .catch(error => {
            console.error('Erreur:', error);
            alert('Une erreur est survenue lors de la tentative d\'enregistrement du fichier.');
        });
}
