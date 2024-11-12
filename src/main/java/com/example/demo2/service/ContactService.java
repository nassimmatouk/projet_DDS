package com.example.demo2.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo2.model.Contact;
import com.example.demo2.model.MessageAutor;
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

    public void supprimerContact(Long id) {
        contactRepository.deleteById(id);
    }

    public boolean estDansContacts(MessageAutor m){
        // Vérification de l'existence dans la base de données
        Optional<Contact> existingAutor = contactRepository.findByNomAuteurAndMailAndTelephoneAndDate(m.getNomAuteur(), m.getMail(), m.getTelephone(), m.getDate());
        
        if (existingAutor.isPresent()) {
            System.out.println("\nAutorisation déjà présente dans la base de données : " + existingAutor.get());
            return true; 
        }
        return false;
    }
}
