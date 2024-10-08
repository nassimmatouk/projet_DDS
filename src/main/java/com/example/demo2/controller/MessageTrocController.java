package com.example.demo2.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo2.model.MessageTroc;
import com.example.demo2.service.MessageTrocService;

@Controller
public class MessageTrocController {

    @Autowired
    private MessageTrocService messageTrocService;

    @GetMapping("/message-troc")
    public String showMessages(Model model) {
        List<MessageTroc> messages = messageTrocService.getAllMessages();

        if (messages.isEmpty()) {
            model.addAttribute("messageInfo", "Aucun message de troc disponible.");
        }

        model.addAttribute("messages", messages);
        return "message_troc";
    }
}
