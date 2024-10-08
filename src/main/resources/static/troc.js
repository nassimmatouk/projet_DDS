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


//Création du json de troc
function handleSubmit(event) {
    event.preventDefault(); // Empêche la soumission du formulaire

    // Récupération des données du formulaire
    const form = document.getElementById('jsonForm');
    const formData = new FormData(form);
    const data = {
        idTroqueur: "g1.1",
        idDestinataire: formData.get('idDestinataire'),
        idFichier: formData.get('idFichier'),
        dateFichier: new Date().toLocaleDateString("fr-FR"),
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

        data.messages.push({
            titre: titre,
            description: description,
            qualite: qualite,
            quantite: quantite
        });
    }

    // Création du fichier JSON
    const jsonString = JSON.stringify(data, null, 2);

    // Enregistrement du fichier JSON
    saveJsonToFile(jsonString);
}

/*
//Création du json d'autorisation
document.getElementById("autor").onsubmit = function (event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const json = {
        idTroqueur: "g1.1",
        idDestinataire: formData.get("idDestinataire"),
        idFichier: formData.get("idFichier"),
        dateFichier: new Date().toLocaleDateString("fr-FR"),
        checksum: generateChecksum(formData),
        statut: "demande",
    };
}
    */

//Enregistrement du json dans le dossier
function saveJsonToFile(jsonString) {
    fetch('/api/save-json', {
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
