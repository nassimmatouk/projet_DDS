package com.example.demo2.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo2.model.MessageTroc;
import com.example.demo2.repository.MessageTrocRepository;

@Service
public class MessageTrocService {

    @Autowired
    private MessageTrocRepository messageTrocRepository;

    public MessageTroc saveBrouillon(MessageTroc messageTroc) {
        messageTroc.setBrouillon(true);
        return messageTrocRepository.save(messageTroc);
    }

    public List<MessageTroc> getAllMessagesRecus() {
        return messageTrocRepository.findByBrouillonFalseAndEnvoyerFalse();
    }

    public List<MessageTroc> getAllBrouillons() {
        return messageTrocRepository.findByBrouillonTrue();
    }

    public void deleteBrouillon(Long idMessage) {
        messageTrocRepository.deleteById(idMessage);
    }
}