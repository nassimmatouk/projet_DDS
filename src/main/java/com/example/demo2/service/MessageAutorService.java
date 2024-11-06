package com.example.demo2.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo2.model.MessageAutor;
import com.example.demo2.repository.MessageAutorRepository;

@Service
public class MessageAutorService {

    @Autowired
    private MessageAutorRepository messageAutorRepository;

    public List<MessageAutor> getAllMessages() {
        return messageAutorRepository.findAll();
    }
    
    public MessageAutor getMessageById(Long id) {
        return messageAutorRepository.findById(id).orElse(null);
    }

    public void supprimerMessage(Long id) {
        messageAutorRepository.deleteById(id);
    }
}
