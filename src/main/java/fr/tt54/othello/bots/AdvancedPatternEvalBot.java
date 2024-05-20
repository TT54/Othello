package fr.tt54.othello.bots;

import fr.tt54.othello.bots.utils.MoveEvaluation;
import fr.tt54.othello.data.DataManager;
import fr.tt54.othello.data.patterns.Pattern;
import fr.tt54.othello.game.OthelloGame;

import java.util.Arrays;

public class AdvancedPatternEvalBot extends Bot{


    public final float[] patternCoeffs;
    public float freedomCoeff;


    public AdvancedPatternEvalBot(boolean white) {
        super(white);
        DataManager.enable();

        patternCoeffs = new float[Pattern.PatternType.values().length];
        Arrays.fill(patternCoeffs, 1f);
        freedomCoeff = 0;
    }

    public AdvancedPatternEvalBot(boolean white, int depthSearch) {
        super(white);
        this.depthSearch = depthSearch;

        patternCoeffs = new float[Pattern.PatternType.values().length];
        Arrays.fill(patternCoeffs, 1f);
        freedomCoeff = 0;
    }

    public AdvancedPatternEvalBot(boolean white, float[] patternCoeffs, float freedomCoeff) {
        super(white);
        this.patternCoeffs = patternCoeffs;
        this.freedomCoeff = freedomCoeff;
    }

    public AdvancedPatternEvalBot(boolean white, int depthSearch, float[] patternCoeffs, float freedomCoeff) {
        super(white);
        this.depthSearch = depthSearch;
        this.patternCoeffs = patternCoeffs;
        this.freedomCoeff = freedomCoeff;
    }

    @Override
    public boolean playMove(OthelloGame game, long timeLeft) {
        int movesToPlayLeft = (60 - game.getMoveCount()) / 2;
        long timeToPlay = (timeLeft == -1) ? Long.MAX_VALUE : (movesToPlayLeft == 0) ? timeLeft : timeLeft / movesToPlayLeft;

        if (!this.tryOpeningMove(game)) {
            if(depthSearch < 0) {
                iterativeSearch(game, timeToPlay, 0, 0, this::advancedEvaluation);
            } else {
                MoveEvaluation result = alphaBeta(game.clone(), depthSearch, Integer.MIN_VALUE, Integer.MAX_VALUE, this::advancedEvaluation);
                game.playMove(result.getMoveChain().getPosition());
            }
        }

        return true;
    }

    @Override
    public Bot copy() {
        Bot bot = new AdvancedPatternEvalBot(isWhite(), this.depthSearch, this.patternCoeffs, this.freedomCoeff);
        bot.setBotNumber(this.getBotNumber());
        return bot;
    }

    public double advancedEvaluation(OthelloGame game){
        if(game.isGameFinished()){
            if(game.getWhitePiecesCount() > game.getBlackPiecesCount()){
                return 500 + game.getWhitePiecesCount() - game.getBlackPiecesCount();
            } else if(game.getWhitePiecesCount() == game.getBlackPiecesCount()) {
                return 0;
            } else {
                return -500 + (game.getWhitePiecesCount() - game.getBlackPiecesCount());
            }
        }

        double value = 0;

        int i = 0;
        for(Pattern.PatternType patternType : Pattern.PatternType.values()){
            for(int[][] patternLocation : patternType.getPatternsLocations()){
                value += patternCoeffs[i] * Pattern.getPatternFromPosition(patternType, game.getMoveCount() > 60 - 24 ? Pattern.GameStage.ENDGAME : (game.getMoveCount() > 10 ? Pattern.GameStage.MID_GAME : Pattern.GameStage.OPENING), game.getPattern(patternLocation), game.isWhiteToPlay()).getPatternValue();
            }
            i++;
        }

        if(!game.isWhiteToPlay()){
            value = value * -1;
        }

        // On ajoute l'évaluation via le tableau après car elle est déjà du bon signe (négative si avantage noir, positive si avantage blanc)
        //value += this.tableCoeff * Evaluation.tableEval(game);
        if(freedomCoeff != 0) {
            value += freedomCoeff * evaluateFreedomDegree(game);
        }

        return value;
    }

    private double evaluateFreedomDegree(OthelloGame game){
        int boardFreedom = 0;

        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                int currentPiece = game.getPiece(i, j);
                if(currentPiece != 0 && (currentPiece > 0) == game.isWhiteToPlay()) {
                    for(int k = -1; k <= 1; k++){
                        for(int l = -1; l <= 1; l++){
                            boardFreedom += Math.abs(game.getPiece(i + k, j + l));
                        }
                    }
                }
            }
        }

        return boardFreedom;
    }

}
