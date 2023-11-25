package com.soccer.app.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name="Matches")
@Getter
@Setter
public class MatchModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String teamOne;

    private String teamTwo;

    private Integer scoreOne;

    private Integer scoreTwo;

}
