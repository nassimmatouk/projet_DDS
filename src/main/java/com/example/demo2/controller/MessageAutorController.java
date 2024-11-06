package com.example.demo2.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo2.model.MessageAutor;
import com.example.demo2.model.Contact;
import com.example.demo2.service.ContactService;
import com.example.demo2.service.MessageAutorService;

@Controller
public class MessageAutorController {

    @Autowired
    private MessageAutorService messageAutorService;

    @Autowired
    private ContactService contactService;

    @GetMapping("/message-autorisation")
    public String showMessages(Model model) {
        List<MessageAutor> messages = messageAutorService.getAllMessages();

        if (messages.isEmpty()) {
            model.addAttribute("AmessageInfo", "Aucun message d'autorisation disponible.");
        }

        model.addAttribute("Amessage", messages);
        return "message_autorisation";
    }





    @PostMapping("/accepter-message")
    @ResponseBody
    public String accepterMessage(@RequestParam Long id) { 
        MessageAutor m = messageAutorService.getMessageById(id); System.out.println("\n\nIN ACCEPT MSG .........\n\n");
        Contact c = new Contact(m.getId(), m.getNomAuteur(), m.getMail(), m.getTelephone(), m.getDate());
        contactService.ajouterContact(c);  // Ajoute le message dans la liste de contacts
        messageAutorService.supprimerMessage(id);  // Supprime le message de la base
        return "Message accepté et ajouté aux contacts";
    }

    @PostMapping("/refuser-message")
    @ResponseBody
    public String refuserMessage(@RequestParam Long id) { System.out.println("\n\nIN REFUSE MSG ....\n\n");
        messageAutorService.supprimerMessage(id);  // Supprime le message de la base
        return "Message refusé et supprimé";
    }
}
