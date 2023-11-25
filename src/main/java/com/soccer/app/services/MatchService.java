package com.soccer.app.services;

import com.soccer.app.model.MatchModel;

import java.util.List;

public interface MatchService {
    MatchModel addMatch(MatchModel m);

    void deleteMatchById(Long id);
}
