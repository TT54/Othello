package fr.tt54.othello.data.objects;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Pattern {

    private static final Map<Integer, Pattern> openingPatterns = new HashMap<>();
    private static final Map<Integer, Pattern> midGamePatterns = new HashMap<>();
    private static final Map<Integer, Pattern> endGamePatterns = new HashMap<>();

    private int[] pawns;
    private int played;
    private int won;

    public Pattern(int[] pawns, int played, int won) {
        this.pawns = pawns;
        this.played = played;
        this.won = won;
    }

    /**
     *
     * @param pawns La liste des pions avec noirs = -1 ; rien = 0 ; blancs = 1
     */
    public Pattern(int[] pawns, boolean whiteToPlay) {
        for (int i = 0; i < pawns.length; i++) {
            if(!whiteToPlay){
                pawns[i] *= -1;
            }
            pawns[i]++;
        }
        this.pawns = pawns;
    }

    public void addPositionPlayed(boolean won){
        this.played++;
        if(won)
            this.won++;
    }

    public void setPlayed(int played) {
        this.played = played;
    }

    public void setWon(int won) {
        this.won = won;
    }

    public void addPlayed(int played){
        this.played += played;
    }

    public void addWon(int won){
        this.won += won;
    }

    public int[] getPawns() {
        return pawns;
    }

    public int getPlayed() {
        return played;
    }

    public int getWon() {
        return won;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pattern pattern = (Pattern) o;
        return Arrays.equals(pawns, pattern.pawns);
    }

    @Override
    public int hashCode() {
        int code = 0;
        for(int i = 0; i < pawns.length; i++){
            code += Math.pow(3, i) * pawns[i];
        }
        return code;
    }

    /**
     *
     * @param stage Moment de la partie
     * @param pattern Pattern sous forme de liste (Noirs = -1 ; Rien = 0 ; Blancs = 1)
     * @param whiteToPlay
     * @return Le pattern stocké
     */
    public static Pattern getPatternFromPosition(GameStage stage, int[] pattern, boolean whiteToPlay){
        Pattern p = new Pattern(pattern, whiteToPlay);
        if(stage == GameStage.OPENING){
            return openingPatterns.getOrDefault(p.hashCode(), p);
        } else if(stage == GameStage.MID_GAME){
            return midGamePatterns.getOrDefault(p.hashCode(), p);
        } else {
            return endGamePatterns.getOrDefault(p.hashCode(), p);
        }
    }

    public static Pattern addPatternOccurrence(GameStage stage, int[] pattern, boolean whiteToPlay, boolean winningPosition){
        Pattern p = new Pattern(pattern, whiteToPlay);
        if(stage == GameStage.OPENING){
            Pattern p2 = openingPatterns.getOrDefault(p.hashCode(), p);
            p2.addPositionPlayed(winningPosition);
            openingPatterns.put(p2.hashCode(), p2);
            return p2;
        } else if(stage == GameStage.MID_GAME){
            Pattern p2 = midGamePatterns.getOrDefault(p.hashCode(), p);
            p2.addPositionPlayed(winningPosition);
            midGamePatterns.put(p2.hashCode(), p2);
            return p2;
        } else {
            Pattern p2 = endGamePatterns.getOrDefault(p.hashCode(), p);
            p2.addPositionPlayed(winningPosition);
            endGamePatterns.put(p2.hashCode(), p2);
            return p2;
        }
    }

    public static Pattern setPatternOccurrences(GameStage stage, int[] pattern, boolean whiteToPlay, int wonPositions, int playedPositions){
        Pattern p = new Pattern(pattern, whiteToPlay);
        if(stage == GameStage.OPENING){
            Pattern p2 = openingPatterns.getOrDefault(p.hashCode(), p);
            p2.setPlayed(playedPositions);
            p2.setWon(wonPositions);
            openingPatterns.put(p2.hashCode(), p2);
            return p2;
        } else if(stage == GameStage.MID_GAME){
            Pattern p2 = midGamePatterns.getOrDefault(p.hashCode(), p);
            p2.setPlayed(playedPositions);
            p2.setWon(wonPositions);
            midGamePatterns.put(p2.hashCode(), p2);
            return p2;
        } else {
            Pattern p2 = endGamePatterns.getOrDefault(p.hashCode(), p);
            p2.setPlayed(playedPositions);
            p2.setWon(wonPositions);
            endGamePatterns.put(p2.hashCode(), p2);
            return p2;
        }
    }

    public static Pattern addPatternOccurrences(GameStage stage, int[] pattern, boolean whiteToPlay, int wonPositions, int playedPositions){
        Pattern p = new Pattern(pattern, whiteToPlay);
        if(stage == GameStage.OPENING){
            Pattern p2 = openingPatterns.getOrDefault(p.hashCode(), p);
            p2.addPlayed(playedPositions);
            p2.addWon(wonPositions);
            openingPatterns.put(p2.hashCode(), p2);
            return p2;
        } else if(stage == GameStage.MID_GAME){
            Pattern p2 = midGamePatterns.getOrDefault(p.hashCode(), p);
            p2.addPlayed(playedPositions);
            p2.addWon(wonPositions);
            midGamePatterns.put(p2.hashCode(), p2);
            return p2;
        } else {
            Pattern p2 = endGamePatterns.getOrDefault(p.hashCode(), p);
            p2.addPlayed(playedPositions);
            p2.addWon(wonPositions);
            endGamePatterns.put(p2.hashCode(), p2);
            return p2;
        }
    }


    public enum GameStage{
        OPENING,
        MID_GAME,
        ENDGAME;
    }
}
