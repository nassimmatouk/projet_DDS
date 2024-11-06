package com.example.demo2.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo2.model.MessageAutor;
import com.example.demo2.repository.ContactRepository;
import com.example.demo2.service.ContactService;
import com.example.demo2.service.MessageAutorService;
import com.example.demo2.model.Contact;

@Controller
public class ControllerTroc {

    @Autowired
    private ContactService contactService;

    @GetMapping("/")
    public String accueil() {
        return "accueil";
    }

    //troc

    @GetMapping("/troc")
    public String troc() {
        return "troc";
    }

    @GetMapping("/demande-troc")
    public String demandeTroc() {
        return "demande_troc";
    }

    //autorisation
    /*
    @GetMapping("/autorisation")
    public String demande() {
        return "autorisation";
    }*/
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
}
