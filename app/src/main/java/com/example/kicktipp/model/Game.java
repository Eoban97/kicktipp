package com.example.kicktipp.model;

public class Game {

    private Integer gameId;

    private Integer gameday;
    private String team1;
    private String team2;

    public Game(Integer gameId, Integer gameday, String team1, String team2) {
        this.gameId =gameId;
        this.gameday =gameday;
        this.team1 = team1;
        this.team2 = team2;
    }

    public String getTeam1() {
        return team1;
    }

    public String getTeam2() {
        return team2;
    }

    public Integer getGameday(){
        return gameday;
    }

    public Integer getGameId(){
        return gameId;
    }
}

