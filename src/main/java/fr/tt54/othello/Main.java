package fr.tt54.othello;

import fr.tt54.othello.game.OthelloGraphicManager;
import fr.ttgraphiclib.GraphicManager;
import fr.ttgraphiclib.graphics.GraphicPanel;
import fr.ttgraphiclib.graphics.events.listener.UserListener;
import fr.ttgraphiclib.thread.Frame;

import java.awt.event.KeyEvent;
import java.util.Random;

public class Main extends UserListener {

    public static final Random random = new Random();
    public static final GraphicPanel panel = new GraphicPanel();
    public static OthelloGraphicManager othelloGraphicManager;


    public static void main(String[] args) {
        GraphicManager.setMaxFPS(30);
        GraphicManager.setMaxMovePerSecond(30);
        Frame frame = new Frame("Othello", 900, 900);
        GraphicManager.enable(frame, panel);
        frame.setMainClass(othelloGraphicManager = new OthelloGraphicManager());

        //othelloGraphicManager.playAgainstBot(new OpeningTestBot(true));
        //playRandomGames();

        //Bot.confrontBots(new OpeningTestBot(true), new OpeningTestBot(false), 8, 60 * 1000, true);
        //System.out.println(Arrays.toString(Bot.confrontBots(new RandomBot(true), new OpeningTestBot(false), 100, 30 * 1000, true)));
        //System.out.println(Arrays.toString(Bot.confrontBots(new RandomBot(true), new OpeningTestBot(false), 100, 30 * 1000, true)));
    }

    @Override
    public void onKeyPressed(KeyEvent e) {

    }
}
