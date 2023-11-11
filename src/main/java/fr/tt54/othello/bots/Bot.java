package fr.tt54.othello.bots;

import fr.tt54.othello.game.OthelloGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Bot {

    public static final Random random = new Random();

    public Bot(){}


    /**
     * Permet de jouer le meilleur coup selon le bot dans la position
     * @param game Partie en cours
     * @param timeLeft Temps restant (en ms) pour jouer les prochains coups de la partie
     */
    public abstract void playMove(OthelloGame game, long timeLeft);


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
     * @param timePerGame Le temps (en ms) que possède chaque joueur dans chaque partie
     * @return Le tableau des scores sous la forme [victoires_bot1, nulles, victoires_bot2]
     */
    public static int[] confrontBots(Bot bot1, Bot bot2, int amount, long timePerGame){
        int[] score = new int[3];

        boolean bot1Color = true; // true si bot1 est blanc, false s'il est noir

        for(int i = 0; i < amount; i++){
            long bot1TimeLeft = timePerGame;
            long bot2TimeLeft = timePerGame;
            OthelloGame game = new OthelloGame();

            while(!game.isGameFinished()){
                if(bot1Color == game.isWhiteToPlay()){
                    long beginTime = System.currentTimeMillis();
                    bot1.playMove(game, bot1TimeLeft);
                    bot1TimeLeft -= (System.currentTimeMillis() - beginTime);
                } else {
                    long beginTime = System.currentTimeMillis();
                    bot2.playMove(game, bot2TimeLeft);
                    bot2TimeLeft -= (System.currentTimeMillis() - beginTime);
                }
            }

            if(game.getWhitePiecesCount() > game.getBlackPiecesCount()){
                score[bot1Color ? 0 : 2] += 1;
            } else if(game.getWhitePiecesCount() < game.getBlackPiecesCount()){
                score[bot1Color ? 2 : 0] += 1;
            } else {
                score[1] += 1;
            }

            bot1Color = !bot1Color;
        }


        return score;
    }

}
