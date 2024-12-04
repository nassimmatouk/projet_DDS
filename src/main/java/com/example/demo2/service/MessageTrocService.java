package com.example.demo2.service;

import java.util.List;
import java.util.Optional;

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

    // Supprimer un message par son ID
    public boolean supprimerMessageParId(String msgId) {
        try {
            Long id = Long.parseLong(msgId);
            messageTrocRepository.deleteById(id);
            System.out.println("Message supprimé de la base de données : " + msgId);
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression du message : " + e.getMessage());
            return false;
        }
    } 

    public boolean supprimerMessages(String idTroqueur, String idDestinataire, String msgId, String dateMessage) {
        Long messageId = Long.parseLong(msgId);

        Optional<MessageTroc> optionalMessage = messageTrocRepository.findById(messageId);
        
        if (optionalMessage.isPresent()) {
            MessageTroc m = optionalMessage.get();
            List<MessageTroc> messagesToDelete = messageTrocRepository.findByIdTroqueurAndIdDestinataireAndIdFichierAndDateMessage(idTroqueur, idDestinataire, m.getIdFichier(), dateMessage);
            boolean msg_found = false;
            for (MessageTroc message : messagesToDelete) {
                messageTrocRepository.delete(message); 
                msg_found = true;
            }
            return msg_found;
        } else {
            return false;
        }
    }
}