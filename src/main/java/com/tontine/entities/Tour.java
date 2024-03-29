package com.tontine.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@ToString
@NoArgsConstructor
@Entity
public class Tour {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateTour;

    private int nbrTour;

    @OneToOne
    private MembreTontine membreTontine;

    @ManyToOne
    private Tontine tontine;

    public Tour(LocalDate dateTour, int nbrTour, MembreTontine membreTontine, Tontine tontine){
        this.dateTour = dateTour;
        this.nbrTour = nbrTour;
        this.membreTontine = membreTontine;
        this.tontine = tontine;
    }
}
