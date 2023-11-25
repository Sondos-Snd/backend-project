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


    @Autowired
    private MatchRepository matchRepo;

    @Override
    public List<MatchModel> getAllMatches() {
        return matchRepo.findAll();
    }

    @Override
    public MatchModel getMatchById(Long id) {
        Optional<MatchModel> m = matchRepo.findById(id);
        return m.isPresent() ? m.get() : null;
    }

    @Override
    public void deleteMatchById(Long id) {
        try{
            matchRepo.deleteById(id);
        }catch(Exception e){
            log.error("sthg happned");
        }

    }

    @Override
    public MatchModel addMatch(MatchModel m) {
        return matchRepo.save(m);
    }

    @Override
    public MatchModel updateMatch(MatchModel m) {
        return matchRepo.save(m);
    }

}
