package fr.tt54.othello.bots.utils;


public class MoveChain {

    private MoveChain previousMove;
    private MoveChain nextMove;
    private final int position;
    private final boolean color;

    public MoveChain(MoveChain previousMove, MoveChain nextMove, int move, boolean color) {
        this.previousMove = previousMove;
        this.nextMove = nextMove;
        this.position = move;
        this.color = color;
    }

    public MoveChain getPreviousMove() {
        return previousMove;
    }

    public MoveChain getNextMove() {
        return nextMove;
    }

    public int getPosition() {
        return position;
    }

    public boolean isColor() {
        return color;
    }

    public MoveChain setPreviousMove(MoveChain previousMove) {
        this.previousMove = previousMove;
        return this;
    }

    public MoveChain setNextMove(MoveChain nextMove) {
        this.nextMove = nextMove;
        return this;
    }
}
