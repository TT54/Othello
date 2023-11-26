package fr.tt54.othello.bots.utils;

public class MoveEvaluation {

    private final int evaluation;
    private final MoveChain moveChain;
    private final boolean finalEvaluation;

    public MoveEvaluation(int evaluation, MoveChain moveChain, boolean finalEvaluation) {
        this.evaluation = evaluation;
        this.moveChain = moveChain;
        this.finalEvaluation = finalEvaluation;
    }

    public int getEvaluation() {
        return evaluation;
    }

    public MoveChain getMoveChain() {
        return moveChain;
    }

    public boolean isFinalEvaluation() {
        return finalEvaluation;
    }
}
