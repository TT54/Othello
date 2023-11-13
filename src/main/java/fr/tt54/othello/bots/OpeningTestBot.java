package fr.tt54.othello.bots;

import fr.tt54.othello.data.DataManager;
import fr.tt54.othello.data.openings.OpeningTree;
import fr.tt54.othello.game.OthelloGame;

import java.util.Map;

public class OpeningTestBot extends Bot{

    public OpeningTestBot(boolean white) {
        super(white);
        DataManager.enable();
    }

    @Override
    public boolean playMove(OthelloGame game, long timeLeft) {
        int movesToPlayLeft = (60 - game.getMoveCount()) / 2;
        long timeToPlay = (timeLeft == -1) ? Long.MAX_VALUE : timeLeft / movesToPlayLeft;
        long currentTime = System.currentTimeMillis();

        byte[] playedMoves = game.getPlayedMoves();
        OpeningTree.OpeningMove move = DataManager.mainOpeningTree.getMoveAfterSequence(playedMoves);
        if(move != null){
            Map<Byte, OpeningTree.OpeningMove> nextMoves = move.getNextMoves();

            if(nextMoves.size() > 0){
                float maxScore = Float.MIN_VALUE;
                OpeningTree.OpeningMove bestMove = null;

                for(byte m : nextMoves.keySet()){
                    OpeningTree.OpeningMove openingMove = nextMoves.get(m);
                    if(openingMove.getScore() > maxScore){
                        bestMove = openingMove;
                        maxScore = openingMove.getScore();
                    }
                }

                if(maxScore > 0){
                    int[] movePos = OthelloGame.intToPosition(bestMove.getMove());
                    game.playMove(movePos[0], movePos[1]);
                    return true;
                }
            }
        }

        // TODO Rechercher un coup
        System.out.println("Il va falloir rechercher un coup");
        return false;
    }
}
