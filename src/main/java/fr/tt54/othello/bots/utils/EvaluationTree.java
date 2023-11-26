package fr.tt54.othello.bots.utils;

import java.util.HashMap;
import java.util.Map;

public class EvaluationTree {

    private MoveEvaluation highestScoreMove = null;
    private MoveEvaluation lowestScoreMove = null;
    private Map<Byte, MoveEvaluation> moveEvaluations = new HashMap<>();
    private Map<Byte, EvaluationTree> nextMoves = new HashMap<>();

    public EvaluationTree() {}

    /**
     * Permet de construire un arbre avec seulement une racine
     * @param move
     */
    public EvaluationTree(MoveEvaluation move){
        this.addMove(move, null);
    }

    public void addMove(MoveEvaluation move, EvaluationTree movesAfter){
        byte movePos = move.getMoveChain() == null ? -1 : (byte) move.getMoveChain().getPosition();
        moveEvaluations.put(movePos, move);
        nextMoves.put(movePos, movesAfter);

        if(highestScoreMove == null || move.getEvaluation() > highestScoreMove.getEvaluation()){
            highestScoreMove = move;
        }
        if(lowestScoreMove == null || move.getEvaluation() < lowestScoreMove.getEvaluation()){
            lowestScoreMove = move;
        }
    }

    public MoveEvaluation getBestMove(boolean whiteToPlay){
        return whiteToPlay ? highestScoreMove : lowestScoreMove;
    }

    public EvaluationTree getMovesAfter(byte move){
        return nextMoves.get(move);
    }

    public MoveEvaluation getMoveEvaluation(byte move){
        return moveEvaluations.get(move);
    }
}
