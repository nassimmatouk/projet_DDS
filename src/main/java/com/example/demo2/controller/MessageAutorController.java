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


    @PostMapping("/supprimer-contact")
    @ResponseBody
    public String supprimerContact(@RequestParam Long id) { 
        contactService.supprimerContact(id);
        System.out.println("Le contact ayant l'id suivant à été supprimer avec succès : " + id);
        return "Contact supprimer avec succès";
    }

}
