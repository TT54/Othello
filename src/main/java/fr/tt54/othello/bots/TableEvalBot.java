package fr.tt54.othello.bots;

import fr.tt54.othello.OthelloGame;
import fr.tt54.othello.bots.utils.Evaluation;
import fr.tt54.othello.bots.utils.MoveEvaluation;
import fr.tt54.othello.data.DataManager;

public class TableEvalBot extends Bot {

    public TableEvalBot(boolean white) {
        super(white);
        DataManager.enable();
    }

    @Override
    public boolean playMove(OthelloGame game, long timeLeft) {
        int movesToPlayLeft = (60 - game.getMoveCount()) / 2;
        long timeToPlay = (timeLeft == -1) ? Long.MAX_VALUE : (movesToPlayLeft == 0) ? timeLeft : timeLeft / movesToPlayLeft;

        if (!this.tryOpeningMove(game)) {
            if(this.depthSearch < 0) {
                iterativeSearch(game, timeToPlay, 0, 0, Evaluation::tableEval);
            } else {
                MoveEvaluation result = alphaBeta(game.clone(), this.depthSearch, Integer.MIN_VALUE, Integer.MAX_VALUE, Evaluation::tableEval);
                game.playMove(result.getMoveChain().getPosition());
            }
        }

        return true;
    }

    @Override
    public Bot copy() {
        TableEvalBot copy = new TableEvalBot(isWhite());
        copy.depthSearch = this.depthSearch;
        return copy;
    }
}
