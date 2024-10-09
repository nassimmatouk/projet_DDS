//Ajout de messages et d'objets dans le troc

function isObjetValid(objet) {
    const titre = objet.querySelector('input[name="titre"]').value.trim();
    const qualite = objet.querySelector('input[name="qualite"]').value.trim();
    const quantite = objet.querySelector('input[name="quantite"]').value.trim();

    return titre !== '' && qualite !== '' && quantite !== '';
}

function addMessage() {
    const messagesContainer = document.getElementById("messagesContainer");
    const lastMessage = messagesContainer.lastElementChild;

    const objets = lastMessage.querySelectorAll('.listeObjet');

    for (let objet of objets) {
        if (!isObjetValid(objet)) {
            alert('Veuillez remplir tous les champs de l\'objet avant d\'ajouter un nouveau message.');
            return;
        }
    }

    const newMessage = lastMessage.cloneNode(true);

    newMessage.querySelectorAll("input").forEach(input => {
        input.value = '';
    });

    const objetsContainer = newMessage.querySelector('.objetsContainer');
    const objetsList = objetsContainer.querySelectorAll('.listeObjet');
    
    for (let i = 1; i < objetsList.length; i++) {
        objetsList[i].remove();
    }

    messagesContainer.appendChild(newMessage);
}

function addObjet(button) {
    const objetsContainer = button.parentNode.querySelector(".objetsContainer");
    const lastObjet = objetsContainer.lastElementChild;

    if (!isObjetValid(lastObjet)) {
        alert('Veuillez remplir tous les champs de l\'objet avant d\'ajouter un nouvel objet.');
        return;
    }

    const newObjet = document.querySelector(".listeObjet").cloneNode(true);

    newObjet.querySelectorAll("input").forEach(input => input.value = '');

    objetsContainer.appendChild(newObjet);
}

//Supression d'un objet ou message

function removeObjet(button) {
    const objetsContainer = button.parentNode.parentNode; // Accéder au container des objets
    const objets = objetsContainer.querySelectorAll('.listeObjet');

    if (objets.length > 1) {
        button.parentNode.remove(); 
    } else {
        alert("Vous ne pouvez pas supprimer le dernier objet.");
    }
}

function removeMessage(button) {
    const messagesContainer = document.getElementById('messagesContainer');
    const messages = messagesContainer.querySelectorAll('.message');

    // Ne pas supprimer si c'est le seul message restant
    if (messages.length > 1) {
        button.parentNode.remove(); // Supprimer le parent de ce bouton, donc le message en question
    } else {
        alert("Vous ne pouvez pas supprimer le dernier message.");
    }
}



//Génération du checksum et formatage de la date
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
    const messageElements = messagesContainer.getElementsByClassName('message');

    // Boucle pour chaque message
    for (let messageElement of messageElements) {
        const titre = messageElement.querySelector('input[name="titre"]').value;
        const description = messageElement.querySelector('input[name="description"]').value;

        // Créer un tableau pour stocker les objets de ce message
        const listeObjet = [];

        // Récupération des objets pour ce message
        const objetsContainer = messageElement.querySelector('.objetsContainer');
        const objetElements = objetsContainer.getElementsByClassName('listeObjet');

        for (let objetElement of objetElements) {
            const titre = objetElement.querySelector('input[name="titre"]').value;
            const description = objetElement.querySelector('input[name="description"]').value;
            const qualite = objetElement.querySelector('input[name="qualite"]').value;
            const quantite = objetElement.querySelector('input[name="quantite"]').value;

            // Ajout de l'objet au tableau listeObjet
            listeObjet.push({
                titre: titre,
                description: description,
                qualite: parseInt(qualite), // Assure que c'est un nombre
                quantite: parseInt(quantite) // Assure que c'est un nombre
            });
        }

        // Ajout du message avec la liste des objets
        data.messages.push({
            dateMessage: formatDate(new Date()), // Date du message au format jj-mm-aaaa
            statut: "propose",
            listeObjet: listeObjet // Liste des objets pour ce message
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
