/****************************** Ajout messages et objets dans troc ******************************/
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


/****************************** Supression d'un objet ou message ******************************/
function removeObjet(button) {
    const objetsContainer = button.parentNode.parentNode;
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

    if (messages.length > 1) {
        button.parentNode.remove();
    } else {
        alert("Vous ne pouvez pas supprimer le dernier message.");
    }
}


/****************************** Génération checksum et formatage date ******************************/
function generateChecksum(data) {
    return "3";
}

function formatDate(date) {
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();

    return `${day}-${month}-${year}`;
}


/****************************** Création des json ******************************/
//Troc
function sendMessage(event, idMessage) {
    event.preventDefault();
    console.log(idMessage);
    const form = document.getElementById('jsonForm');
    const formData = new FormData(form);
    const data = {
        idTroqueur: "g1.1",
        idDestinataire: formData.get('idDestinataire'),
        idFichier: "g1.1",
        dateFichier: formatDate(new Date()),
        checksum: generateChecksum(formData),
        messages: []
    };

    const messagesContainer = document.getElementById('messagesContainer');
    const messageElements = messagesContainer.getElementsByClassName('message');

    for (let messageElement of messageElements) {
        const titre = messageElement.querySelector('input[name="titre"]').value;
        const description = messageElement.querySelector('input[name="description"]').value;

        const listeObjet = [];

        const objetsContainer = messageElement.querySelector('.objetsContainer');
        const objetElements = objetsContainer.getElementsByClassName('listeObjet');

        for (let objetElement of objetElements) {
            const titre = objetElement.querySelector('input[name="titre"]').value;
            const description = objetElement.querySelector('input[name="description"]').value;
            const qualite = objetElement.querySelector('input[name="qualite"]').value;
            const quantite = objetElement.querySelector('input[name="quantite"]').value;

            listeObjet.push({
                titre: titre,
                description: description,
                qualite: parseInt(qualite),
                quantite: parseInt(quantite)
            });
        }

        data.messages.push({
            dateMessage: formatDate(new Date()),
            statut: "propose",
            listeObjet: listeObjet
        });
    }

    const jsonString = JSON.stringify(data, null, 2);
    saveJsonToFile(jsonString, idMessage);
}

//Autorisation
function handleSubmitA(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const data = {
        idTroqueur: "g1.1",
        idDestinataire: formData.get("idDestinataire"),
        idFichier: "g1.1",
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


/****************************** Enregistrement json dans dossier ******************************/
//Troc
function saveJsonToFile(jsonString, idMessage) {
    const url = idMessage ? `/api/save-troc?idMessage=${idMessage}` : '/api/save-troc';
    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: jsonString
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('Fichier JSON enregistré avec succès.');
                window.location.href = data.redirect;
            } else {
                alert('Erreur lors de l\'enregistrement du fichier : ' + data.message);
            }
        })
        .catch(error => {
            console.error('Erreur:', error);
            alert('Une erreur est survenue lors de la tentative d\'enregistrement du fichier.');
        });
}


//Autorisation
function saveJsonToFileA(jsonString) {
    fetch('/api/save-autor', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: jsonString
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('Fichier JSON enregistré avec succès.');
                window.location.href = data.redirect;
            } else {
                alert('Erreur lors de l\'enregistrement du fichier : ' + data.message);
            }
        })
        .catch(error => {
            console.error('Erreur:', error);
            alert('Une erreur est survenue lors de la tentative d\'enregistrement du fichier.');
        });
}


/****************************** Gestion des brouillons ******************************/
//Sauvegarde en bdd
function saveMessage(event) {

    const form = document.getElementById('jsonForm');
    const formData = new FormData(form);
    const data = {
        idTroqueur: "g1.1",
        idDestinataire: formData.get('idDestinataire'),
        idFichier: "g1.1",
        dateFichier: formatDate(new Date()),
        checksum: generateChecksum(formData),
        messages: []
    };

    const messagesContainer = document.getElementById('messagesContainer');
    const messageElements = messagesContainer.getElementsByClassName('message');

    for (let messageElement of messageElements) {
        const titre = messageElement.querySelector('input[name="titre"]').value;
        const description = messageElement.querySelector('input[name="description"]').value;

        const listeObjet = [];

        const objetsContainer = messageElement.querySelector('.objetsContainer');
        const objetElements = objetsContainer.getElementsByClassName('listeObjet');

        for (let objetElement of objetElements) {
            const titre = objetElement.querySelector('input[name="titre"]').value;
            const description = objetElement.querySelector('input[name="description"]').value;
            const qualite = objetElement.querySelector('input[name="qualite"]').value;
            const quantite = objetElement.querySelector('input[name="quantite"]').value;

            listeObjet.push({
                titre: titre,
                description: description,
                qualite: parseInt(qualite),
                quantite: parseInt(quantite)
            });
        }

        data.messages.push({
            dateMessage: formatDate(new Date()),
            statut: "brouillon",  // Statut "brouillon" pour les messages enregistrés
            listeObjet: listeObjet
        });
    }

    // Envoi des données pour être enregistrées en BDD
    fetch('/api/save-troc-draft', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('Brouillon enregistré avec succès.');
                window.location.href = data.redirect;
            } else {
                alert('Erreur lors de l\'enregistrement du brouillon.');
            }
        })
        .catch(error => {
            console.error('Erreur:', error);
            alert('Une erreur est survenue lors de l\'enregistrement du brouillon.');
        });
}


