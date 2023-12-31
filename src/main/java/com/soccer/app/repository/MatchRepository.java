package com.soccer.app.repository;

import com.soccer.app.model.MatchModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends CrudRepository<MatchModel, Long> {

//    @Query("select * from Team where teamOne=:teamName")
//    MatchModel findByTeamName(String teamName);

}
