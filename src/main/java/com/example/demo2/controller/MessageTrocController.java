package com.example.demo2.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo2.model.MessageTroc;
import com.example.demo2.service.MessageTrocService;
import com.example.demo2.repository.MessageTrocRepository;

@Controller
public class MessageTrocController {

    @Autowired
    private MessageTrocService messageTrocService;

    @Autowired
    private MessageTrocRepository messageTrocRepository;

    @GetMapping("/message-troc")
    public String showMessages(Model model) {
        List<MessageTroc> messages = messageTrocService.getAllMessagesRecus();
        List<MessageTroc> brouillons = messageTrocService.getAllBrouillons();

        if (messages.isEmpty()) {
            model.addAttribute("messageInfo", "Aucun message de troc disponible.");
        }
        if (brouillons.isEmpty()) {
            model.addAttribute("brouillonInfo", "Aucun brouillon disponible.");
        }

        model.addAttribute("messages", messages);
        model.addAttribute("brouillons", brouillons);
        return "message_troc";
    }



    @GetMapping("/reponse")
    public String reponse(@RequestParam(value = "idMessage", required = false) Long idMessage,
            @RequestParam(value = "idDestinataire", required = false) String idDestinataire, Model model) {
        /*if (idMessage != null) {
            Optional<MessageTroc> messageTroc = messageTrocRepository.findById(idMessage);
            if (messageTroc.isPresent()) {
                model.addAttribute("message", messageTroc.get());
            } else {
                model.addAttribute("error", "Message de troc non trouvé");
            }
            return "/edit";
        } else if (idDestinataire != null) {
            model.addAttribute("idDestinataire", idDestinataire);
            return "/demande_troc";
        } else {
            return "/demande_troc";
        }*/

        
        if (idMessage != null) {
            Optional<MessageTroc> messageTroc = messageTrocRepository.findById(idMessage);
            if (messageTroc.isPresent()) {
                model.addAttribute("message", messageTroc.get());
            } else {
                model.addAttribute("error", "Message de troc non trouvé");
            }
            return "/reponse";
        }
        else {
            return "message-troc"; 
        }
    }

    
}
