package fr.tt54.othello.bots;

import fr.tt54.othello.bots.utils.Evaluation;
import fr.tt54.othello.data.DataManager;
import fr.tt54.othello.game.OthelloGame;

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
            iterativeSearch(game, timeToPlay, 0, 0, Evaluation::tableEval);
        }

        return true;
    }

    @Override
    public Bot copy() {
        return new TableEvalBot(isWhite());
    }
}
