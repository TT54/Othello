package fr.tt54.othello.bots;

import fr.tt54.othello.Main;
import fr.tt54.othello.OthelloGame;
import fr.tt54.othello.bots.utils.EvaluationFunction;
import fr.tt54.othello.bots.utils.MoveChain;
import fr.tt54.othello.bots.utils.MoveEvaluation;
import fr.tt54.othello.data.DataManager;
import fr.tt54.othello.data.genetic.GeneticAlgorithm;
import fr.tt54.othello.data.openings.OpeningTree;

import java.util.*;

@SuppressWarnings("all")
public abstract class Bot {

    public static final Random random = new Random();
    public static final int TIME_BETWEEN_MOVES = 0; // Temps, en ms, entre le moment où un coup est joué sur le plateau et le moment où le calcul du prochain coup est lancé
    public static final double ITERATION_MULTIPLIER_FACTOR = 7d;

    private boolean white;

    private int botNumber;

    public int depthSearch = -1; // Profondeur de recherche de l'algorithme alpha-beta. Si la profondeur vaut -1, le programme
    // joue en fonction du temps à sa disposition

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
     * @param game     Partie en cours
     * @param timeLeft Temps restant (en ms) pour jouer les prochains coups de la partie
     * @return true si un coup a pu être joué, false sinon
     */
    public abstract boolean playMove(OthelloGame game, long timeLeft);

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

    /**
     *
     * @param startingPosition   position de départ
     * @param depth              profondeur de recherche
     * @param evaluationFunction fonction d'évaluation
     * @return un tableau contenant {évaluation, meilleur coup}
     */
    public static int[] minMax(OthelloGame startingPosition, int depth, EvaluationFunction evaluationFunction){
        if(depth == 0 || startingPosition.isGameFinished()){
            // Si on a atteint une racine, on renvoie uniquement l'évaluation
            return new int[] {(int) evaluationFunction.evaluate(startingPosition)};
        }

        // Si c'est aux blancs de jouer, on est dans le cas max
        if(startingPosition.isWhiteToPlay()){
            int max = Integer.MIN_VALUE; // Evaluation maximale
            int bestMove = -1; // Meilleur coup trouvé

            for(int move : startingPosition.getAvailablePlacements()){
                OthelloGame game = startingPosition.clone();
                int[] moveCoordinates = OthelloGame.intToPosition(move); // Récupération des coordonnées du coup
                game.playMove(moveCoordinates[0], moveCoordinates[1]);

                int previousMax = max;
                // On compare le maximum précédent à la valeur de min-max après avoir joué le coup
                max = Math.max(max, minMax(game, depth - 1, evaluationFunction)[0]);
                if(max != previousMax){
                    bestMove = move;
                }
            }
            return new int[] {max, bestMove};
        } else {
            int min = Integer.MAX_VALUE; // Evaluation minimale
            int bestMove = -1; // Meilleur coup trouvé

            for(int move : startingPosition.getAvailablePlacements()){
                OthelloGame game = startingPosition.clone();
                int[] moveCoordinates = OthelloGame.intToPosition(move);
                game.playMove(moveCoordinates[0], moveCoordinates[1]); // Récupération des coordonnées du coup

                int previousMin = min;
                // On compare le minimum précédent à la valeur de min-max après avoir joué le coup
                min = Math.min(min, minMax(game, depth - 1, evaluationFunction)[0]);
                if(min != previousMin){
                    bestMove = move;
                }
            }
            return new int[] {min, bestMove};
        }
    }


