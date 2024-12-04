package com.example.demo2.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo2.model.Contact;
import com.example.demo2.model.MessageTroc;
import com.example.demo2.repository.MessageTrocRepository;
import com.example.demo2.service.ContactService;

@Controller
public class ControllerTroc {

    @Autowired 
    private ContactService contactService;

    @Autowired
    private MessageTrocRepository messageTrocRepository;

    @GetMapping("/")
    public String accueil() {
        return "accueil";
    }

    // troc

    @GetMapping("/troc")
    public String troc() {
        return "troc";
    }

    // autorisation
    @GetMapping("/autorisation")
    public String showContact(Model model) {
        List<Contact> contacts = contactService.getAllMessages();

        if (contacts.isEmpty()) {
            model.addAttribute("AcontactInfo", "Aucun contact disponible.");
        }

        model.addAttribute("Acontact", contacts);
        return "autorisation";
    }

    @GetMapping("/demande-autorisation")
    public String demandeAutorisation() {
        return "demande_autorisation";
    }

    @GetMapping("/edit")
    public String edit(@RequestParam(value = "idMessage", required = false) Long idMessage,
            @RequestParam(value = "idDestinataire", required = false) String idDestinataire, Model model) {
        if (idMessage != null) {
            Optional<MessageTroc> messageTroc = messageTrocRepository.findById(idMessage);
            if (messageTroc.isPresent()) {
                model.addAttribute("message", messageTroc.get());
            } else {
                model.addAttribute("error", "Message de troc non trouvé");
            }
            List<Contact> contacts = contactService.getAllMessages();
            model.addAttribute("contacts", contacts);
            return "/edit";
        } else {
            List<Contact> contacts = contactService.getAllMessages();
            model.addAttribute("contacts", contacts);

            if (idDestinataire != null) {
                model.addAttribute("idDestinataire", idDestinataire);
            }
            return "/demande_troc";
        }
    }
}
