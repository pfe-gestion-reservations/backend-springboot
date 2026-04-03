package com.lounes.gestion_reservations.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "employes")
@Data
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entreprise_id", nullable = true)
    private Entreprise entreprise;

    @Column(nullable = false)
    private Boolean archived = false;

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }
    public User getUser()                      { return user; }
    public void setUser(User user)             { this.user = user; }
    public Entreprise getEntreprise()          { return entreprise; }
    public void setEntreprise(Entreprise e)    { this.entreprise = e; }
    public Boolean getArchived()               { return archived; }
    public void setArchived(Boolean archived)  { this.archived = archived; }

    public Employe() {}

    public Employe(Long id, User user, Entreprise entreprise, Boolean archived) {
        this.id = id;
        this.user = user;
        this.entreprise = entreprise;
        this.archived = archived;
    }
}