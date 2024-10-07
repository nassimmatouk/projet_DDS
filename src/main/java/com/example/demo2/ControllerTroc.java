package com.example.demo2;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControllerTroc {

    @GetMapping("/")
    public String accueil() {
        return "accueil";
    }

    @GetMapping("/demande-troc")
    public String demandeTroc() {
        return "demande_troc";
    }

    @GetMapping("/demande-autorisation")
    public String demandeAutorisation() {
        return "demande_autorisation";
    }

}
