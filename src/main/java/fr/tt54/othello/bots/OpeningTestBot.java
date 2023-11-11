package fr.tt54.othello.bots;

import fr.tt54.othello.data.DataManager;
import fr.tt54.othello.data.openings.OpeningTree;
import fr.tt54.othello.game.OthelloGame;

import java.util.Map;

public class OpeningTestBot extends Bot{

    public OpeningTestBot() {
        super();
        DataManager.enable();
    }

    @Override
    public void playMove(OthelloGame game, long timeLeft) {
        int movesToPlayLeft = (60 - game.getMoveCount()) / 2;
        long timeToPlay = (timeLeft == -1) ? Long.MAX_VALUE : timeLeft / movesToPlayLeft;
        long currentTime = System.currentTimeMillis();

        byte[] playedMoves = game.getPlayedMoves();
        OpeningTree.OpeningMove move = DataManager.mainOpeningTree.getMoveAfterSequence(playedMoves);
        if(move != null){
            Map<Byte, OpeningTree.OpeningMove> nextMoves = move.getNextMoves();
            //TODO
        }
    }
}
