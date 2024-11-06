package com.example.demo2.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo2.model.Contact;
import com.example.demo2.repository.ContactRepository;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    public List<Contact> getAllMessages() {
        return contactRepository.findAll();
    }

    public Contact ajouterContact(Contact c){
        return contactRepository.save(c);
    }

}
