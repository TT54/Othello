package fr.tt54.othello.bots;

import fr.tt54.othello.bots.utils.Evaluation;
import fr.tt54.othello.bots.utils.MoveEvaluation;
import fr.tt54.othello.data.DataManager;
import fr.tt54.othello.game.OthelloGame;

public class OpeningTestBot extends Bot{

    public OpeningTestBot(boolean white) {
        super(white);
        DataManager.enable();
    }

    @Override
    public boolean playMove(OthelloGame game, long timeLeft) {
        int movesToPlayLeft = (60 - game.getMoveCount()) / 2;
        long timeToPlay = (timeLeft == -1) ? Long.MAX_VALUE : (movesToPlayLeft == 0) ? timeLeft : timeLeft / movesToPlayLeft;

        if(!this.tryOpeningMove(game)) {
            iterativeSearch(game, timeToPlay, 0, 0, Evaluation::patternEval);
        }

        return true;
    }

    @Override
    public Bot copy() {
        return new OpeningTestBot(isWhite());
    }

    private long[] testIterative(OthelloGame startingPosition, int depth, int maxDepth){
        long[] values = new long[maxDepth];

        long time = System.nanoTime();
        MoveEvaluation bestMove = alphaBeta(startingPosition.clone(), depth, Integer.MIN_VALUE, Integer.MAX_VALUE, Evaluation::patternEval);
        values[depth - 1] = System.nanoTime() - time;

        if(depth != maxDepth){
            long[] iterations = testIterative(startingPosition, depth + 1, maxDepth);
            for(int i = 0; i < iterations.length; i++){
                values[i] += iterations[i];
            }
        } else {
            startingPosition.playMove(bestMove.getMoveChain().getPosition());
        }

        return values;
    }
}
