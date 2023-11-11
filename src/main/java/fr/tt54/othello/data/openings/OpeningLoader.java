package fr.tt54.othello.data.openings;

import fr.tt54.othello.data.DataManager;
import fr.tt54.othello.data.WTHORReader;
import fr.tt54.othello.data.objects.PlayedGame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class OpeningLoader {

    public static final String HUMAN_GAME_FOLDER = "/games/human_games/";
    public static final String LOGISTELLO_GAME_FOLDER = "/games/logistello_games/";

    public static void loadGames(String resourceFolder) throws FileNotFoundException {
        File folder = new File(OpeningLoader.class.getResource(resourceFolder).getFile());
        if(!folder.isDirectory())
            return;

        for (File file : folder.listFiles()){
            PlayedGame[] games = WTHORReader.readWTHORFile(new FileInputStream(file));

            for(PlayedGame game : games){
                int[] moves = game.moves();

                int score;
                if(game.theoreticalBlackScore() >= 33){
                    score = -1;
                } else if(game.theoreticalBlackScore() == 32){
                    score = 0;
                } else {
                    score = 1;
                }

                byte[] firstMoves = new byte[10];
                for(int i = 0; i < 10; i++){
                    firstMoves[i] = (byte) moves[i];
                }
                DataManager.mainOpeningTree.insertMoveSequence(firstMoves, score);
            }
        }
    }

}
