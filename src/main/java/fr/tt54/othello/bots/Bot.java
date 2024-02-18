package fr.tt54.othello.bots;

import fr.tt54.othello.Main;
import fr.tt54.othello.bots.utils.EvaluationFunction;
import fr.tt54.othello.bots.utils.MoveChain;
import fr.tt54.othello.bots.utils.MoveEvaluation;
import fr.tt54.othello.data.DataManager;
import fr.tt54.othello.data.openings.OpeningTree;
import fr.tt54.othello.game.OthelloGame;

import java.util.*;

public abstract class Bot {

    public static final Random random = new Random();
    public static final int TIME_BETWEEN_MOVES = 0; // Temps, en ms, entre le moment où un coup est joué sur le plateau et le moment où le calcul du prochain coup est lancé
    public static final double ITERATION_MULTIPLIER_FACTOR = 5.5d;

    private boolean white;

    private int botNumber;

    public Bot(boolean white){
        this.white = white;
    }

    public boolean isWhite() {
        return white;
    }

    public void setWhite(boolean white) {
        this.white = white;
    }

    public int getBotNumber() {
        return botNumber;
    }

    public void setBotNumber(int botNumber) {
        this.botNumber = botNumber;
    }

    /**
     * Permet de jouer le meilleur coup selon le bot dans la position
     *
     * @param game     Partie en cours
     * @param timeLeft Temps restant (en ms) pour jouer les prochains coups de la partie
     * @return true si un coup a pu être joué, false sinon
     */
    public abstract boolean playMove(OthelloGame game, long timeLeft);

    /**
     *
     * @return une copie du bot
     */
    public abstract Bot copy();


    /**
     *
     * @param game la position actuelle
     * @return true si un bon coup a été trouvé et joué dans la base d'ouvertures, false sinon
     */
    public boolean tryOpeningMove(OthelloGame game){
        byte[] playedMoves = Arrays.copyOfRange(game.getPlayedMoves(), 0, game.getMoveCount());

        OpeningTree.OpeningMove move = DataManager.mainOpeningTree.getMoveAfterSequence(playedMoves);

        if(playedMoves.length == 0 || playedMoves[0] == -1){
            game.playMove(4, 5);
            return true;
        } else {
            if (move != null) {
                Map<Byte, OpeningTree.OpeningMove> nextMoves = move.getNextMoves();

                if (nextMoves.size() > 0) {
                    float maxScore = -Float.MAX_VALUE;
                    OpeningTree.OpeningMove bestMove = null;

                    for (byte m : nextMoves.keySet()) {
                        OpeningTree.OpeningMove openingMove = nextMoves.get(m);
                        float openingScore = this.isWhite() ? openingMove.getScore() : -openingMove.getScore();
                        if (openingScore > maxScore) {
                            bestMove = openingMove;
                            maxScore = openingScore;
                        }
                    }


                    if (maxScore >= 0) {
                        int[] movePos = OthelloGame.intToPosition(bestMove.getMove());
                        game.playMove(movePos[0], movePos[1]);
                        return true;
                    }
                }
            }
        }

        return false;
    }


    /**
     *
     * @param startingPosition position initiale
     * @param timeLeft temps restant pour jouer le coup
     * @param previousDepth profondeur de la recherche précédente
     * @param previousIterationDuration durée de la recherche précédente
     * @param evaluationFunction la fonction d'évaluation utilisée
     * @return true si un coup a été joué, false sinon
     */
    public static boolean iterativeSearch(OthelloGame startingPosition, long timeLeft, int previousDepth, long previousIterationDuration, EvaluationFunction evaluationFunction){
        long estimatedTime = (long) (previousIterationDuration * ITERATION_MULTIPLIER_FACTOR);

        if(estimatedTime < timeLeft){
            long time = System.currentTimeMillis();
            MoveEvaluation result = alphaBeta(startingPosition.clone(), previousDepth + 1, Integer.MIN_VALUE, Integer.MAX_VALUE, evaluationFunction);
            long elapsedTime = System.currentTimeMillis() - time;

            if (result.isFinalEvaluation() || !iterativeSearch(startingPosition, timeLeft - elapsedTime, previousDepth + 1, elapsedTime, evaluationFunction)) {
                startingPosition.playMove(result.getMoveChain().getPosition());
            }
            return true;
        }

        return false;
    }


