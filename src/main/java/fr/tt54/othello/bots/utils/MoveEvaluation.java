package fr.tt54.othello.bots.utils;

public class MoveEvaluation {

    private final int evaluation;
    private final MoveChain moveChain;

    public MoveEvaluation(int evaluation, MoveChain moveChain) {
        this.evaluation = evaluation;
        this.moveChain = moveChain;
    }

    public int getEvaluation() {
        return evaluation;
    }

    public MoveChain getMoveChain() {
        return moveChain;
    }
}
