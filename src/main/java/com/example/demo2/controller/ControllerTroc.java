package com.example.demo2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControllerTroc {

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

    @GetMapping("/autorisation")
    public String demande() {
        return "autorisation";
    }

    @GetMapping("/demande-autorisation")
    public String demandeAutorisation() {
        return "demande_autorisation";
    }
}
