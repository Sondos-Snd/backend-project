package com.soccer.app.services;

import com.soccer.app.model.MatchModel;

import java.util.List;

public interface MatchService {
    public List<MatchModel> getAllMatches();

    public MatchModel getMatchById(Long id);

    public void deleteMatchById(Long id);

    public MatchModel addMatch(MatchModel m);

    public MatchModel updateMatch(MatchModel m);
}
