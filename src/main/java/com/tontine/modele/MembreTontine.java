package com.tontine.modele;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Collection;
import java.util.Date;
import java.util.List;


@Data
@AllArgsConstructor
@ToString
@NoArgsConstructor
@Entity
public class MembreTontine {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank
    @Column(nullable = false)
    private Date dateadhesion;

    @OneToOne
    private Utilisateur utilisateur;

    @OneToOne
    private GroupeUtilisateur groupeUtilisateur;

    @OneToOne
    private Tour tour;

    @OneToMany(mappedBy = "membreTontine")
    private List<DemandeJointure> demandeJointures;

}