function checkAll() {
    const checkboxes = document.querySelectorAll('.selectMessage');
    checkboxes.forEach(checkbox => {
        checkbox.checked = checkbox.checked ? false : true;
    });
}


function getSelectedMessageIds() {
    return Array.from(document.querySelectorAll('.selectMessage:checked'))
        .map(checkbox => checkbox.value);
}


document.getElementById('deleteSelectedButton').addEventListener('click', function (event) {
    const selectedMessageIds = getSelectedMessageIds();

    if (selectedMessageIds.length > 0) {
        document.getElementById('selectedMessages').value = selectedMessageIds.join(',');
    } else {
        event.preventDefault();
        alert("Aucun message sélectionné");
    }
});



function sendSingleMessage(messageId) {
    fetch(`/api/get-message-info/${messageId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Préparer les données du message
                const messageData = {
                    idTroqueur: "g1.1",  // Autres données nécessaires
                    idDestinataire: data.message.idDestinataire,
                    idFichier: "g1.1",  // Autres informations nécessaires
                    dateFichier: data.message.dateFichier,
                    checksum: generateChecksum(data.message),
                    messages: []
                };

                var idDest = data.message.idDestinataire;
                var bool = true;
                if (!idDest || idDest.trim() === '') {
                    alert("Le message n'a pas de destinataire valide");
                    bool = false;
                }

                // Récupérer tous les objets du message et les ajouter à la liste
                const listeObjet = [];
                data.message.objets.forEach(objet => {
                    const titre = objet.titre ? objet.titre.trim() : '';
                    const qualite = objet.qualite ? objet.qualite : '';
                    const quantite = objet.quantite ? objet.quantite : '';

                    if (titre === '' || qualite === '' || qualite == 0 || quantite === '' || quantite == 0) {
                        alert("Il manque un des champs suivant à l'objet : Titre, Qualité ou Quantité");
                        bool = false;
                    }

                    listeObjet.push({
                        titre: titre,
                        description: objet.description,
                        qualite: parseInt(qualite),
                        quantite: parseInt(quantite)
                    });
                });

                // Ajouter le message avec la liste des objets dans le tableau de messages
                messageData.messages.push({
                    dateMessage: data.message.dateMessage,
                    statut: "propose",
                    listeObjet: listeObjet
                });

                // Appeler saveJsonToFile avec les données JSON du message
                if (bool == true) {
                    const jsonString = JSON.stringify(messageData, null, 2);
                    saveJsonToFile(jsonString, messageId);
                }
                else {
                    return;
                }
            } else {
                alert('Erreur lors de la récupération du message.');
            }
        })
        .catch(error => {
            console.error('Erreur:', error);
            alert('Une erreur est survenue.');
        });
}




//gestion de plusieurs messages
function sendSelectedMessages() {
    const selectedMessages = getSelectedMessageIds();
}


//Update
function updateMessage(idMessage, dateF) {
    console.log(idMessage);
    const form = document.getElementById('jsonForm');
    const formData = new FormData(form);
    const data = {
        idTroqueur: "g1.1",
        idDestinataire: formData.get('idDestinataire'),
        idFichier: "g1.1",
        dateFichier: dateF,
        checksum: generateChecksum(formData),
        messages: []
    };

    const messagesContainer = document.getElementById('messagesContainer');
    const messageElements = messagesContainer.getElementsByClassName('message');

    for (let messageElement of messageElements) {
        const titre = messageElement.querySelector('input[name="titre"]').value;
        const description = messageElement.querySelector('input[name="description"]').value;

        const listeObjet = [];

        const objetsContainer = messageElement.querySelector('.objetsContainer');
        const objetElements = objetsContainer.getElementsByClassName('listeObjet');

        for (let objetElement of objetElements) {
            const titre = objetElement.querySelector('input[name="titre"]').value;
            const description = objetElement.querySelector('input[name="description"]').value;
            const qualite = objetElement.querySelector('input[name="qualite"]').value;
            const quantite = objetElement.querySelector('input[name="quantite"]').value;

            listeObjet.push({
                titre: titre,
                description: description,
                qualite: parseInt(qualite),
                quantite: parseInt(quantite)
            });
        }

        data.messages.push({
            dateMessage: formatDate(new Date()),
            statut: "brouillon",  // Statut "brouillon" pour les messages enregistrés
            listeObjet: listeObjet
        });
    }

    console.log(data);

    // Requête fetch pour envoyer les données au backend
    fetch(`/api/update/${idMessage}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(responseData => {
            console.log('Message mis à jour :', responseData);
            if (responseData.success) {
                alert("Mise à jour du message réussi");
                window.location.href = responseData.redirectUrl;
            } else {
                alert("Erreur lors de la mise à jour du message.");
            }
        })
        .catch(error => {
            console.error('Erreur lors de la mise à jour du message :', error);
            alert("Erreur lors de la mise à jour du message.");
        });
}
/*************************** Mettre à jour le statut d'un fichier ****************************** */
/*
function updateStatut(idTroqueur, idFichier, idMessage, nouveauStatut) {
    fetch('/api/update-statut', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            idTroqueur: idTroqueur,
            idFichier: idFichier,
            idMessage: idMessage,
            nouveauStatut: nouveauStatut
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert("Statut mis à jour avec succès.");
            location.reload(); // Rafraîchit la page pour afficher le nouveau statut
        } else {
            alert("Échec de la mise à jour du statut.");
        }
    })
    .catch(error => console.error('Erreur:', error));
}*/