    public static MoveEvaluation alphaBeta(OthelloGame startingPosition, int depth, int alpha, int beta, EvaluationFunction evaluationFunction) {
        if (depth == 0 || startingPosition.isGameFinished()) {
            // Cas de base
            return new MoveEvaluation(
                    (int) evaluationFunction.evaluate(startingPosition),
                    null,
                    startingPosition.isGameFinished());
        }

        if (startingPosition.isWhiteToPlay()) {
            MoveEvaluation bestMove = new MoveEvaluation(Integer.MIN_VALUE, null, false);

            for (int move : startingPosition.getAvailablePlacements()) {
                OthelloGame game = startingPosition.clone();
                boolean previousPlayer = game.isWhiteToPlay();
                int[] moveCoordinates = OthelloGame.intToPosition(move);
                game.playMove(moveCoordinates[0], moveCoordinates[1]);

                // On évalue la position puis on la compare au max
                MoveEvaluation currentEval = alphaBeta(game, depth - 1, alpha, beta, evaluationFunction);
                int max = Math.max(bestMove.getEvaluation(), currentEval.getEvaluation());
                if (max != bestMove.getEvaluation()) {
                    bestMove = new MoveEvaluation(
                            max,
                            new MoveChain(null, currentEval.getMoveChain(), move, previousPlayer),
                            currentEval.isFinalEvaluation());
                }

                if (beta <= max) {
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
                game.playMove(moveCoordinates[0], moveCoordinates[1]);

                // On évalue la position et on la compare au min
                MoveEvaluation currentEval = alphaBeta(game, depth - 1, alpha, beta, evaluationFunction);
                int min = Math.min(bestMove.getEvaluation(), currentEval.getEvaluation());
                if(min != bestMove.getEvaluation()){
                    bestMove = new MoveEvaluation(
                            min,
                            new MoveChain(null, currentEval.getMoveChain(), move, previousPlayer),
                            currentEval.isFinalEvaluation());
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

    public static BotEvaluationResult evaluateBot(Bot bot, Bot[] adversaries, int[] evaluationDepths, int[] gamesAmountPerDepth){
        int[][] depthBotVictories = new int[adversaries.length][evaluationDepths.length]; // Contient le nombre de parties gagnées contre chaque bot à chaque profondeur
        int[][] depthBotDraw = new int[adversaries.length][evaluationDepths.length]; // Contient le nombre de parties nulles contre chaque bot à chaque profondeur
        int[][] depthBotLooses = new int[adversaries.length][evaluationDepths.length]; // Contient le nombre de parties perdues contre chaque bot à chaque profondeur
        float[][] depthConfrontationResults = new float[adversaries.length][evaluationDepths.length]; // Contient le "score" des confrontations contre chaque adversaire à chaque profondeur

        for (int i = 0; i < evaluationDepths.length; i++) {
            int depth = evaluationDepths[i];
            bot.depthSearch = depth;

            System.out.println("#### DEPTH " + depth + " ####");

            for(int k = 0; k < gamesAmountPerDepth[i]; k++) {
                System.out.println("#### Game " + (k+1) + "/" + gamesAmountPerDepth[i] +" ####");

                OthelloGame beginningPosition = new OthelloGame();

                // On génère une position aléatoire à partir de laquelle évaluer les différents programmes
                for (int j = 1; j <= random.nextInt(11); j++) {
                    List<Integer> availableMoves = new ArrayList<>(beginningPosition.getAvailablePlacements());
                    beginningPosition.playMove(availableMoves.get(random.nextInt(availableMoves.size())));
                }

                // Chaque programme affronte le programme de référence 'bot'
                for (int j = 0; j < adversaries.length; j++) {
                    adversaries[j].depthSearch = depth;
                    Bot.GameResults results = GeneticAlgorithm.Individu.launchEvaluationGame(bot.copy(), adversaries[j].copy(), beginningPosition);

                    // On enregistre les résultats
                    depthBotVictories[j][i] += results.getScore()[0];
                    depthBotDraw[j][i] += results.getScore()[1];
                    depthBotLooses[j][i] += results.getScore()[2];
                    depthConfrontationResults[j][i] += (results.getGlobalResult() + 1f) / 2;
                }
            }
        }

        System.out.println("");

        for(int i = 0; i < depthBotVictories.length; i++){
            System.out.println("Bot VS Adversaire " + i);
            System.out.println("Victoires : " + Arrays.toString(depthBotVictories[i]));
            System.out.println("Nulles : " + Arrays.toString(depthBotDraw[i]));
            System.out.println("Défaites : " + Arrays.toString(depthBotLooses[i]));
            System.out.println("Score total des confrontations : " + Arrays.toString(depthConfrontationResults[i]));
            System.out.println(" ------ ");
        }

        return new BotEvaluationResult(true, depthBotVictories, depthBotDraw, depthBotLooses, depthConfrontationResults);
    }


    static int currentDepth = 0; // Profondeur à laquelle les programmes sont actuellement évalués
    static int gamesPlayedAtCurrentDepth = 0; // Nombre de parties jouées à la profondeur actuelle
    static int currentAdversary = 0; // Indice de l'adversaire qui est actuellement en train d'être affronté
    static int finishedThreads = 0; // Nombre de threads ayant terminé leur travail

    public static BotEvaluationResult evaluateBotAsync(Bot bot, Bot[] adversaries, int[] evaluationDepths, int[] gamesAmountPerDepth, int threadAmount, boolean showInformations){
        int[][] depthBotVictories = new int[adversaries.length][evaluationDepths.length]; // Contient le nombre de parties gagnées contre chaque bot à chaque profondeur
        int[][] depthBotDraw = new int[adversaries.length][evaluationDepths.length]; // Contient le nombre de parties nulles contre chaque bot à chaque profondeur
        int[][] depthBotLooses = new int[adversaries.length][evaluationDepths.length]; // Contient le nombre de parties perdues contre chaque bot à chaque profondeur
        float[][] depthConfrontationResults = new float[adversaries.length][evaluationDepths.length]; // Contient le "score" des confrontations contre chaque adversaire à chaque profondeur
        BotEvaluationResult result = new BotEvaluationResult();

        for(int i = 0; i < threadAmount; i++){
            Thread thread = new Thread(){

                private int threadDepth = 0; // Profondeur à laquelle doit évaluer le thread
                private int threadAdversary = 0; // Adversaire contre lequel le thread évalue

                @Override
                public void run() {
                    while(!this.isInterrupted()) {
                        gamesPlayedAtCurrentDepth++;

                        // On a joué toutes les parties requises à la profondeur donnée, on passe à la profondeur suivante
                        if (gamesPlayedAtCurrentDepth >= gamesAmountPerDepth[threadDepth]) {
                            gamesPlayedAtCurrentDepth = 0;
                            currentDepth++;

                            if(currentDepth < evaluationDepths.length) {
                                System.out.println("#### DEPTH " + evaluationDepths[currentDepth] + " ####");
                            }
                        }

                        // On a joué toutes les parties contre l'adversaire actuel, on passe à l'adversaire suivant
                        if (currentDepth >= evaluationDepths.length) {
                            currentAdversary++;
                            currentDepth = 0;
                        }

                        // On a terminé d'évaluer le programme
                        if (currentAdversary >= adversaries.length) {
                            if(showInformations) {
                                System.out.println("");

                                for (int i = 0; i < depthBotVictories.length; i++) {
                                    System.out.println("Bot VS Adversaire " + i);
                                    System.out.println("Victoires : " + Arrays.toString(depthBotVictories[i]));
                                    System.out.println("Nulles : " + Arrays.toString(depthBotDraw[i]));
                                    System.out.println("Défaites : " + Arrays.toString(depthBotLooses[i]));
                                    System.out.println("Score total des confrontations : " + Arrays.toString(depthConfrontationResults[i]));
                                    System.out.println(" ------ ");
                                }
                            }

                            finishedThreads++;
                            if(finishedThreads == threadAmount){
                                result.setDepthBotVictories(depthBotVictories);
                                result.setDepthBotDraw(depthBotDraw);
                                result.setDepthBotLooses(depthBotLooses);
                                result.setDepthConfrontationResults(depthConfrontationResults);
                                result.finishEvaluation();

                                currentDepth = 0;
                                gamesPlayedAtCurrentDepth = 0;
                                currentAdversary = 0;
                                finishedThreads = 0;
                            }

                            interrupt();
                            return;
                        }

                        this.threadAdversary = currentAdversary;
                        this.threadDepth = currentDepth;

                        int depth = evaluationDepths[threadDepth];

                        // On génère une position aléatoire à partir de laquelle évaluer les différents programmes
                        OthelloGame beginningPosition = new OthelloGame();
                        for (int j = 1; j <= random.nextInt(11); j++) {
                            List<Integer> availableMoves = new ArrayList<>(beginningPosition.getAvailablePlacements());
                            beginningPosition.playMove(availableMoves.get(random.nextInt(availableMoves.size())));
                        }

                        adversaries[threadAdversary].depthSearch = depth;
                        bot.depthSearch = depth;
                        Bot.GameResults results = GeneticAlgorithm.Individu.launchEvaluationGame(bot.copy(), adversaries[threadAdversary].copy(), beginningPosition);

                        // On enregistre les résultats
                        depthBotVictories[threadAdversary][threadDepth] += results.getScore()[0];
                        depthBotDraw[threadAdversary][threadDepth] += results.getScore()[1];
                        depthBotLooses[threadAdversary][threadDepth] += results.getScore()[2];
                        depthConfrontationResults[threadAdversary][threadDepth] += (results.getGlobalResult() + 1f) / 2;
                    }
                }
            };
            thread.start();
        }

        return result;
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

    public static class BotEvaluationResult{

        private boolean evaluationFinished = false;
        private int[][] depthBotVictories; // Contient le nombre de parties gagnées contre chaque bot à chaque profondeur
        private int[][] depthBotDraw; // Contient le nombre de parties nulles contre chaque bot à chaque profondeur
        private int[][] depthBotLooses; // Contient le nombre de parties perdues contre chaque bot à chaque profondeur
        private float[][] depthConfrontationResults; // Contient le "score" des confrontations contre chaque adversaire à chaque profondeur

        public BotEvaluationResult(boolean evaluationFinished, int[][] depthBotVictories, int[][] depthBotDraw, int[][] depthBotLooses, float[][] depthConfrontationResults) {
            this.evaluationFinished = evaluationFinished;
            this.depthBotVictories = depthBotVictories;
            this.depthBotDraw = depthBotDraw;
            this.depthBotLooses = depthBotLooses;
            this.depthConfrontationResults = depthConfrontationResults;
        }

        public BotEvaluationResult() {}

        public void finishEvaluation(){
            this.evaluationFinished = true;
        }

        public void setDepthBotVictories(int[][] depthBotVictories) {
            this.depthBotVictories = depthBotVictories;
        }

        public void setDepthBotDraw(int[][] depthBotDraw) {
            this.depthBotDraw = depthBotDraw;
        }

        public void setDepthBotLooses(int[][] depthBotLooses) {
            this.depthBotLooses = depthBotLooses;
        }

        public void setDepthConfrontationResults(float[][] depthConfrontationResults) {
            this.depthConfrontationResults = depthConfrontationResults;
        }

        public boolean isEvaluationFinished() {
            return evaluationFinished;
        }

        public int[][] getDepthBotVictories() {
            return depthBotVictories;
        }

        public int[][] getDepthBotDraw() {
            return depthBotDraw;
        }

        public int[][] getDepthBotLooses() {
            return depthBotLooses;
        }

        public float[][] getDepthConfrontationResults() {
            return depthConfrontationResults;
        }
    }

}
