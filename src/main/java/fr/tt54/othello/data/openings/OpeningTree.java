package fr.tt54.othello.data.openings;

import fr.tt54.othello.game.OthelloGame;

import java.util.HashMap;
import java.util.Map;

public class OpeningTree {

    private final OpeningMove firstMove;

    public OpeningTree(byte firstMove){
        this.firstMove = new OpeningMove(firstMove, null);
    }

    public OpeningMove getFirstMove() {
        return firstMove;
    }

    public OpeningMove getMoveAfterSequence(byte[] moves){
        if(moves.length == 0 || moves[0] == -1){
            return firstMove;
        }
        if(moves[0] != firstMove.getMove()){
            return null;
        }
        // On commence à 1 puisque le premier coup est nécessairement f5
        return firstMove.getMoveAfterSequence(moves, 1);
    }

    public OpeningMove insertMoveSequence(byte[] moves, int score){
        // On commence à 1 puisque le premier coup est nécessairement f5
        return firstMove.insertMoveSequence(moves, 1, score);
    }

    public static class OpeningMove{

        private byte move;
        private OpeningMove previousMove;
        private Map<Byte, OpeningMove> nextMoves;

        // Le score est la somme des scores obtenus par les blancs (victoire : +1, nulle : 0, défait : -1)
        private int score = 0;
        private int gamesAmount = 0;

        public OpeningMove(byte move, OpeningMove previousMove) {
            this.move = move;
            this.previousMove = previousMove;
            this.nextMoves = new HashMap<>();
        }

        public byte getMove() {
            return move;
        }

        public OpeningMove getPreviousMove() {
            return previousMove;
        }

        public Map<Byte, OpeningMove> getNextMoves() {
            return nextMoves;
        }

        public void addNextMove(OpeningMove nextMove) {
            if(!this.nextMoves.containsKey(nextMove.getMove()))
                this.nextMoves.put(nextMove.getMove(), nextMove);
        }

        /**
         * Permet d'insérer une séquence de coups depuis l'ouverture ainsi qu'un score associé
         * @param moveSequence la séquence de coups joués depuis le début de la partie
         * @param begin position du coup actuel
         * @param score score attribué à la séquence
         * @return le dernier coup de la séquence
         */
        public OpeningMove insertMoveSequence(byte[] moveSequence, int begin, int score){
            if(begin >= moveSequence.length || moveSequence[begin] < 0) {
                this.addGamePlayed(score);
                return this;
            }
            byte move = moveSequence[begin];

            if(this.nextMoves.containsKey(move)){
                return this.nextMoves.get(move).insertMoveSequence(moveSequence, begin + 1, score);
            }

            OpeningMove nextMove = new OpeningMove(move, this);
            this.addNextMove(nextMove);

            return nextMove.insertMoveSequence(moveSequence, begin + 1, score);
        }

        public OpeningMove getMoveAfterSequence(byte[] moveSequence, int begin){
            if(begin >= moveSequence.length || moveSequence[begin] == -1)
                return this;
            byte move = moveSequence[begin];
            if(this.nextMoves.containsKey(move)){
                return this.nextMoves.get(move).getMoveAfterSequence(moveSequence, begin + 1);
            }
            return null;
        }

        public void addGamePlayed(int gameScore){
            this.score += gameScore;
            this.gamesAmount++;
            if(this.previousMove != null){
                this.previousMove.addGamePlayed(gameScore);
            }
        }

        public int getScore() {
            return score;
        }

        public int getGamesAmount() {
            return gamesAmount;
        }

        public String showNextMoves(){
            StringBuilder builder = new StringBuilder();
            for(OpeningMove nextMove : this.nextMoves.values()){
                builder.append(OthelloGame.intToStringPosition(nextMove.getMove())).append(" : ").append(nextMove.getScore()).append(" [").append(nextMove.gamesAmount).append("]").append("\n");
            }
            return builder.toString();
        }
    }

}
