package com.lounes.gestion_reservations.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String numtel;
    private String createdBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "client_entreprise",
            joinColumns = @JoinColumn(name = "client_id"),
            inverseJoinColumns = @JoinColumn(name = "entreprise_id")
    )
    private Set<Entreprise> entreprises = new HashSet<>();

    @Column(nullable = false)
    private Boolean archived = false;

    // ── Constructeur complet mis à jour ──
    public Client(Long id, User user, String numtel, String createdBy,
                  Set<Entreprise> entreprises, Boolean archived) {
        this.id = id;
        this.user = user;
        this.numtel = numtel;
        this.createdBy = createdBy;
        this.entreprises = entreprises != null ? entreprises : new HashSet<>();
        this.archived = archived;
    }

    public Client() {}

    // ── Getters & Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getNumtel() { return numtel; }
    public void setNumtel(String numtel) { this.numtel = numtel; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Boolean getArchived() { return archived; }
    public void setArchived(Boolean archived) { this.archived = archived; }

    public Set<Entreprise> getEntreprises() { return entreprises; }
    public void setEntreprises(Set<Entreprise> entreprises) { this.entreprises = entreprises; }
    public void addEntreprise(Entreprise e) { this.entreprises.add(e); }
    public void removeEntreprise(Entreprise e) { this.entreprises.remove(e); }
}