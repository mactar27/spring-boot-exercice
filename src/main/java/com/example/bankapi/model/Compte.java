package com.example.bankapi.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Compte {
    @Id
    private Long id;
    private String numero;
    private Double solde;

    @ManyToOne
    @JsonBackReference  // ← empêche Jackson de boucler sur client
    private Client client;

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public Double getSolde() { return solde; }
    public void setSolde(Double solde) { this.solde = solde; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
}
