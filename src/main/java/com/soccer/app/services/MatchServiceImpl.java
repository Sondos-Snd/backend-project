package com.soccer.app.services;

import com.soccer.app.model.MatchModel;
import com.soccer.app.repository.MatchRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    @Override
    public void deleteMatchById(Long id) {
        try{
            matchRepository.deleteById(id);
        }catch(Exception e){
            log.error("sthg happned");
        }

    }

    @Override
    public MatchModel addMatch(MatchModel m) {
        return matchRepository.save(m);
    }
}
