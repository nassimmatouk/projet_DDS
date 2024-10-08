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

    public List<MessageTroc> getAllMessages() {
        return messageTrocRepository.findAll();
    }
}