    public static MoveEvaluation alphaBeta(OthelloGame startingPosition, int depth, int alpha, int beta, EvaluationFunction evaluationFunction){
        if(depth == 0 || startingPosition.isGameFinished()){
            return new MoveEvaluation((int) evaluationFunction.evaluate(startingPosition), null, startingPosition.isGameFinished());
        }

        if(startingPosition.isWhiteToPlay()){
            MoveEvaluation bestMove = new MoveEvaluation(Integer.MIN_VALUE, null, false);
            for(int move : startingPosition.getAvailablePlacements()){
                OthelloGame game = startingPosition.clone();
                boolean previousPlayer = game.isWhiteToPlay();
                int[] moveCoordinates = OthelloGame.intToPosition(move);
                boolean canAdversaryPlay = !game.playMove(moveCoordinates[0], moveCoordinates[1]);

                MoveEvaluation currentEval = alphaBeta(game, depth - 1, alpha, beta, evaluationFunction);
                int max = Math.max(bestMove.getEvaluation(), currentEval.getEvaluation());
                if(max != bestMove.getEvaluation()){
                    bestMove = new MoveEvaluation(max, new MoveChain(null, currentEval.getMoveChain(), move, previousPlayer), currentEval.isFinalEvaluation());
                }

                if(beta <= max){
                    // On fait une coupure beta
                    // ==> beta étant la plus petite valeur déjà enregistrée par le noeud parent,
                    //     on sait que notre max sera plus grand que beta, on peut donc arrêter les recherches ici : cette branche n'aura
                    //     aucune influence sur le reste de l'arbre
                    return bestMove;
                }

                alpha = Math.max(alpha, max);
            }
            return bestMove;
        } else {
            MoveEvaluation bestMove = new MoveEvaluation(Integer.MAX_VALUE, null, false);

            for(int move : startingPosition.getAvailablePlacements()){
                OthelloGame game = startingPosition.clone();
                boolean previousPlayer = game.isWhiteToPlay();
                int[] moveCoordinates = OthelloGame.intToPosition(move);
                boolean canAdversaryPlay = !game.playMove(moveCoordinates[0], moveCoordinates[1]);

                MoveEvaluation currentEval = alphaBeta(game, depth - 1, alpha, beta, evaluationFunction);
                int min = Math.min(bestMove.getEvaluation(), currentEval.getEvaluation());
                if(min != bestMove.getEvaluation()){
                    bestMove = new MoveEvaluation(min, new MoveChain(null, currentEval.getMoveChain(), move, previousPlayer), currentEval.isFinalEvaluation());
                }

                if(alpha >= min){
                    // On fait une coupure alpha
                    // ==> alpha étant la plus grande valeur déjà enregistrée par le noeud parent,
                    //     on sait que notre min sera plus petit qu'alpha, on peut donc arrêter les recherches ici : cette branche n'aura
                    //     aucune influence sur le reste de l'arbre
                    return bestMove;
                }

                beta = Math.min(beta, min);
            }
            return bestMove;
        }
    }


    /**
     *
     * @param amount Nombre de parties à jouer
     * @param timePerGame Temps (en ms) que possède le programme pour jouer chaque partie
     * @return Un tableau contenant [nbr_victoires, nbr_nulles, nbr_défaites]
     */
    public int[] playGamesAgainstRandom(int amount, long timePerGame){
        int[] score = new int[3];

        boolean color = true; // true si le bot est blanc, false s'il est noir
        for(int i = 0; i < amount; i++){
            long timeLeft = timePerGame;
            OthelloGame game = new OthelloGame();

            while(!game.isGameFinished()){
                if(color == game.isWhiteToPlay()){
                    long beginTime = System.currentTimeMillis();
                    playMove(game, timeLeft);
                    timeLeft -= (System.currentTimeMillis() - beginTime);
                } else {
                    List<Integer> moves = new ArrayList<>(game.getAvailablePlacements());
                    int[] move = OthelloGame.intToPosition(moves.get(random.nextInt(moves.size())));
                    game.playMove(move[0], move[1]);
                }
            }

            if(game.getWhitePiecesCount() > game.getBlackPiecesCount()){
                score[color ? 0 : 2] += 1;
            } else if(game.getWhitePiecesCount() < game.getBlackPiecesCount()){
                score[color ? 2 : 0] += 1;
            } else {
                score[1] += 1;
            }

            color = !color;
        }

        return score;
    }

