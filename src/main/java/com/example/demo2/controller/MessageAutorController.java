package com.example.demo2.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo2.model.MessageAutor;
import com.example.demo2.service.MessageAutorService;

@Controller
public class MessageAutorController {

    @Autowired
    private MessageAutorService messageAutorService;

    @GetMapping("/message-autorisation")
    public String showMessages(Model model) {
        List<MessageAutor> messages = messageAutorService.getAllMessages();

        if (messages.isEmpty()) {
            model.addAttribute("AmessageInfo", "Aucun message d'autorisation disponible.");
        }

        model.addAttribute("Amessage", messages);
        return "message_autorisation";
    }
}
