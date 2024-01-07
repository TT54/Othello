package fr.tt54.othello.bots;

import fr.tt54.othello.game.OthelloGame;

import java.util.ArrayList;

public class RandomBot extends Bot{

    public RandomBot(boolean white) {
        super(white);
    }

    @Override
    public boolean playMove(OthelloGame game, long timeLeft) {
        game.playMove(new ArrayList<>(game.getAvailablePlacements()).get(random.nextInt(game.getAvailablePlacements().size())));
        return true;
    }

    @Override
    public Bot copy() {
        return new RandomBot(isWhite());
    }


}
