package fr.tt54.othello;

import fr.tt54.othello.bots.AdvancedPatternEvalBot;
import fr.tt54.othello.bots.Bot;
import fr.tt54.othello.bots.TableEvalBot;
import fr.tt54.othello.data.DataManager;
import fr.tt54.othello.game.OthelloGraphicManager;
import fr.ttgraphiclib.GraphicManager;
import fr.ttgraphiclib.graphics.GraphicPanel;
import fr.ttgraphiclib.graphics.events.listener.UserListener;
import fr.ttgraphiclib.thread.Frame;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Random;

public class Main extends UserListener {

    public static final Random random = new Random();
    public static final GraphicPanel panel = new GraphicPanel();
    public static OthelloGraphicManager othelloGraphicManager;


    public static void main(String[] args) {
        DataManager.enable();
        GraphicManager.setMaxFPS(30);
        GraphicManager.setMaxMovePerSecond(30);
        Frame frame = new Frame("Othello", 900, 900);
        GraphicManager.enable(frame, panel);
        frame.setMainClass(othelloGraphicManager = new OthelloGraphicManager());

        //othelloGraphicManager.playAgainstBot(new OpeningTestBot(true));
        //playRandomGames();

        //Bot.confrontBots(new OpeningTestBot(true), new OpeningTestBot(false), 8, 60 * 1000, true);


/*        System.out.println(Pattern.getPatternFromPosition(Pattern.PatternType.MAIN_DIAGONAL, Pattern.GameStage.MID_GAME,
                othelloGraphicManager.getGame().getPattern(Pattern.PatternType.MAIN_DIAGONAL.getPatternsLocations()[0]), othelloGraphicManager.getGame().isWhiteToPlay()).getPatternValue());

        Pattern p = new Pattern(othelloGraphicManager.getGame().getPattern(Pattern.PatternType.MAIN_DIAGONAL.getPatternsLocations()[0]), false);
        System.out.println(p.hashCode());
        System.out.println(Pattern.getPatternFromPosition(Pattern.PatternType.MAIN_DIAGONAL, Pattern.GameStage.MID_GAME, p.getPawns(), true).getPatternValue());

        Pattern p2 = Pattern.getPatternFromPosition(Pattern.PatternType.MAIN_DIAGONAL, Pattern.GameStage.MID_GAME,
                othelloGraphicManager.getGame().getPattern(Pattern.PatternType.MAIN_DIAGONAL.getPatternsLocations()[0]), othelloGraphicManager.getGame().isWhiteToPlay());
        System.out.println(p2.hashCode());
        System.out.println(p2.getGameWonAmount());
        System.out.println(p2.getPlayedGamesAmount());*/


        System.out.println(Arrays.toString(Bot.confrontBots(new AdvancedPatternEvalBot(true), new TableEvalBot(false), 2, 30 * 1000, true)));

        //System.out.println(Arrays.toString(Bot.confrontBots(new RandomBot(true), new OpeningTestBot(false), 100, 30 * 1000, true)));

/*        byte[][] board = new byte[8][8];
        for(int[][] positions : Pattern.PatternType.DIAGONAL_7.getPatternsLocations()){
            for(int i = 0; i < positions.length; i++){
                board[positions[i][0]][positions[i][1]] = 1;
            }
        }
        othelloGraphicManager.setGame(new OthelloGame(board));*/

    }

    @Override
    public void onKeyPressed(KeyEvent e) {

    }
}