    /**
     * Permet de faire s'affronter deux programmes
     * @param bot1 Le premier programme
     * @param bot2 Le second programme
     * @param amount Le nombre de parties jouées
     * @param timePerPlayer Le temps (en ms) que possède chaque joueur dans chaque partie
     * @return Le résultat des parties
     */
    public static GameResults confrontBots(Bot bot1, Bot bot2, int amount, long timePerPlayer, boolean showGame){
        int[] score = new int[3];
        GameResult[] results = new GameResult[amount];

        boolean bot1Color = bot1.isWhite(); // true si bot1 est blanc, false s'il est noir

        for(int i = 0; i < amount; i++){
            long bot1TimeLeft = timePerPlayer;
            long bot2TimeLeft = timePerPlayer;
            OthelloGame game = new OthelloGame();

            bot1.setWhite(bot1Color);
            bot2.setWhite(!bot1Color);

            if(showGame){
                Main.othelloGraphicManager.playAgainstBot(null);
                Main.othelloGraphicManager.setGame(game);
            }

            while(!game.isGameFinished()){
                long beginTime = System.currentTimeMillis();
                if(bot1Color == game.isWhiteToPlay()){
                    bot1.playMove(game, bot1TimeLeft);
                    bot1TimeLeft -= (System.currentTimeMillis() - beginTime);
                } else {
                    bot2.playMove(game, bot2TimeLeft);
                    bot2TimeLeft -= (System.currentTimeMillis() - beginTime);
                }

                if(showGame) {
                    try {
                        Thread.sleep(TIME_BETWEEN_MOVES);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if(game.getWhitePiecesCount() > game.getBlackPiecesCount()){
                score[bot1Color ? 0 : 2] += 1;

                if(bot1.isWhite()){
                    System.out.println(bot1.getClass().getName());
                } else {
                    System.out.println(bot2.getClass().getName());
                }
            } else if(game.getWhitePiecesCount() < game.getBlackPiecesCount()){
                score[bot1Color ? 2 : 0] += 1;

                if(!bot1.isWhite()){
                    System.out.println(bot1.getClass().getName());
                } else {
                    System.out.println(bot2.getClass().getName());
                }
            } else {
                score[1] += 1;

                System.out.println("Draw");
            }


            System.out.println("Game ended on : " + game.getBlackPiecesCount() + " (b) - " + game.getWhitePiecesCount() + " (w)");
            System.out.println("Game Transcription :");
            System.out.println(game.getGameTranscription());

            System.out.println();

            GameResult result = new GameResult(bot1.isWhite() ? game.getWhitePiecesCount() : game.getBlackPiecesCount(),
                    bot2.isWhite() ? game.getWhitePiecesCount() : game.getBlackPiecesCount());
            results[i] = result;

            bot1Color = !bot1Color;
        }

        return new GameResults(results);
    }

    public record GameResult(int player1Score, int player2Score){

        /**
         *
         * @return -1 si le joueur 2 a gagné, 0 s'il y a eu nulle, 1 sinon
         */
        public int getScore(){
            return Integer.compare(player1Score, player2Score);
        }

    }

    public record GameResults(GameResult[] results){

        /**
         *
         * @return La somme du nombre de pions qu'il restait au joueur 1 à la fin de chaque partie
         */
        public int getTotalPlayer1Pawns(){
            int sum = 0;
            for(GameResult result : results){
                sum += result.player1Score;
            }
            return sum;
        }

        /**
         *
         * @return La somme du nombre de pions qu'il restait au joueur 2 à la fin de chaque partie
         */
        public int getTotalPlayer2Pawns(){
            int sum = 0;
            for(GameResult result : results){
                sum += result.player2Score;
            }
            return sum;
        }

        /**
         *
         * @return La somme des résultats des parties du joueur 1. Chaque partie vaut +1 en cas de victoire, +1/2 en cas de nulle, 0 en cas de défaite
         */
        public float getPlayer1Result(){
            float sum = 0;
            for(GameResult result : results){
                sum += (result.getScore() + 1) / 2f;
            }
            return sum;
        }

        /**
         *
         * @return La somme des résultats des parties du joueur 2. Chaque partie vaut +1 en cas de victoire, +1/2 en cas de nulle, 0 en cas de défaite
         */
        public float getPlayer2Result(){
            float sum = 0;
            for(GameResult result : results){
                sum += (-result.getScore() + 1) / 2f;
            }
            return sum;
        }

        /**
         *
         * @return un tableau contenant [victoires bot1, nulles, défaites bot1]
         */
        public int[] getScore(){
            int[] score = new int[3];
            for(GameResult result : results){
                if(result.getScore() == 1){
                    score[0]++;
                } else if(result.getScore() == 0){
                    score[1]++;
                } else {
                    score[2]++;
                }
            }
            return score;
        }

        @Override
        public String toString() {
            return "GameResults{" +
                    "results=" + Arrays.toString(this.getScore()) +
                    '}';
        }

        /**
         * Renvoie le résultat global de la suite de parties, i.e. celui qui a cumulé le plus de pions à la fin des n parties
         * @return -1 si le joueur 2 a gagné, 0 s'il y a eu nulle, 1 sinon
         */
        public int getGlobalResult(){
            return Integer.compare(this.getTotalPlayer1Pawns(), this.getTotalPlayer2Pawns());
        }

    }

}
