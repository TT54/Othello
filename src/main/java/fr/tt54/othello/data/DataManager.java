package fr.tt54.othello.data;

import fr.tt54.othello.data.openings.OpeningLoader;
import fr.tt54.othello.data.openings.OpeningTree;
import fr.tt54.othello.game.OthelloGame;

import java.io.FileNotFoundException;

public class DataManager {

    public static OpeningTree mainOpeningTree;

    private static boolean enabled = false;

    public static void main(String[] args) {
        DataManager.enable();
    }


    public static void enable(){
        if(enabled) return;
        enabled = true;

        mainOpeningTree = new OpeningTree((byte) OthelloGame.positionToInt(5, 4));
        try {
            OpeningLoader.loadGames(OpeningLoader.HUMAN_GAME_FOLDER);
            OpeningLoader.loadGames(OpeningLoader.LOGISTELLO_GAME_FOLDER);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println(mainOpeningTree.getMoveAfterSequence(new byte[] {
                (byte) OthelloGame.positionToInt(4, 5),
                (byte) OthelloGame.positionToInt(3, 5),
                (byte) OthelloGame.positionToInt(2, 4),
                (byte) OthelloGame.positionToInt(1, 3),
                (byte) OthelloGame.positionToInt(1, 4)}
        ).showNextMoves());

        System.out.println("------");

        System.out.println(mainOpeningTree.getMoveAfterSequence(new byte[] {
                (byte) OthelloGame.positionToInt(4, 5)}
        ).showNextMoves());
    }

}
