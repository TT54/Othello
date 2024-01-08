package fr.tt54.othello.bots;

import fr.tt54.othello.bots.utils.Evaluation;
import fr.tt54.othello.data.DataManager;
import fr.tt54.othello.data.patterns.Pattern;
import fr.tt54.othello.game.OthelloGame;

import java.util.UUID;

public class AdvancedPatternEvalBot extends Bot{

    private double cornerCoeff = 516.0555568762916d;
    private double borderCoeff = 600;
    private double tableCoeff = 0.31;
    private double freedomCoeff = 0d;

    private final UUID botUUID;

    public AdvancedPatternEvalBot(boolean white) {
        super(white);
        DataManager.enable();
        botUUID = UUID.randomUUID();
    }

    public double getCornerCoeff() {
        return cornerCoeff;
    }

    public double getBorderCoeff() {
        return borderCoeff;
    }

    public double getTableCoeff() {
        return tableCoeff;
    }

    public double getFreedomCoeff() {
        return freedomCoeff;
    }

    public AdvancedPatternEvalBot(boolean white, double cornerCoeff, double borderCoeff, double tableCoeff, double freedomCoeff, UUID botUUID) {
        super(white);
        this.cornerCoeff = cornerCoeff;
        this.borderCoeff = borderCoeff;
        this.tableCoeff = tableCoeff;
        this.freedomCoeff = freedomCoeff;
        this.botUUID = botUUID;
    }

    public UUID getUUID() {
        return botUUID;
    }

    @Override
    public boolean playMove(OthelloGame game, long timeLeft) {
        int movesToPlayLeft = (60 - game.getMoveCount()) / 2;
        long timeToPlay = (timeLeft == -1) ? Long.MAX_VALUE : (movesToPlayLeft == 0) ? timeLeft : timeLeft / movesToPlayLeft;

        if (!this.tryOpeningMove(game)) {
            iterativeSearch(game, timeToPlay, 0, 0, this::advancedEvaluation);
        }

        return true;
    }

    @Override
    public Bot copy() {
        Bot bot = new AdvancedPatternEvalBot(isWhite(), cornerCoeff, borderCoeff, tableCoeff, freedomCoeff, botUUID);
        bot.setBotNumber(this.getBotNumber());
        return bot;
    }

    private double advancedEvaluation(OthelloGame game){
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

        double cornerValue = Pattern.getPatternFromPosition(Pattern.PatternType.CORNER, Pattern.GameStage.MID_GAME, game.getTopLeftPattern(), game.isWhiteToPlay()).getPatternValue();
        cornerValue += Pattern.getPatternFromPosition(Pattern.PatternType.CORNER, Pattern.GameStage.MID_GAME, game.getTopRightPattern(), game.isWhiteToPlay()).getPatternValue();
        cornerValue += Pattern.getPatternFromPosition(Pattern.PatternType.CORNER, Pattern.GameStage.MID_GAME, game.getBottomLeftPattern(), game.isWhiteToPlay()).getPatternValue();
        cornerValue += Pattern.getPatternFromPosition(Pattern.PatternType.CORNER, Pattern.GameStage.MID_GAME, game.getBottomRightPattern(), game.isWhiteToPlay()).getPatternValue();

        double borderValue = Pattern.getPatternFromPosition(Pattern.PatternType.BORDER, Pattern.GameStage.MID_GAME, game.getTopBorderPattern(), game.isWhiteToPlay()).getPatternValue();
        borderValue += Pattern.getPatternFromPosition(Pattern.PatternType.BORDER, Pattern.GameStage.MID_GAME, game.getBottomBorderPattern(), game.isWhiteToPlay()).getPatternValue();
        borderValue += Pattern.getPatternFromPosition(Pattern.PatternType.BORDER, Pattern.GameStage.MID_GAME, game.getLeftBorderPattern(), game.isWhiteToPlay()).getPatternValue();
        borderValue += Pattern.getPatternFromPosition(Pattern.PatternType.BORDER, Pattern.GameStage.MID_GAME, game.getRightBorderPattern(), game.isWhiteToPlay()).getPatternValue();

        //value += this.cornerCoeff * cornerValue + this.borderCoeff * borderValue + this.freedomCoeff * evaluateFreedomDegree(game);
        value += this.cornerCoeff * cornerValue + this.borderCoeff * borderValue;

        if(!game.isWhiteToPlay()){
            return value * -1;
        }

        // On ajoute l'évaluation via le tableau après car elle est déjà du bon signe (négative si avantage noir, positive si avantage blanc)
        value += this.tableCoeff * Evaluation.tableEval(game);

        return value;
    }

    private double evaluateFreedomDegree(OthelloGame game){
        int boardFreedom = -1;

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
