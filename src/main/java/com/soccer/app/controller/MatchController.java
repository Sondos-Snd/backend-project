package com.soccer.app.controller;

import com.soccer.app.model.MatchModel;
import com.soccer.app.services.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin("*")
@RequestMapping("api/matches")
@RestController
public class MatchController {

    @Autowired
    private MatchService matchService;


    @PostMapping
//    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<MatchModel> addMatch(@RequestBody MatchModel m) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("titre-example","test de header")
                .body(matchService.addMatch(m));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MatchModel> getAllMatches() {
        return null;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public MatchModel getMatchById(@PathVariable Long id) {
        return null;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public MatchModel updateMatch(@RequestBody MatchModel m) {
        return null;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteMatchById(@PathVariable Long id) {
        matchService.deleteMatchById(id);
    }
}
