package fr.tt54.othello;

import fr.tt54.othello.bots.Bot;
import fr.tt54.othello.bots.TableEvalBot;
import fr.tt54.othello.data.DataManager;
import fr.tt54.othello.data.genetic.GeneticAlgorithm;
import fr.tt54.othello.game.OthelloGame;
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

        OthelloGame game = OthelloGame.generateEmptyBoard();
        game.placePiece(1, 7, false);
        game.placePiece(3, 7, true);
        game.placePiece(4, 7, false);
        game.placePiece(5, 7, true);
        game.placePiece(6, 7, true);
        game.placePiece(7, 7, true);
        othelloGraphicManager.setGame(game);

        //othelloGraphicManager.playAgainstBot(GeneticAlgorithm.FIRST_ATTEMPT_BOT);
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



/*        Bot bot = GeneticAlgorithm.FIRST_ATTEMPT_BOT.copy();
        Bot adversary = new TableEvalBot(false);
        bot.depthSearch = 7;
        adversary.depthSearch = 7;
        bot.setWhite(false);

        othelloGraphicManager.playAgainstBot(bot);*/

        //System.out.println(Arrays.toString(Bot.confrontBots(bot, adversary, 2, 2 * 1000, true).getScore()));

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
