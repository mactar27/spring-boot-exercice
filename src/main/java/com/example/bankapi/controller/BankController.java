package com.example.bankapi.controller;

import com.example.bankapi.model.Client;
import com.example.bankapi.model.Compte;
import com.example.bankapi.repository.ClientRepository;
import com.example.bankapi.repository.CompteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping
public class BankController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CompteRepository compteRepository;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/clients")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Client>> getClients() {
        List<Client> clients = clientRepository.findAll();
        return ResponseEntity.ok(clients);
    }

    @PostMapping("/clients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        Client savedClient = clientRepository.save(client);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedClient);
    }

    @GetMapping("/clients/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getClientById(@PathVariable Long id) {
        Optional<Client> client = clientRepository.findById(id);
        if (client.isPresent()) {
            return ResponseEntity.ok(client.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Client non trouvé");
        }
    }

    @PutMapping("/clients/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateClient(@PathVariable Long id, @RequestBody Client clientDetails) {
        Optional<Client> client = clientRepository.findById(id);
        if (client.isPresent()) {
            Client existingClient = client.get();
            existingClient.setNom(clientDetails.getNom());
            existingClient.setPrenom(clientDetails.getPrenom());
            existingClient.setEmail(clientDetails.getEmail());
            Client updatedClient = clientRepository.save(existingClient);
            return ResponseEntity.ok(updatedClient);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Client non trouvé");
        }
    }

    @DeleteMapping("/clients/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteClient(@PathVariable Long id) {
        Optional<Client> client = clientRepository.findById(id);
        if (client.isPresent()) {
            clientRepository.delete(client.get());
            return ResponseEntity.ok("Client supprimé");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Client non trouvé");
        }
    }

    @GetMapping("/solde")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getSolde(@RequestParam Long compteId) {
        Optional<Compte> compte = compteRepository.findById(compteId);
        if (compte.isPresent()) {
            return ResponseEntity.ok(compte.get().getSolde());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Compte non trouvé");
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCompte(@RequestBody Compte compte) {
        if (compte.getClient() == null || compte.getClient().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Client requis");
        }
        Optional<Client> client = clientRepository.findById(compte.getClient().getId());
        if (!client.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Client non trouvé");
        }
        compte.setClient(client.get());
        Compte savedCompte = compteRepository.save(compte);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCompte);
    }

    @GetMapping("/comptes")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Compte>> getComptes() {
        List<Compte> comptes = compteRepository.findAll();
        return ResponseEntity.ok(comptes);
    }

    @GetMapping("/comptes/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getCompteById(@PathVariable Long id) {
        Optional<Compte> compte = compteRepository.findById(id);
        if (compte.isPresent()) {
            return ResponseEntity.ok(compte.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Compte non trouvé");
        }
    }

    @PutMapping("/comptes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCompte(@PathVariable Long id, @RequestBody Compte compteDetails) {
        Optional<Compte> compte = compteRepository.findById(id);
        if (compte.isPresent()) {
            Compte existingCompte = compte.get();
            existingCompte.setNumero(compteDetails.getNumero());
            existingCompte.setSolde(compteDetails.getSolde());
            if (compteDetails.getClient() != null && compteDetails.getClient().getId() != null) {
                Optional<Client> client = clientRepository.findById(compteDetails.getClient().getId());
                if (client.isPresent()) {
                    existingCompte.setClient(client.get());
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Client non trouvé");
                }
            }
            Compte updatedCompte = compteRepository.save(existingCompte);
            return ResponseEntity.ok(updatedCompte);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Compte non trouvé");
        }
    }

    @DeleteMapping("/comptes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCompte(@PathVariable Long id) {
        Optional<Compte> compte = compteRepository.findById(id);
        if (compte.isPresent()) {
            compteRepository.delete(compte.get());
            return ResponseEntity.ok("Compte supprimé");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Compte non trouvé");
        }
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> transfer(@RequestParam Long fromCompteId, @RequestParam Long toCompteId, @RequestParam Double amount) {
        Optional<Compte> fromCompte = compteRepository.findById(fromCompteId);
        Optional<Compte> toCompte = compteRepository.findById(toCompteId);

        if (!fromCompte.isPresent() || !toCompte.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Compte source ou destination non trouvé");
        }

        if (fromCompte.get().getSolde() < amount) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Solde insuffisant");
        }

        fromCompte.get().setSolde(fromCompte.get().getSolde() - amount);
        toCompte.get().setSolde(toCompte.get().getSolde() + amount);

        compteRepository.save(fromCompte.get());
        compteRepository.save(toCompte.get());

        return ResponseEntity.ok("Transfert réussi");
    }
}