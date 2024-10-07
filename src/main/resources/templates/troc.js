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
    return "checksum_genere";
}

document.getElementById("jsonForm").onsubmit = function (event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const json = {
        idTroqueur: "g1.1",
        idDestinataire: formData.get("idDestinataire"),
        idFichier: formData.get("idFichier"),
        dateFichier: new Date().toLocaleDateString("fr-FR"),
        checksum: generateChecksum(formData),
        messages: []
    };

    const messages = document.querySelectorAll(".message");
    messages.forEach((message) => {
        const messageData = {
            dateMessage: new Date().toLocaleDateString("fr-FR"),
            statut: "propose",
            listeObjet: []
        };

        const objets = message.querySelectorAll(".listeObjet");
        objets.forEach((objet) => {
            const objetData = {
                titre: objet.querySelector("input[name='titre']").value,
                description: objet.querySelector("input[name='description']").value || "",
                qualite: parseInt(objet.querySelector("input[name='qualite']").value),
                quantite: parseInt(objet.querySelector("input[name='quantite']").value)
            };
            messageData.listeObjet.push(objetData);
        });

        json.messages.push(messageData);
    });

    console.log(JSON.stringify(json, null, 2));
};