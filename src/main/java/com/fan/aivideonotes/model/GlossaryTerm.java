package com.fan.aivideonotes.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class GlossaryTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    private String term;

    @Column(length = 1000)
    private String shortExplanation;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String longExplanation;
}
